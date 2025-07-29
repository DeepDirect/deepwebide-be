package com.deepdirect.deepwebide_be.member.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.global.security.JwtTokenProvider;
import com.deepdirect.deepwebide_be.global.security.RefreshTokenService;
import com.deepdirect.deepwebide_be.member.domain.OauthAccount;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.dto.response.GithubEmailResponse;
import com.deepdirect.deepwebide_be.member.dto.response.GithubUserResponse;
import com.deepdirect.deepwebide_be.member.dto.response.SignInResponse;
import com.deepdirect.deepwebide_be.member.dto.response.SignInUserDto;
import com.deepdirect.deepwebide_be.member.repository.OauthAccountRepository;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.member.util.NicknameGenerator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    private final UserRepository userRepository;
    private final OauthAccountRepository oauthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ProfileImageService profileImageService;
    private final UserService userService;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String githubRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public SignInResponse processGitHubLogin(String code, HttpServletResponse servletResponse) {
        String accessToken = getGitHubAccessToken(code);

        GithubUserResponse githubUser = getGitHubUserInfo(accessToken);

        // 이메일이 null인 경우 별도 API로 가져오기
        if (!StringUtils.hasText(githubUser.getEmail())) {
            String email = getGitHubUserEmail(accessToken);
            githubUser.setEmail(email);
        }

        // 이메일이 여전히 없으면 에러
        if (!StringUtils.hasText(githubUser.getEmail())) {
            throw new GlobalException(ErrorCode.OAUTH_EMAIL_NOT_FOUND);
        }

        Optional<OauthAccount> existingOAuth = oauthAccountRepository.findByProviderAndProviderUserId("github", String.valueOf(githubUser.getId()));

        User user;
        if (existingOAuth.isPresent()) {
            user = existingOAuth.get().getUser();
        } else {
            user = handleNewGitHubUser(githubUser);
        }

        return generateTokenResponse(user, servletResponse);
    }

    private String getGitHubAccessToken(String code) {
        String tokenUrl = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", githubClientId);
        params.add("client_secret", githubClientSecret);
        params.add("code", code);
        params.add("redirect_uri", githubRedirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenUrl, request, TokenResponse.class);

            if (response.getBody() == null || response.getBody().getAccessToken() == null) {
                throw new GlobalException(ErrorCode.OAUTH_TOKEN_ERROR);
            }

            return response.getBody().getAccessToken();
        } catch (Exception e) {
            log.error("GitHub 액세스 토큰 요청 실패", e);
            throw new GlobalException(ErrorCode.OAUTH_TOKEN_ERROR);
        }
    }

    private GithubUserResponse getGitHubUserInfo(String accessToken) {
        String userUrl = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GithubUserResponse> response = restTemplate.exchange(userUrl, HttpMethod.GET, request, GithubUserResponse.class);

            if (response.getBody() == null) {
                throw new GlobalException(ErrorCode.OAUTH_USER_INFO_ERROR);
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("GitHub 사용자 정보 요청 실패", e);
            throw new GlobalException(ErrorCode.OAUTH_USER_INFO_ERROR);
        }
    }

    // 새로 추가된 메서드: GitHub 이메일 정보 가져오기
    private String getGitHubUserEmail(String accessToken) {
        String emailUrl = "https://api.github.com/user/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<GithubEmailResponse>> response = restTemplate.exchange(
                    emailUrl,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<GithubEmailResponse>>() {}
            );

            if (response.getBody() == null || response.getBody().isEmpty()) {
                log.warn("GitHub 이메일 정보를 가져올 수 없습니다.");
                return null;
            }

            // 우선순위: primary이면서 verified인 이메일 > primary인 이메일 > verified인 이메일 > 첫 번째 이메일
            List<GithubEmailResponse> emails = response.getBody();

            // 1순위: primary이면서 verified
            Optional<GithubEmailResponse> primaryVerified = emails.stream()
                    .filter(email -> Boolean.TRUE.equals(email.getPrimary()) && Boolean.TRUE.equals(email.getVerified()))
                    .findFirst();

            if (primaryVerified.isPresent()) {
                return primaryVerified.get().getEmail();
            }

            // 2순위: primary
            Optional<GithubEmailResponse> primary = emails.stream()
                    .filter(email -> Boolean.TRUE.equals(email.getPrimary()))
                    .findFirst();

            if (primary.isPresent()) {
                return primary.get().getEmail();
            }

            // 3순위: verified
            Optional<GithubEmailResponse> verified = emails.stream()
                    .filter(email -> Boolean.TRUE.equals(email.getVerified()))
                    .findFirst();

            if (verified.isPresent()) {
                return verified.get().getEmail();
            }

            // 4순위: 첫 번째 이메일
            return emails.get(0).getEmail();

        } catch (Exception e) {
            log.error("GitHub 이메일 정보 요청 실패", e);
            return null;
        }
    }

    private User handleNewGitHubUser(GithubUserResponse githubUser) {
        Optional<User> existingUser = userRepository.findByEmail(githubUser.getEmail());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = createNewUserFromGitHub(githubUser);
        }

        OauthAccount oauthAccount = OauthAccount.builder()
                .provider("github")
                .providerUserId(String.valueOf(githubUser.getId()))
                .user(user)
                .build();

        oauthAccountRepository.save(oauthAccount);

        return user;
    }

    private User createNewUserFromGitHub(GithubUserResponse githubUser) {
        String nickname = userService.generateUniqueNickname(NicknameGenerator.generate());

        String profileImageUrl = profileImageService.generateProfileImageUrl(nickname, 48);

        User user = User.builder()
                .username(githubUser.getName() != null ? githubUser.getName() : githubUser.getLogin())
                .nickname(nickname)
                .email(githubUser.getEmail())
                .phoneNumber("")
                .password("")
                .profileImageUrl(profileImageUrl)
                .emailVerified(true) // GitHub에서 인증된 이메일로 간주
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public SignInResponse generateTokenResponse(User user, HttpServletResponse servletResponse) {
        String accessToken = jwtTokenProvider.createToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.save(user.getId(), refreshToken);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 2주
        refreshCookie.setSecure(true); // SameSite=None 사용 시 필수
        servletResponse.addCookie(refreshCookie);

        // SameSite=None 설정을 위한 Set-Cookie 헤더 추가
        servletResponse.setHeader(
                "Set-Cookie",
                "refreshToken=" + refreshToken + "; HttpOnly; Secure; Path=/; Max-Age=" + (60 * 60 * 24 * 14) + "; SameSite=None"
        );

        return new SignInResponse(accessToken, new SignInUserDto(user));
    }

    private static class TokenResponse {
        @JsonProperty("access_token")
        private String access_token;

        @JsonProperty("token_type")
        private String token_type;

        @JsonProperty("scope")
        private String scope;

        public String getAccessToken() { return access_token; }
        public void setAccessToken(String access_token) { this.access_token = access_token; }
        public String getTokenType() { return token_type; }
        public void setTokenType(String token_type) { this.token_type = token_type; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }
}