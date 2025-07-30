package com.deepdirect.deepwebide_be.chat.util;

import org.springframework.data.redis.listener.ChannelTopic;

public class ChatChannelManager {
    private static final String PREFIX = "chat:";

    public static String getChannelName(Long repositoryId) {
        return PREFIX + repositoryId;
    }

    public static ChannelTopic getTopic(Long repositoryId) {
        return new ChannelTopic(getChannelName(repositoryId));
    }
}