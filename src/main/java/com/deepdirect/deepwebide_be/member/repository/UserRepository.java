package com.deepdirect.deepwebide_be.member.repository;

import com.deepdirect.deepwebide_be.member.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);

    // 닉네임 중복
    @Query("SELECT u.nickname FROM User u WHERE u.nickname LIKE CONCAT(:base, '%')")
    List<String> findNicknamesByPrefix(@Param("base") String baseNickname);
}