package com.deepdirect.deepwebide_be.member.service;

import java.util.List;
import java.util.regex.Pattern;

import com.deepdirect.deepwebide_be.global.security.ReauthTokenService;
import com.deepdirect.deepwebide_be.global.security.RefreshTokenService;
import com.deepdirect.deepwebide_be.member.domain.AuthType;
import com.deepdirect.deepwebide_be.member.domain.PhoneVerification;
import com.deepdirect.deepwebide_be.member.dto.request.*;
import com.deepdirect.deepwebide_be.member.repository.PhoneVerificationRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.global.security.JwtTokenProvider;
import com.deepdirect.deepwebide_be.member.domain.User;
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
    private final PhoneVerificationRepository verificationRepository;
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%]).{8,}$";
    private static final Pattern PASSWORD_REGEX = Pattern.compile(PASSWORD_PATTERN);
    private static final String NAME_PATTERN = "^[가-힣]{2,20}$";
    private static final Pattern NAME_REGEX = Pattern.compile(NAME_PATTERN);

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileImageService profileImageService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;
    private final ReauthTokenService reauthTokenService;

    public String generateUniqueNickname(String baseNickname) {
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
        Pattern phonePattern = Pattern.compile("^01[0|1|6|7|8|9][0-9]{7,8}$");
        if (!phonePattern.matcher(request.getPhoneNumber()).matches()) {
            throw new GlobalException(ErrorCode.INVALID_PHONE_FORMAT);
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new GlobalException(ErrorCode.PHONE_NUMBER_ALREADY_USED);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String nickname = generateUniqueNickname(NicknameGenerator.generate());
        String profileImageUrl = profileImageService.generateProfileImageUrl(nickname, 48);

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

        if (!user.isEmailVerified()) {
            emailVerificationService.handleEmailVerification(user.getEmail());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.WRONG_PASSWORD);
        }

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

    @Transactional
    public void signOut(String authorizationHeader, HttpServletResponse response) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        String accessToken = authorizationHeader.replace("Bearer ", "");

        jwtTokenProvider.validateToken(accessToken); // 예외 방식으로 변경됨

        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
        refreshTokenService.delete(userId);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // SameSite=None 설정을 위한 Set-Cookie 헤더로 쿠키 삭제
        response.setHeader(
                "Set-Cookie",
                "refreshToken=; HttpOnly; Secure; Path=/; Max-Age=0; SameSite=None"
        );
    }

    public PhoneVerification getVerification(AuthType authType, String phoneNumber, String phoneCode) {
        return verificationRepository
                .findByPhoneNumberAndPhoneCodeAndAuthType(phoneNumber, phoneCode, authType)
                .orElseThrow(() -> new GlobalException(ErrorCode.VERIFICATION_NOT_FOUND));
    }

    @Transactional
    public String findEmail(FindEmailRequest request) {
        AuthType authType = AuthType.FIND_ID;

        PhoneVerification verification = getVerification(authType, request.getPhoneNumber(), request.getPhoneCode());

        if (verification.isVerified()) {
            throw new GlobalException(ErrorCode.ALREADY_VERIFIED);
        }

        User user = userRepository.findByUsernameAndPhoneNumber(
                request.getUsername(), request.getPhoneNumber()
        ).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        verification.verify();
        return user.getEmail();
    }

    public boolean isEmailAlreadyExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GlobalException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return true;
    }

    @Transactional
    public String passwordVerifyUser(PasswordVerifyUserRequest request) {
        AuthType authType = AuthType.FIND_PASSWORD;

        PhoneVerification verification = getVerification(authType, request.getPhoneNumber(), request.getPhoneCode());

        if (verification.isVerified()) {
            throw new GlobalException(ErrorCode.ALREADY_VERIFIED);
        }

        userRepository.findByUsernameAndPhoneNumber(
                request.getUsername(), request.getPhoneNumber()
        ).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        verification.verify();

        String reauthToken = jwtTokenProvider.createReauthenticateToken(
                request.getUsername(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getPhoneCode()
        );

        reauthTokenService.save(request.getEmail(), reauthToken);

        return reauthToken;
    }

    @Transactional
    public void verifyAndResetPassword(PasswordResetRequest request, String authorizationHeader) {
        final AuthType authType = AuthType.FIND_PASSWORD;

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        String reauthToken = authorizationHeader.replace("Bearer ", "");

        jwtTokenProvider.validateToken(reauthToken); // 예외 방식으로 수정

        Claims claims = jwtTokenProvider.getClaims(reauthToken);
        String username = claims.get("username", String.class);
        String email = claims.get("email", String.class);
        String phoneNumber = claims.get("phoneNumber", String.class);
        String phoneCode = claims.get("phoneCode", String.class);

        if (!reauthTokenService.isValid(email, reauthToken, username, email, phoneNumber, phoneCode)) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        PhoneVerification verification = getVerification(authType, phoneNumber, phoneCode);
        if (!verification.isVerified()) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findByEmailAndUsername(email, username)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (!request.getNewPassword().equals(request.getPasswordCheck())) {
            throw new GlobalException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        reauthTokenService.delete(email);
    }

    @Transactional
    public void setEmailVerificationService(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        user.setEmailVerified(true);
        userRepository.save(user);
    }
}
