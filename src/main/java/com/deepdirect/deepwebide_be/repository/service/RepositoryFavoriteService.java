package com.deepdirect.deepwebide_be.repository.service;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryFavorite;
import com.deepdirect.deepwebide_be.repository.dto.response.FavoriteToggleResponse;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryFavoriteRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepositoryFavoriteService {
    private final RepositoryRepository repositoryRepository;
    private final RepositoryFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    @Transactional
    public FavoriteToggleResponse toggleFavorite(Long repositoryId, Long userId) {
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        Optional<RepositoryFavorite> favorite =
                favoriteRepository.findByUserAndRepository(user, repository);

        if (favorite.isPresent()) {
            favoriteRepository.deleteByUserAndRepository(user, repository);
            return FavoriteToggleResponse.builder()
                    .isFavorite(false)
                    .message("레포지토리가 즐겨찾기에 등록 취소되었습니다.")
                    .build();
        }

        RepositoryFavorite newFavorite = RepositoryFavorite.builder()
                .user(user)
                .repository(repository)
                .build();
        favoriteRepository.save(newFavorite);

        return FavoriteToggleResponse.builder()
                .isFavorite(true)
                .message("레포지토리가 즐겨찾기에 등록되었습니다.")
                .build();
    }


}
