package com.deepdirect.deepwebide_be.repository.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "running_containers")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunningContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long repositoryId;

    @Column(nullable = false)
    private String uuid;

    @Column(nullable = false)
    private String containerName;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String framework;

    @Column
    private String s3Url;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime stoppedAt;

    // PrePersist로 createdAt 자동 설정
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // 상태 업데이트 메서드
    public void stop() {
        this.status = "STOPPED";
        this.stoppedAt = LocalDateTime.now();
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    // Builder 패턴을 위한 커스텀 빌더 클래스
    public static class RunningContainerBuilder {
        private LocalDateTime createdAt = LocalDateTime.now(); // 기본값 설정

        public RunningContainerBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
    }
}