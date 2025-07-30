package com.deepdirect.deepwebide_be.chat.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatChannelSubscriptionManager {

    private final RedisMessageListenerContainer container;
    private final RedisSubscriber redisSubscriber;

    private final Set<Long> subscribedRepositoryIds = ConcurrentHashMap.newKeySet();

    public void subscribe(Long repositoryId) {
        if (subscribedRepositoryIds.add(repositoryId)) { // 중복 구독 방지
            container.addMessageListener(redisSubscriber, getTopic(repositoryId));
        }
    }

    public void unsubscribe(Long repositoryId) {
        if (subscribedRepositoryIds.remove(repositoryId)) { // 구독되어 있던 경우만
            container.removeMessageListener(redisSubscriber, getTopic(repositoryId));
        }
    }


    public ChannelTopic getTopic(Long repositoryId) {
        return ChatChannelManager.getTopic(repositoryId);
    }

    public String getChannelName(Long repositoryId) {
        return ChatChannelManager.getChannelName(repositoryId);
    }

}