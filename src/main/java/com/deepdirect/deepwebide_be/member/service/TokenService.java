package com.deepdirect.deepwebide_be.member.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.global.security.JwtTokenProvider;
import com.deepdirect.deepwebide_be.global.security.RefreshToken;
import com.deepdirect.deepwebide_be.global.security.RefreshTokenService;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public String reissueAccessToken(String refreshToken) {
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GlobalException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 토큰에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 리프레시 토큰 확인
        String saveToken = refreshTokenService.findByUserId(userId);
        if (saveToken == null || !saveToken.equals(refreshToken)) {
            throw new GlobalException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 사용자 조회
        User user = userRepository.findById(userId).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        // Access Token 발급
        return jwtTokenProvider.createToken(user.getId());
    }
}
