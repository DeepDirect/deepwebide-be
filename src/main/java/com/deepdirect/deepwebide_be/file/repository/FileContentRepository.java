package com.deepdirect.deepwebide_be.file.repository;

import com.deepdirect.deepwebide_be.file.domain.FileContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileContentRepository extends JpaRepository<FileContent, Long> {
    // 보통 fileNode로 조회, 필요에 따라 메서드 추가 가능
}
