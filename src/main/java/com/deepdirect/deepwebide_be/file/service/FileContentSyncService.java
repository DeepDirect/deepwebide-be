package com.deepdirect.deepwebide_be.file.service;

import com.deepdirect.deepwebide_be.file.domain.FileContent;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import com.deepdirect.deepwebide_be.file.domain.FileType;
import com.deepdirect.deepwebide_be.file.repository.FileContentRepository;
import com.deepdirect.deepwebide_be.file.repository.FileNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class FileContentSyncService {
    private final String DIRTY_SET_KEY = "dirty:files";
    private final RedisTemplate<String, String> redisTemplate;
    private final FileContentRepository fileContentRepository;
    private final FileNodeRepository fileNodeRepository;

    @Scheduled(fixedRate = 30000) // 30초마다 실행
    public void syncRedisToDb() {
        Set<String> dirtyFileKeys = redisTemplate.opsForSet().members(DIRTY_SET_KEY);
        if (dirtyFileKeys == null) return;

        for (String fileKey : dirtyFileKeys) {
            // file:{repositoryId}:{fileId} → 파싱
            String[] parts = fileKey.split(":");
            if (parts.length != 3) continue;
            Long repositoryId = Long.valueOf(parts[1]);
            Long fileId = Long.valueOf(parts[2]);
            String content = redisTemplate.opsForValue().get(fileKey);

            // DB 저장
            FileNode fileNode = fileNodeRepository.findById(fileId).orElse(null);
            if (fileNode == null || fileNode.getFileType() != FileType.FILE) {
                // 파일이 없거나 폴더이면 패스
                redisTemplate.opsForSet().remove(DIRTY_SET_KEY, fileKey);
                continue;
            }
            FileContent fileContent = fileContentRepository.findByFileNode(fileNode)
                    .orElseThrow(() -> new RuntimeException("FileContent not found: " + fileId));
            fileContent.updateContent(content.getBytes(StandardCharsets.UTF_8));
            fileContentRepository.save(fileContent);

            // dirty set에서 제거
            redisTemplate.opsForSet().remove(DIRTY_SET_KEY, fileKey);
        }
    }
}
