package com.deepdirect.deepwebide_be.member.service;

import java.util.List;
import java.util.regex.Pattern;

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

        User user = userRepository.save(User.builder()
                .username(request.getUsername())
                .nickname(nickname)
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .profileImageUrl(profileImageUrl)
                .emailVerified(true) // TODO: 이메일 인증 추가
                .build());

        return SignUpResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @Transactional
    public SignInResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(ErrorCode.WRONG_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.WRONG_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createToken(user.getId());

        return new SignInResponse(accessToken, new SignInUserDto(user));
    }
}
