package com.deepdirect.deepwebide_be.history.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "history_file")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HistoryFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 히스토리(버전)에 속한 파일인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private History history;

    @Column(nullable = false)
    private Long fileId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType; // "FILE" or "FOLDER"

    private Long parentId;

    @Column(nullable = false)
    private String path;

    @Lob
    private String content; // 파일일 때만 값이 있음, 폴더는 null

}
