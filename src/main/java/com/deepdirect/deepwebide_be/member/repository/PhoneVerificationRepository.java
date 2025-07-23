package com.deepdirect.deepwebide_be.member.repository;

import com.deepdirect.deepwebide_be.member.domain.AuthType;
import com.deepdirect.deepwebide_be.member.domain.PhoneVerification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    // 검증용
    Optional<PhoneVerification> findTopByPhoneNumberAndPhoneCodeOrderByCreatedAtDesc(String phoneNumber, String phoneCode);

    Optional<PhoneVerification> findByPhoneNumberAndPhoneCodeAndAuthType(String phoneNumber, String phoneCode, AuthType authType);
}
