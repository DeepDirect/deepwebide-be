package com.deepdirect.deepwebide_be.repository.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "repository_entry_codes")
public class RepositoryEntryCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_code", nullable = false, length = 10)
    private String entryCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false, unique = true)
    private Repository repository;

    @PrePersist
    public void prePersist() {
        this.createdAt = this.createdAt == null ? LocalDateTime.now() : this.createdAt;
    }

    @Builder
    public RepositoryEntryCode(String entryCode, LocalDateTime expiresAt, Repository repository) {
        this.entryCode = entryCode;
        this.expiresAt = expiresAt;
        this.repository = repository;
    }

    public void updateEntryCode(String newCode, LocalDateTime newExpiresAt) {
        this.entryCode = newCode;
        this.expiresAt = newExpiresAt;
    }


}

