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


    /** 이름과 경로를 동시 변경 (이름 바꿀 때 사용) */
    public void rename(String newName) {
        this.name = newName;
        this.path = (parent == null) ? newName : parent.getPath() + "/" + newName;
    }

    /** 경로만 변경 (부모 폴더 이름/경로 바뀔 때 하위까지) */
    public void updatePath(String newPath) {
        this.path = newPath;
    }

    /** 폴더인지 체크 */
    public boolean isFolder() {
        return this.fileType == FileType.FOLDER;
    }

    public void moveToParent(FileNode newParent, String newParentPath) {
        this.parent = newParent;
        this.path = newParentPath.isEmpty() ? this.name : newParentPath + "/" + this.name;
    }
}
