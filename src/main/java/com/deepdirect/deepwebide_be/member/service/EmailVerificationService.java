package com.deepdirect.deepwebide_be.member.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.EmailVerification;
import com.deepdirect.deepwebide_be.member.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    // 인증 코드 생성 및 저장
    @Transactional
    public String createVerification(String email) {
        // 코드 생성
        String code = UUID.randomUUID().toString();

        // 인증코드 만료
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .emailCode(code)
                .expiresAt(expiresAt)
                .build();

        emailVerificationRepository.save(verification);

        return code;
    }

    // 이메일 인증 요청 메일 발송
    public void sendVerificationEmail(String email, String code) {
//        String link = "http://localhost:8080/api/auth/email/send-code?code=" + code;
        String link = "https://api.deepdirect.site/api/auth/email/send-code?code=" + code;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증 요청");
        message.setText("아래 링크를 클릭하여 이메일 인증을 완료해주세요:\n" + link);

        javaMailSender.send(message);
    }

    // 이메일 인증 코드 검증
    public boolean verifyEmailCode(String code) {
        return emailVerificationRepository.findByEmailCode(code)
                .filter(verification ->
                        verification.getExpiresAt().isAfter(LocalDateTime.now())) // 만료 여부 확인
                .map(verification -> {
                    verification.setVerified(true);
                    emailVerificationRepository.save(verification); // 검증 여부 true로 변경

                    return true;
                })
                .orElse(false);
    }

    public String findVerifiedEmailByCode(String code) {
        return emailVerificationRepository.findByEmailCode(code)
                .map(EmailVerification::getEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 코드로 등록된 이메일이 없습니다."));
    }

    @Transactional
    public void handleEmailVerification(String email) {
        Optional<EmailVerification> latestVerification = emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email);

        if (latestVerification.isPresent()) {
            EmailVerification verification = latestVerification.get();

            // 인증 코드가 아직 유효한 경우
            if (verification.getExpiresAt().isAfter(LocalDateTime.now())) {
                throw new GlobalException(ErrorCode.EMAIL_NOT_VERIFIED);
            }
        }

        // 새로운 인증 코드 발송
        String newCode = createVerification(email);
        sendVerificationEmail(email, newCode);

        throw  new GlobalException(ErrorCode.EMAIL_NOT_VERIFIED_CODE_RESENT);
    }
}
