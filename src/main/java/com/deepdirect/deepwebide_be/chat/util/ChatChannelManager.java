package com.deepdirect.deepwebide_be.chat.util;

import org.springframework.data.redis.listener.ChannelTopic;

/**
 * Redis 채널명 및 Topic 생성을 담당하는 유틸리티 클래스.
 */
public class ChatChannelManager {
    private static final String PREFIX = "chat:";

    public static String getChannelName(Long repositoryId) {
        return PREFIX + repositoryId;
    }

    public static ChannelTopic getTopic(Long repositoryId) {
        return new ChannelTopic(getChannelName(repositoryId));
    }
}