package com.deepdirect.deepwebide_be.history.domain;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "history")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 저장된 레포지토리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    // 저장 메시지 (예: "1차 개발 완료")
    @Column(nullable = false)
    private String message;

    // 저장자 (member id)
    @Column(nullable = false)
    private Long authorId;

    // 생성 일시
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 연관된 파일 목록
    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL)
    private List<HistoryFile> files;

}
