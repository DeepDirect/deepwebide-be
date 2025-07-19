package com.deepdirect.deepwebide_be.member.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.dto.request.SignUpRequest;
import com.deepdirect.deepwebide_be.member.dto.response.SignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.deepdirect.deepwebide_be.member.util.NicknameGenerator;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;
import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%]).{8,}$";
    private static final Pattern PASSWORD_REGEX = Pattern.compile(PASSWORD_PATTERN);
    private static final String NAME_PATTERN = "^[가-힣]{2,20}$";
    private static final Pattern NAME_REGEX = Pattern.compile(NAME_PATTERN);
    private final PasswordEncoder PasswordEncoder;

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

        String encodedPassword = PasswordEncoder.encode(request.getPassword());
        String nickname = generateUniqueNickname(NicknameGenerator.generate());

        User user = userRepository.save(User.builder()
                .username(request.getUsername())
                .nickname(nickname)
                .email(request.getEmail())
                .password(encodedPassword)
                .profileImageUrl("") // TODO: 이미지 url 생성 추가
                .emailVerified(true) // TODO: 이메일 인증 추가
                .build());

        return SignUpResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

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
}
