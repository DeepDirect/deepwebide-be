package com.deepdirect.deepwebide_be.repository.service;

import com.deepdirect.deepwebide_be.file.domain.FileContent;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import com.deepdirect.deepwebide_be.file.repository.FileContentRepository;
import com.deepdirect.deepwebide_be.file.repository.FileNodeRepository;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.repository.domain.PortRegistry;
import com.deepdirect.deepwebide_be.repository.domain.PortStatus;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryType;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryExecuteResponse;
import com.deepdirect.deepwebide_be.repository.repository.PortRegistryRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import com.deepdirect.deepwebide_be.sandbox.dto.request.SandboxExecutionRequest;
import com.deepdirect.deepwebide_be.sandbox.dto.response.SandboxExecutionResponse;
import com.deepdirect.deepwebide_be.sandbox.service.S3Service;
import com.deepdirect.deepwebide_be.sandbox.service.SandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockMultipartFile;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryRunService {

    private final RepositoryRepository repositoryRepository;
    private final S3Service s3Service;
    private final SandboxService sandboxService;
    private final PortRegistryRepository portRegistryRepository;
    private final FileNodeRepository fileNodeRepository;
    private final FileContentRepository fileContentRepository;

    @Transactional
    public RepositoryExecuteResponse executeRepository(Long repositoryId, Long userId) {
        log.info("Starting repository execution - repositoryId: {}, userId: {}", repositoryId, userId);

        File zipFile = null;
        try {
            // 1. 권한 체크 & 레포지토리 조회
            Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

            // 2. framework, port 등 정보 추출
            String framework = convertTypeToFramework(repo.getRepositoryType());
            Integer port = allocateOrGetPort(repo);

            // 3. 파일트리를 zip으로 변환
            String uuid = UUID.randomUUID().toString();
            zipFile = fileTreeToZip(repositoryId, uuid);

            // 4. S3 업로드
            String s3Url = uploadToS3(zipFile, uuid);

            // 5. 샌드박스 실행 요청
            SandboxExecutionRequest request = SandboxExecutionRequest.builder()
                    .uuid(uuid)
                    .url(s3Url)
                    .framework(framework)
                    .port(port)
                    .build();

            SandboxExecutionResponse result = sandboxService.requestExecution(request);

            log.info("Repository execution completed - uuid: {}, port: {}", uuid, port);

            return RepositoryExecuteResponse.builder()
                    .uuid(uuid)
                    .s3Url(s3Url)
                    .port(port)
                    .message(result.getMessage())
                    .executionId(result.getExecutionId())
                    .status(result.getStatus())
                    .output(result.getOutput())
                    .error(result.getError())
                    .executionTime(result.getExecutionTime())
                    .build();

        } catch (GlobalException e) {
            log.error("Repository execution failed - repositoryId: {}, userId: {}", repositoryId, userId, e);
            throw e;
        } catch (Exception e) {
            log.error("Repository execution failed - repositoryId: {}, userId: {}", repositoryId, userId, e);
            throw new GlobalException(ErrorCode.REPOSITORY_EXECUTION_FAILED);
        } finally {
            // 임시 zip 파일 정리
            cleanupTempFile(zipFile);
        }
    }

    private String convertTypeToFramework(RepositoryType type) {
        return switch (type) {
            case SPRING_BOOT -> "spring";
            case REACT -> "react";
            case FAST_API -> "fastapi";
            default -> throw new GlobalException(ErrorCode.UNSUPPORTED_REPOSITORY_TYPE);
        };
    }

    private Integer allocateOrGetPort(Repository repo) {
        return portRegistryRepository.findByRepository(repo)
                .map(PortRegistry::getPort)
                .orElseGet(() -> allocateNewPortForRepository(repo).getPort());
    }

    @Transactional
    public PortRegistry allocateNewPortForRepository(Repository repo) {
        log.debug("Allocating new port for repository: {}", repo.getId());

        // 1. 사용 가능한 포트 목록 조회 (status == AVAILABLE)
        PortRegistry available = portRegistryRepository.findFirstByStatus(PortStatus.AVAILABLE)
                .orElseThrow(() -> new GlobalException(ErrorCode.NO_AVAILABLE_PORT));

        // 2. 해당 포트 할당/저장
        available.assignToRepository(repo);
        PortRegistry savedRegistry = portRegistryRepository.save(available);

        log.info("Port {} allocated to repository {}", available.getPort(), repo.getId());
        return savedRegistry;
    }

    private String uploadToS3(File zipFile, String uuid) throws IOException {
        log.debug("Uploading zip file to S3 - uuid: {}", uuid);

        try (FileInputStream fis = new FileInputStream(zipFile)) {
            String s3Url = s3Service.upload(
                    new MockMultipartFile("file", zipFile.getName(), "application/zip", fis),
                    uuid
            );
            log.debug("S3 upload completed - url: {}", s3Url);
            return s3Url;
        }
    }

    public File fileTreeToZip(Long repositoryId, String uuid) {
        Path tempDir = null;
        Path zipPath = null;

        try {
            log.debug("Converting file tree to zip - repositoryId: {}, uuid: {}", repositoryId, uuid);

            // 1. 임시 디렉토리 생성
            tempDir = Files.createTempDirectory("repo-" + uuid);

            // 2. 파일트리/내용 복원
            List<FileNode> nodes = fileNodeRepository.findAllByRepositoryId(repositoryId);
            if (nodes.isEmpty()) {
                log.warn("No files found for repository: {}", repositoryId);
                throw new GlobalException(ErrorCode.REPOSITORY_FILES_NOT_FOUND);
            }

            restoreFileTree(nodes, tempDir);

            // 3. zip 압축
            zipPath = createZipFromDirectory(tempDir);

            log.debug("File tree conversion completed - zipPath: {}", zipPath);
            return zipPath.toFile();

        } catch (Exception e) {
            log.error("Failed to convert file tree to zip - repositoryId: {}", repositoryId, e);
            // 실패 시 생성된 임시 파일들 정리
            cleanupTempResources(tempDir, zipPath);
            throw new GlobalException(ErrorCode.FILE_TREE_CONVERSION_FAILED);
        } finally {
            // 임시 디렉토리 정리 (zip 파일은 유지)
            cleanupTempDirectory(tempDir);
        }
    }

    private void restoreFileTree(List<FileNode> nodes, Path tempDir) throws IOException {
        log.debug("Restoring file tree - nodes count: {}", nodes.size());

        // 폴더 먼저 생성
        nodes.stream()
                .filter(FileNode::isFolder)
                .forEach(node -> {
                    try {
                        Path nodePath = tempDir.resolve(node.getPath());
                        Files.createDirectories(nodePath);
                        log.trace("Created directory: {}", nodePath);
                    } catch (IOException e) {
                        throw new UncheckedIOException("Failed to create directory: " + node.getPath(), e);
                    }
                });

        // 파일 생성
        for (FileNode node : nodes) {
            if (!node.isFolder()) {
                Path nodePath = tempDir.resolve(node.getPath());

                // 부모 디렉토리 먼저 생성
                if (nodePath.getParent() != null) {
                    Files.createDirectories(nodePath.getParent());
                }

                // 파일 내용 작성
                FileContent content = fileContentRepository.findByFileNode(node)
                        .orElseThrow(() -> new GlobalException(ErrorCode.FILE_CONTENT_NOT_FOUND));

                Files.write(nodePath, content.getContent());
                log.trace("Created file: {} (size: {} bytes)", nodePath, content.getContent().length);
            }
        }
    }

    private Path createZipFromDirectory(Path tempDir) throws IOException {
        Path zipPath = Paths.get(tempDir.toString() + ".zip");
        log.debug("Creating zip file: {}", zipPath);

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> addToZip(zos, tempDir, path));
        }

        long zipSize = Files.size(zipPath);
        log.debug("Zip file created successfully - size: {} bytes", zipSize);

        return zipPath;
    }

    private void addToZip(ZipOutputStream zos, Path baseDir, Path filePath) {
        try {
            String entryName = baseDir.relativize(filePath).toString().replace('\\', '/');
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(Files.readAllBytes(filePath));
            zos.closeEntry();
            log.trace("Added to zip: {}", entryName);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to add file to zip: " + filePath, e);
        }
    }

    private void cleanupTempFile(File file) {
        if (file != null && file.exists()) {
            try {
                Files.deleteIfExists(file.toPath());
                log.debug("Cleaned up temp file: {}", file.getPath());
            } catch (IOException e) {
                log.warn("Failed to cleanup temp file: {}", file.getPath(), e);
            }
        }
    }

    private void cleanupTempDirectory(Path tempDir) {
        if (tempDir != null && Files.exists(tempDir)) {
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
                log.debug("Cleaned up temp directory: {}", tempDir);
            } catch (IOException e) {
                log.warn("Failed to cleanup temp directory: {}", tempDir, e);
            }
        }
    }

    private void cleanupTempResources(Path tempDir, Path zipPath) {
        cleanupTempDirectory(tempDir);
        if (zipPath != null) {
            try {
                Files.deleteIfExists(zipPath);
                log.debug("Cleaned up temp zip file: {}", zipPath);
            } catch (IOException e) {
                log.warn("Failed to cleanup zip file: {}", zipPath, e);
            }
        }
    }
}
