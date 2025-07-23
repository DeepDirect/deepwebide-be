package com.deepdirect.deepwebide_be.member.service;

import java.util.List;
import java.util.regex.Pattern;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.global.security.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.global.security.JwtTokenProvider;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.dto.request.SignInRequest;
import com.deepdirect.deepwebide_be.member.dto.request.SignUpRequest;
import com.deepdirect.deepwebide_be.member.dto.response.SignInResponse;
import com.deepdirect.deepwebide_be.member.dto.response.SignInUserDto;
import com.deepdirect.deepwebide_be.member.dto.response.SignUpResponse;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.member.util.NicknameGenerator;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%]).{8,}$";
    private static final Pattern PASSWORD_REGEX = Pattern.compile(PASSWORD_PATTERN);
    private static final String NAME_PATTERN = "^[가-힣]{2,20}$";
    private static final Pattern NAME_REGEX = Pattern.compile(NAME_PATTERN);

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileImageService profileImageService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;

    private String generateUniqueNickname(String baseNickname) {
        if (!userRepository.existsByNickname(baseNickname)) {
            return baseNickname;
        }

        List<String> existingNicknames = userRepository.findNicknamesByPrefix(baseNickname);

        int maxSuffix = 0;
        for (String nickname : existingNicknames) {
            String suffix = nickname.substring(baseNickname.length());
            if (suffix.matches("\\d+")) {
                int num = Integer.parseInt(suffix);
                maxSuffix = Math.max(maxSuffix, num + 1);
            }
        }

        return baseNickname + maxSuffix;
    }

    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        if (!NAME_REGEX.matcher(request.getUsername()).matches()) {
            throw new GlobalException(ErrorCode.INVALID_USERNAME);
        }

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new GlobalException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        if (!PASSWORD_REGEX.matcher(request.getPassword()).matches()) {
            throw new GlobalException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new GlobalException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new GlobalException(ErrorCode.PHONE_NUMBER_ALREADY_USED);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String nickname = generateUniqueNickname(NicknameGenerator.generate());

        String profileImageUrl = profileImageService.generateProfileImageUrl(nickname, 48);

        // 이메일 인증
        String code = emailVerificationService.createVerification(request.getEmail());
        emailVerificationService.sendVerificationEmail(request.getEmail(), code);

        User user = userRepository.save(User.builder()
                .username(request.getUsername())
                .nickname(nickname)
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .profileImageUrl(profileImageUrl)
                .emailVerified(false)
                .build());

        return SignUpResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @Transactional
    public SignInResponse signIn(SignInRequest request, HttpServletResponse servletResponse) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(ErrorCode.WRONG_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.WRONG_PASSWORD);
        }

        // 1. 토큰 발급 (AccessToken, RefreshToken)
        String accessToken = jwtTokenProvider.createToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 2. 리프레시 토큰 Redis에 저장 (userId -> refreshToken)
        refreshTokenService.save(user.getId(), refreshToken);

        // 3. 리프레시 토큰을 쿠키로 내려주기
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 2주
        refreshCookie.setSecure(false); // 실제 배포시 true(https) 권장!
        servletResponse.addCookie(refreshCookie);

        // 4. 액세스 토큰만 응답 본문으로 전달
        return new SignInResponse(accessToken, new SignInUserDto(user));
    }

    @Transactional
    public void signOut(String authorizationHeader, HttpServletResponse response) {
        // 1. accessToken 추출 및 검증
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        String accessToken = authorizationHeader.replace("Bearer ", "");
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        // 2. userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        // 3. Redis에서 refreshToken 삭제
        refreshTokenService.delete(userId);

        // 4. 쿠키 만료 (refreshToken)
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // 운영환경에서만 true, 개발은 false도 가능
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // SameSite=Strict 명시적 추가
        response.setHeader(
                "Set-Cookie",
                "refreshToken=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0"
        );
    }
}
