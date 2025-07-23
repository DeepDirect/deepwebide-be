package com.deepdirect.deepwebide_be.member.repository;

import com.deepdirect.deepwebide_be.member.domain.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    // 검증용
    Optional<PhoneVerification> findTopByPhoneNumberAndPhoneCodeOrderByCreatedAtDesc(String phoneNumber, String phoneCode);
}
