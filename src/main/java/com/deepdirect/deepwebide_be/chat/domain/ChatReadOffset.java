package com.deepdirect.deepwebide_be.chat.domain;

import com.deepdirect.deepwebide_be.member.domain.User;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_read_offsets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReadOffset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id", nullable = false)
    private ChatMessage lastReadMessage;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }


    @Builder
    private ChatReadOffset(Repository repository, User user, ChatMessage lastReadMessage) {
        this.repository = repository;
        this.user = user;
        this.lastReadMessage = lastReadMessage;
        this.updatedAt = LocalDateTime.now();
    }

    public static ChatReadOffset of(Repository repository, User user, ChatMessage message) {
        return ChatReadOffset.builder()
                .repository(repository)
                .user(user)
                .lastReadMessage(message)
                .build();
    }

    public void update(ChatMessage newMessage) {
        this.lastReadMessage = newMessage;
        this.updatedAt = LocalDateTime.now();
    }
}
