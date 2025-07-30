package com.deepdirect.deepwebide_be.chat.service;

import com.deepdirect.deepwebide_be.chat.dto.response.ChatSystemMessageResponse;
import com.deepdirect.deepwebide_be.chat.util.ChatChannelSubscriptionManager;
import com.deepdirect.deepwebide_be.chat.util.RedisPublisher;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisPublisher redisPublisher;
    private final UserRepository userRepository;
    private final ChatChannelSubscriptionManager chatChannelSubscriptionManager;

    private static final String SESSION_KEY_PREFIX = "chat_session:";

    public void addSession(Long repositoryId, Long userId) {
        String key = SESSION_KEY_PREFIX + repositoryId;
        redisTemplate.opsForSet().add(key, userId.toString());
        long connectedCount = redisTemplate.opsForSet().size(key);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        chatChannelSubscriptionManager.subscribe(repositoryId);

        ChatSystemMessageResponse message = ChatSystemMessageResponse.enter(
                repositoryId,
                userId,
                user.getNickname(),
                user.getProfileImageUrl(),
                connectedCount
        );

        redisPublisher.publish(chatChannelSubscriptionManager.getTopic(repositoryId).getTopic(), message);
    }

    public void removeSession(Long repositoryId, Long userId) {
        String key = SESSION_KEY_PREFIX + repositoryId;
        redisTemplate.opsForSet().remove(key, userId.toString());

        long connectedCount = redisTemplate.opsForSet().size(key);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        ChatSystemMessageResponse message = ChatSystemMessageResponse.exit(
                repositoryId,
                userId,
                user.getNickname(),
                connectedCount
        );

        redisPublisher.publish(chatChannelSubscriptionManager.getChannelName(repositoryId), message);
    }

    public long getConnectedCount(Long repositoryId) {
        String key = SESSION_KEY_PREFIX + repositoryId;
        return redisTemplate.opsForSet().size(key);
    }
}