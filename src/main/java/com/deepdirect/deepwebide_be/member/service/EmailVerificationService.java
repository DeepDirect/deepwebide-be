package com.deepdirect.deepwebide_be.member.service;

import com.deepdirect.deepwebide_be.member.domain.EmailVerification;
import com.deepdirect.deepwebide_be.member.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    // 인증 코드 생성 및 저장
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
        String link = "http://localhost:8080/api/members/verify-email?code=" + code;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증 요청");
        message.setText("아래 링크를 클릭하여 이메일 인증을 완료해주세요:\n" + link);

        javaMailSender.send(message);
    }

    // 이메일 인증 코드 검증
    public boolean verifyEmailCode(String code) {
        System.out.println(code);
        return emailVerificationRepository.findByEmailCode(code)
                .filter(verification -> !verification.isVerified()) // 기존 인증 여부 확인
                .filter(verification ->
                        verification.getExpiresAt().isAfter(LocalDateTime.now())) // 만료 여부 확인
                .map(verification -> {
                    verification.setVerified(true);
                    emailVerificationRepository.save(verification); // 검증 여부 true로 변경

                    return true;
                })
                .orElse(false);
    }
}
