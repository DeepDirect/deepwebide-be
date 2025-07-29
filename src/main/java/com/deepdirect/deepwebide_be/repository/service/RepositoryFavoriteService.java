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

@Service
@RequiredArgsConstructor
public class RepositoryFavoriteService {
    private final RepositoryRepository repositoryRepository;
    private final RepositoryFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    @Transactional
    public FavoriteToggleResponse toggleFavorite(Long repositoryId, Long userId) {

        if (repositoryRepository.findByIdAndDeletedAtIsNull(repositoryId).isEmpty())
        {
            throw  new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND);
        }

        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));
        User user = getUserOrThrow(userId);

        return favoriteRepository.findByUserAndRepository(user, repository)
                .map(existing -> removeFavorite(existing, user, repository))
                .orElseGet(() -> addFavorite(user, repository));
    }

    private FavoriteToggleResponse removeFavorite(RepositoryFavorite existing, User user, Repository repository) {
        favoriteRepository.deleteByUserAndRepository(user, repository);
        return buildResponse(false, "레포지토리가 즐겨찾기에서 취소되었습니다.");
    }

    private FavoriteToggleResponse addFavorite(User user, Repository repository) {
        RepositoryFavorite favorite = RepositoryFavorite.builder()
                .user(user)
                .repository(repository)
                .build();
        favoriteRepository.save(favorite);
        return buildResponse(true, "레포지토리가 즐겨찾기에 등록되었습니다.");
    }

    private FavoriteToggleResponse buildResponse(boolean isFavorite, String message) {
        return FavoriteToggleResponse.builder()
                .isFavorite(isFavorite)
                .message(message)
                .build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }


}
