package com.deepdirect.deepwebide_be.chat.util;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChannelInitializer {

    private final RepositoryRepository repositoryRepository;
    private final ChatChannelSubscriptionManager chatChannelSubscriptionManager;

    @PostConstruct
    public void subscribeSharedRepositories() {
        List<Repository> sharedRepos = repositoryRepository.findAllByIsSharedTrue();

        for (Repository repo : sharedRepos) {
            Long repositoryId = repo.getId();
            chatChannelSubscriptionManager.subscribe(repositoryId);
        }
    }
}