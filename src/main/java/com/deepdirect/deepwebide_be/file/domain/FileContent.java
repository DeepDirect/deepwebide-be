package com.deepdirect.deepwebide_be.file.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "file_contents")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FileContent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileNode fileNode;

    @Lob
    @Column(nullable = false)
    private byte[] content;
}
