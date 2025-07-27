package com.deepdirect.deepwebide_be.file.domain;

import com.deepdirect.deepwebide_be.repository.domain.Repository;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "file_nodes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FileNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private Repository repository;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType; // FILE, FOLDER

    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FileNode parent;
}
