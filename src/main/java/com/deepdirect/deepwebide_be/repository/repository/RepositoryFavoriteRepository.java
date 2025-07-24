package com.deepdirect.deepwebide_be.repository.repository;

import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryFavoriteRepository extends JpaRepository<RepositoryFavorite, Long> {

    // 조회
    Optional<RepositoryFavorite> findByUserAndRepository(User user, com.deepdirect.deepwebide_be.repository.domain.Repository repository);

    //삭제
    void deleteByUserAndRepository(User user, com.deepdirect.deepwebide_be.repository.domain.Repository repository);
}
