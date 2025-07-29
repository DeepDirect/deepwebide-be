package com.deepdirect.deepwebide_be.member.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_oa_provider_user", columnNames = {"provider", "provider_user_id"})
        },
        indexes = {
                @Index(name = "idx_oa_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OauthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소셜 로그인 제공자 (github, google 등)
    @Column(name = "provider", nullable = false)
    private String provider;

    // provider가 부여한 고유 사용자 ID
    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    // 생성 시 자동 기록
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_oa_user"))
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}