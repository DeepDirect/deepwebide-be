package com.deepdirect.deepwebide_be.repository.service;

import com.deepdirect.deepwebide_be.file.domain.FileContent;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import com.deepdirect.deepwebide_be.file.repository.FileContentRepository;
import com.deepdirect.deepwebide_be.file.repository.FileNodeRepository;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.repository.domain.*;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryExecuteResponse;
import com.deepdirect.deepwebide_be.repository.dto.response.RepositoryStatusResponse;
import com.deepdirect.deepwebide_be.repository.repository.PortRegistryRepository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import com.deepdirect.deepwebide_be.repository.repository.RunningContainerRepository;
import com.deepdirect.deepwebide_be.sandbox.dto.request.SandboxExecutionRequest;
import com.deepdirect.deepwebide_be.sandbox.dto.response.SandboxExecutionResponse;
import com.deepdirect.deepwebide_be.sandbox.service.S3Service;
import com.deepdirect.deepwebide_be.sandbox.service.SandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private final RunningContainerRepository runningContainerRepository;
    private final String sandboxBaseUrl = "http://localhost:9090";
    private final RestTemplate restTemplate;

    @Transactional
    public RepositoryExecuteResponse executeRepository(Long repositoryId, Long userId) {
        log.info("Starting repository execution - repositoryId: {}, userId: {}", repositoryId, userId);

        File zipFile = null;
        try {
            // 1. 기존 실행 중인 컨테이너 중지 요청
            stopExistingContainer(repositoryId);

            // 2. 권한 체크 & 레포지토리 조회
            Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

            // 3. framework, port 등 정보 추출
            String framework = convertTypeToFramework(repo.getRepositoryType());
            Integer port = allocateOrGetPort(repo);

            // 4. 새로운 UUID 생성
            String uuid = UUID.randomUUID().toString();

            // 5. 파일트리를 zip으로 변환
            zipFile = fileTreeToZip(repositoryId, uuid);

            // 6. S3 업로드
            String s3Url = uploadToS3(zipFile, uuid);

            // 7. 샌드박스 실행 요청
            SandboxExecutionRequest request = SandboxExecutionRequest.builder()
                    .uuid(uuid)
                    .url(s3Url)
                    .framework(framework)
                    .port(port)
                    .build();

            SandboxExecutionResponse result = sandboxService.requestExecution(request);

            // 8. 실행 중인 컨테이너 정보 저장
            saveRunningContainer(repositoryId, uuid, "sandbox-" + uuid, port, framework, s3Url);

            log.info("Repository execution completed - repositoryId: {}, uuid: {}, port: {}", repositoryId, uuid, port);

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
            cleanupTempFile(zipFile);
        }
    }

    /**
     * 기존 실행 중인 컨테이너 중지
     */
    private void stopExistingContainer(Long repositoryId) {
        runningContainerRepository.findByRepositoryId(repositoryId)
                .ifPresent(container -> {
                    try {
                        log.info("Stopping existing container for repository {}: {}", repositoryId, container.getUuid());

                        // 샌드박스 서버에 중지 요청
                        boolean success = sandboxService.stopContainer(container.getUuid());

                        if (success) {
                            // 상태 업데이트
                            container.stop();
                            runningContainerRepository.save(container);

                            log.info("Successfully stopped existing container: {}", container.getUuid());
                        } else {
                            log.warn("Failed to stop existing container: {}", container.getUuid());
                        }

                        // 성공 여부와 관계없이 DB에서 제거 (새로운 컨테이너가 실행될 예정이므로)
                        runningContainerRepository.deleteByRepositoryId(repositoryId);

                    } catch (Exception e) {
                        log.error("Error while stopping existing container: {}", container.getUuid(), e);
                        // 에러가 발생해도 DB에서는 제거
                        runningContainerRepository.deleteByRepositoryId(repositoryId);
                    }
                });
    }

    /**
     * 실행 중인 컨테이너 정보 저장
     */
    @Transactional
    public void saveRunningContainer(Long repositoryId, String uuid, String containerName,
                                     Integer port, String framework, String s3Url) {
        try {
            RunningContainer container = RunningContainer.builder()
                    .repositoryId(repositoryId)
                    .uuid(uuid)
                    .containerName(containerName)
                    .port(port)
                    .status("RUNNING")
                    .framework(framework)
                    .s3Url(s3Url)
                    .build();

            runningContainerRepository.save(container);
            log.info("Saved running container info - repositoryId: {}, uuid: {}, port: {}",
                    repositoryId, uuid, port);

        } catch (Exception e) {
            log.error("Failed to save running container info - repositoryId: {}, uuid: {}",
                    repositoryId, uuid, e);
        }
    }

    /**
     * 레포지토리 중지 (수동)
     */
    @Transactional
    public boolean stopRepository(Long repositoryId, Long userId) {
        try {
            // 권한 체크
            repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

            Optional<RunningContainer> containerOpt = runningContainerRepository.findByRepositoryId(repositoryId);

            if (containerOpt.isEmpty()) {
                log.info("No running container found for repository: {}", repositoryId);
                return false;
            }

            RunningContainer container = containerOpt.get();

            // 샌드박스 서버에 중지 요청
            boolean success = sandboxService.stopContainer(container.getUuid());

            if (success) {
                container.stop();
                runningContainerRepository.save(container);
                log.info("Successfully stopped repository: {} (uuid: {})", repositoryId, container.getUuid());
            }

            return success;

        } catch (Exception e) {
            log.error("Failed to stop repository: {}", repositoryId, e);
            return false;
        }
    }

    /**
     * 레포지토리 상태 조회
     */
    public RepositoryStatusResponse getRepositoryStatus(Long repositoryId, Long userId) {
        try {
            repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

            Optional<RunningContainer> containerOpt = runningContainerRepository.findByRepositoryId(repositoryId);

            if (containerOpt.isEmpty()) {
                return RepositoryStatusResponse.builder()
                        .repositoryId(repositoryId)
                        .dbStatus("NOT_RUNNING")
                        .sandboxStatus(Map.of("message", "No running container found"))
                        .build();
            }

            RunningContainer container = containerOpt.get();
            Map<String, Object> sandboxStatus = sandboxService.getContainerStatus(container.getUuid());

            return RepositoryStatusResponse.builder()
                    .repositoryId(repositoryId)
                    .uuid(container.getUuid())
                    .containerName(container.getContainerName())
                    .port(container.getPort())
                    .framework(container.getFramework())
                    .createdAt(container.getCreatedAt())
                    .dbStatus(container.getStatus())
                    .sandboxStatus(sandboxStatus)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get repository status: {}", repositoryId, e);
            return RepositoryStatusResponse.builder()
                    .repositoryId(repositoryId)
                    .dbStatus("ERROR")
                    .sandboxStatus(Map.of("error", e.getMessage()))
                    .build();
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

    /**
     * 레포지토리 로그 조회
     */
    /**
     * 레포지토리 로그 조회 (상태 자동 동기화)
     */
    public Map<String, Object> getRepositoryLogs(Long repositoryId, Long userId, int lines, String since) {
        try {
            log.info("Getting repository logs - repositoryId: {}, userId: {}", repositoryId, userId);

            repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

            Optional<RunningContainer> containerOpt = runningContainerRepository.findByRepositoryId(repositoryId);

            if (containerOpt.isEmpty()) {
                return createNoContainerResponse(repositoryId);
            }

            RunningContainer container = containerOpt.get();
            log.info("Found container - uuid: {}, dbStatus: {}", container.getUuid(), container.getStatus());

            String url = String.format("%s/api/sandbox/logs/%s?lines=%d&since=%s",
                    sandboxBaseUrl, container.getUuid(), lines, since);

            try {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response != null) {
                    String status = (String) response.get("status");

                    // 컨테이너를 찾을 수 없는 경우 DB 상태 업데이트
                    if ("CONTAINER_NOT_FOUND".equals(status)) {
                        log.warn("Container {} not found, updating DB status", container.getUuid());

                        // DB 상태 자동 동기화
                        container.stop();
                        runningContainerRepository.save(container);

                        Map<String, Object> result = new HashMap<>(response);
                        result.put("repositoryId", repositoryId);
                        result.put("dbStatusUpdated", true);
                        result.put("message", "컨테이너가 존재하지 않아 DB 상태를 업데이트했습니다.");
                        result.put("containerInfo", createContainerInfo(container, "STOPPED"));

                        return result;
                    }

                    // 정상 응답 처리
                    Map<String, Object> result = new HashMap<>(response);
                    result.put("repositoryId", repositoryId);
                    result.put("requestedUrl", url);
                    result.put("containerInfo", createContainerInfo(container, container.getStatus()));

                    return result;
                }

                return createErrorResponse(repositoryId, "Empty response from sandbox server");

            } catch (Exception httpEx) {
                log.error("HTTP request failed - url: {}", url, httpEx);

                // HTTP 오류 시에도 컨테이너 상태 확인
                if (httpEx.getMessage().contains("404") || httpEx.getMessage().contains("Not Found")) {
                    container.stop();
                    runningContainerRepository.save(container);
                }

                return createErrorResponse(repositoryId, httpEx.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to get repository logs: {}", repositoryId, e);
            return createErrorResponse(repositoryId, e.getMessage());
        }
    }

    private Map<String, Object> createContainerInfo(RunningContainer container, String actualStatus) {
        return Map.of(
                "uuid", container.getUuid(),
                "containerName", container.getContainerName(),
                "port", container.getPort(),
                "framework", container.getFramework(),
                "createdAt", container.getCreatedAt(),
                "dbStatus", actualStatus
        );
    }

    private Map<String, Object> createNoContainerResponse(Long repositoryId) {
        return Map.of(
                "repositoryId", repositoryId,
                "status", "NO_CONTAINER",
                "message", "실행 중인 컨테이너 정보가 없습니다.",
                "stdout", "",
                "stderr", "",
                "logs", ""
        );
    }

    private Map<String, Object> createErrorResponse(Long repositoryId, String errorMessage) {
        return Map.of(
                "repositoryId", repositoryId,
                "status", "ERROR",
                "error", errorMessage,
                "stdout", "",
                "stderr", "",
                "logs", ""
        );
    }
}
