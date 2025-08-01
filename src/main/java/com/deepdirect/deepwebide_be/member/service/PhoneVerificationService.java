package com.deepdirect.deepwebide_be.member.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.AuthType;
import com.deepdirect.deepwebide_be.member.domain.PhoneVerification;
import com.deepdirect.deepwebide_be.member.repository.PhoneVerificationRepository;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final UserRepository userRepository;

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.api.number}")
    private String fromPhoneNumber;

    private final PhoneVerificationRepository phoneVerificationRepository;

    private DefaultMessageService defaultMessageService;

    @PostConstruct
    public void init() {
        this.defaultMessageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    // 랜덤 6자리 숫자 생성
    private String generateRandomNumber() {
        Random random = new Random();

        StringBuilder numString = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            numString.append(random.nextInt(10));
        }

        return numString.toString();
    }

    // 문자 발송

    public int sendVerificationCode(String phoneNumber, String reqUserName, AuthType reqAuthType) {
        // 휴대전화 번호 형식 정규식 (010으로 시작하고 숫자 총 10~11자리)
        Pattern phonePattern = Pattern.compile("^01[0|1|6|7|8|9][0-9]{7,8}$");
        if (!phonePattern.matcher(phoneNumber).matches()) {
            throw new GlobalException(ErrorCode.INVALID_PHONE_FORMAT);
        }

        if (reqAuthType == AuthType.SIGN_UP && isUserExists(phoneNumber, reqUserName)) {
            throw new GlobalException(ErrorCode.DUPLICATE_NAME_AND_PHONE);
        }

        String code = generateRandomNumber();

        Message message = new Message();
        message.setFrom(fromPhoneNumber);
        message.setTo(phoneNumber);
        message.setText("본인확인 인증번호 [" + code + "] 입니다.");

        try {
            defaultMessageService.send(message);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.SMS_SEND_FAILED);
        }

        phoneVerificationRepository.save(
                PhoneVerification.builder()
                        .phoneNumber(phoneNumber)
                        .authType(reqAuthType)
                        .phoneCode(code)
                        .expiresAt(LocalDateTime.now().plusMinutes(1))
                        .verified(false)
                        .build()
        );

        return 60;
    }

    // 사용자 검증
    public boolean isUserExists(String phoneNumber, String name) {
        return userRepository.findByUsernameAndPhoneNumber(name, phoneNumber).isPresent();
    }

    // 인증코드 검증 및 인증 여부 저장
    public boolean verifyCode(String phoneNumber, String code) {
        PhoneVerification verification = phoneVerificationRepository
                .findTopByPhoneNumberAndPhoneCodeOrderByCreatedAtDesc(phoneNumber, code)
                        .orElseThrow(() -> new GlobalException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (verification.isExpired()) {
            throw new GlobalException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        if (verification.isVerified()) {
            throw new GlobalException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        verification.verify();

        return true;
    }
}
