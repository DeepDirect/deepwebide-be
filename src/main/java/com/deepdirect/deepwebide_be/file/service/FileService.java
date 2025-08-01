package com.deepdirect.deepwebide_be.file.service;

import com.deepdirect.deepwebide_be.file.domain.FileContent;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import com.deepdirect.deepwebide_be.file.domain.FileType;
import com.deepdirect.deepwebide_be.file.dto.request.FileCreateRequest;
import com.deepdirect.deepwebide_be.file.dto.response.*;
import com.deepdirect.deepwebide_be.file.repository.FileContentRepository;
import com.deepdirect.deepwebide_be.file.repository.FileNodeRepository;
import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class FileService {

    private final RepositoryRepository repositoryRepository;
    private final FileNodeRepository fileNodeRepository;
    private final FileContentRepository fileContentRepository;
    private final FileContentRedisService fileContentRedisService;

    public List<FileTreeNodeResponse> getFileTree(Long repositoryId, Long userId) {
        // 1. 레포지토리/권한 체크
        repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 모든 FileNode 조회 (1쿼리)
        List<FileNode> allNodes = fileNodeRepository.findAllByRepositoryId(repositoryId);

        // 3. id → FileTreeNodeResponse 변환 및 맵핑
        Map<Long, FileTreeNodeResponse> idToNode = new HashMap<>();
        for (FileNode node : allNodes) {
            idToNode.put(node.getId(),
                    FileTreeNodeResponse.builder()
                            .fileId(node.getId())
                            .fileName(node.getName())
                            .fileType(node.getFileType().name())
                            .parentId(node.getParent() == null ? null : node.getParent().getId())
                            .path(node.getPath())
                            .children(new ArrayList<>())
                            .build()
            );
        }
        // 4. 부모-자식 연결 및 루트 노드 추출
        List<FileTreeNodeResponse> roots = new ArrayList<>();
        for (FileTreeNodeResponse node : idToNode.values()) {
            if (node.getParentId() == null) {
                roots.add(node);
            } else {
                FileTreeNodeResponse parent = idToNode.get(node.getParentId());
                if (parent != null) parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Transactional
    public FileNodeResponse createFileOrFolder(Long repositoryId, Long userId, FileCreateRequest req) {

        if (req.getParentId() == null) {
            throw new GlobalException(ErrorCode.PARENT_ID_REQUIRED);
        }

        if ("FILE".equals(req.getFileType())) {
            validateFileNameHasExtension(req.getFileName());
        }

        // 1. 레포 권한 체크
        Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 부모 폴더 체크 (없으면 null)
        FileNode parent = null;
        String parentPath = "";
        if (req.getParentId() != null) {
            parent = findFileNodeWithRepositoryCheck(repositoryId, req.getParentId());
            if (!parent.getFileType().equals(FileType.FOLDER)) {
                throw new GlobalException(ErrorCode.INVALID_PARENT_TYPE);
            }
            parentPath = parent.getPath();
        }

        // 3. 중복 이름 체크 (동일 폴더 하위에 동일 이름)
        if (fileNodeRepository.existsByRepositoryIdAndParentIdAndName(
                repositoryId, req.getParentId(), req.getFileName())) {
            throw new GlobalException(ErrorCode.DUPLICATE_FILE_NAME);
        }

        // 4. 경로 계산
        String newPath = parentPath.isEmpty() ? req.getFileName() : parentPath + "/" + req.getFileName();

        // 5. FileNode 저장
        FileNode fileNode = FileNode.builder()
                .repository(repo)
                .name(req.getFileName())
                .fileType(FileType.valueOf(req.getFileType()))
                .parent(parent)
                .path(newPath)
                .build();
        fileNode = fileNodeRepository.save(fileNode);

        // 6. 파일이면 내용도 생성 (빈 파일)
        if (fileNode.getFileType() == FileType.FILE) {
            FileContent content = FileContent.builder()
                    .fileNode(fileNode)
                    .content(new byte[0])
                    .build();
            fileContentRepository.save(content);
        }

        // 7. 응답 DTO 반환
        return FileNodeResponse.builder()
                .fileId(fileNode.getId())
                .fileName(fileNode.getName())
                .fileType(fileNode.getFileType().name())
                .parentId(parent == null ? null : parent.getId())
                .path(fileNode.getPath())
                .build();
    }

    @Transactional
    public FileRenameResponse renameFileOrFolder(Long repositoryId, Long fileId, Long userId, String newFileName) {
        // 1. 권한/레포 체크
        repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));
        FileNode fileNode = findFileNodeWithRepositoryCheck(repositoryId, fileId);

        if (fileNode.getFileType() == FileType.FILE) {
            validateFileNameHasExtension(newFileName);
        }

        // 2. 같은 폴더 내에 동일 이름 존재 체크
        Long parentId = (fileNode.getParent() == null) ? null : fileNode.getParent().getId();
        boolean isDuplicate = fileNodeRepository.existsByRepositoryIdAndParentIdAndName(
                repositoryId, parentId, newFileName);
        if (isDuplicate) throw new GlobalException(ErrorCode.DUPLICATE_FILE_NAME);

        // 3. 이름 변경 + 경로(path) 재계산
        fileNode.rename(newFileName);

        // 4. 하위 모든 파일/폴더 경로(path)도 재귀적으로 수정 (폴더일 경우)
        updateChildPathsRecursively(fileNode);

        // 5. 결과 반환
        return FileRenameResponse.builder()
                .fileId(fileNode.getId())
                .fileName(fileNode.getName())
                .path(fileNode.getPath())
                .build();
    }

    // 하위 경로 업데이트 (폴더 재귀)
    private void updateChildPathsRecursively(FileNode parentNode) {
        if (!parentNode.isFolder()) return;
        List<FileNode> children = fileNodeRepository.findAllByParent(parentNode);
        for (FileNode child : children) {
            child.updatePath(parentNode.getPath() + "/" + child.getName());
            updateChildPathsRecursively(child);
        }
    }

    @Transactional
    public void deleteFileOrFolder(Long repositoryId, Long fileId, Long userId) {
        repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));
        FileNode node = findFileNodeWithRepositoryCheck(repositoryId, fileId);

        // (폴더일 경우) 하위 전체 삭제 (재귀)
        if (node.isFolder()) {
            deleteChildrenRecursively(node);
        }

        // 파일 내용 삭제 (FileType.FILE)
        if (node.getFileType() == FileType.FILE) {
            fileContentRepository.deleteByFileNode(node);
        }

        // 자기 자신 삭제
        fileNodeRepository.delete(node);
    }

    // 하위 전체 삭제 재귀
    private void deleteChildrenRecursively(FileNode parent) {
        List<FileNode> children = fileNodeRepository.findAllByParent(parent);
        for (FileNode child : children) {
            if (child.isFolder()) {
                deleteChildrenRecursively(child);
            }
            if (child.getFileType() == FileType.FILE) {
                fileContentRepository.deleteByFileNode(child);
            }
            fileNodeRepository.delete(child);
        }
    }

    @Transactional
    public FileNodeResponse moveFileOrFolder(
            Long repositoryId, Long fileId, Long userId, Long newParentId) {

        if (newParentId == null) {
            throw new GlobalException(ErrorCode.PARENT_ID_REQUIRED);
        }

        repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));
        FileNode fileNode = findFileNodeWithRepositoryCheck(repositoryId, fileId);

        // 새 부모 폴더 체크
        FileNode newParent = null;
        String newParentPath = "";
        newParent = findFileNodeWithRepositoryCheck(repositoryId, newParentId);
        if (!newParent.isFolder()) {
            throw new GlobalException(ErrorCode.INVALID_PARENT_TYPE);
        }
        // 순환구조 방지 (본인 또는 하위로 이동 불가)
        if (isDescendant(fileNode, newParent)) {
            throw new GlobalException(ErrorCode.CANNOT_MOVE_TO_CHILD);
        }
        newParentPath = newParent.getPath();

        // 같은 폴더에 동일 이름 체크
        if (fileNodeRepository.existsByRepositoryIdAndParentAndName(
                repositoryId, newParent, fileNode.getName())) {
            throw new GlobalException(ErrorCode.DUPLICATE_FILE_NAME);
        }

        // parent 변경 & path 재계산
        fileNode.moveToParent(newParent, newParentPath);

        // 하위 경로 재귀 갱신 (폴더일 경우)
        updateChildPathsRecursively(fileNode);

        // 저장/응답
        return FileNodeResponse.builder()
                .fileId(fileNode.getId())
                .fileName(fileNode.getName())
                .fileType(fileNode.getFileType().name())
                .parentId(newParent.getId())
                .path(fileNode.getPath())
                .build();
    }

    // --- 본인 또는 하위로 이동 방지 ---
    private boolean isDescendant(FileNode node, FileNode targetParent) {
        FileNode current = targetParent;
        while (current != null) {
            if (current.getId().equals(node.getId())) return true;
            current = current.getParent();
        }
        return false;
    }

    @Transactional(readOnly = true)
    public FileContentResponse getFileContent(Long repositoryId, Long fileId, Long userId) {
        repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));
        FileNode fileNode = findFileNodeWithRepositoryCheck(repositoryId, fileId);

        if (fileNode.getFileType() == FileType.FOLDER) {
            throw new GlobalException(ErrorCode.CANNOT_OPEN_FOLDER);
        }

        FileContent fileContent = fileContentRepository.findByFileNode(fileNode)
                .orElseThrow(() -> new GlobalException(ErrorCode.FILE_CONTENT_NOT_FOUND));

        // 확장자 체크
        String fileName = fileNode.getName();
        String extension = "";
        int idx = fileName.lastIndexOf('.');
        if (idx > 0) extension = fileName.substring(idx + 1).toLowerCase();

        String content;
        // 이미지/바이너리면 Base64, 텍스트면 UTF-8
        if (List.of("png", "jpg", "jpeg", "gif", "svg").contains(extension)) {
            content = Base64.getEncoder().encodeToString(fileContent.getContent());
        } else {
            content = new String(fileContent.getContent(), java.nio.charset.StandardCharsets.UTF_8);
        }

        return FileContentResponse.builder()
                .fileId(fileNode.getId())
                .fileName(fileNode.getName())
                .path(fileNode.getPath())
                .content(content)
                .build();
    }

    @Transactional
    public FileContentSaveResponse saveFileContent(Long repositoryId, Long fileId, Long userId, String content) {
        // 1. 레포 권한 및 존재 확인
        Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 파일 노드 + 소속 레포 검증
        FileNode fileNode = findFileNodeWithRepositoryCheck(repositoryId, fileId);

        // 3. 폴더라면 예외
        if (fileNode.getFileType() == FileType.FOLDER) {
            throw new GlobalException(ErrorCode.CANNOT_SAVE_FOLDER);
        }

        // 4. Redis에 파일 내용 저장
        fileContentRedisService.saveFileContent(repositoryId, fileId, content);

        // 5. 응답 DTO
        return FileContentSaveResponse.builder()
                .fileId(fileNode.getId())
                .fileName(fileNode.getName())
                .path(fileNode.getPath())
                .updatedAt(java.time.LocalDateTime.now().toString())
                .build();
    }

    // 파일이 진짜 레포 소속인지 검증
    private FileNode findFileNodeWithRepositoryCheck(Long repositoryId, Long fileId) {
        FileNode fileNode = fileNodeRepository.findById(fileId)
                .orElseThrow(() -> new GlobalException(ErrorCode.FILE_NOT_FOUND));
        if (!fileNode.getRepository().getId().equals(repositoryId)) {
            throw new GlobalException(ErrorCode.FILE_NOT_FOUND); // 또는 ACCESS_DENIED
        }
        return fileNode;
    }


    @Transactional
    public FileNodeResponse uploadFile(Long repositoryId, Long userId, Long parentId, MultipartFile file) {

        if (parentId == null) {
            throw new GlobalException(ErrorCode.PARENT_ID_REQUIRED);
        }

        String fileName = file.getOriginalFilename();
        validateFileNameHasExtension(fileName);


        // 1. 권한/레포 체크
        Repository repo = repositoryRepository.findByIdAndMemberOrOwner(repositoryId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 부모 폴더 체크
        FileNode parent = null;
        String parentPath = "";
        parent = findFileNodeWithRepositoryCheck(repositoryId, parentId);
        if (!parent.getFileType().equals(FileType.FOLDER)) {
            throw new GlobalException(ErrorCode.INVALID_PARENT_TYPE);
        }
        parentPath = parent.getPath();

        // 3. 중복 이름 체크
        if (fileNodeRepository.existsByRepositoryIdAndParentIdAndName(
                repositoryId, parentId, file.getOriginalFilename())) {
            throw new GlobalException(ErrorCode.DUPLICATE_FILE_NAME);
        }

        // 4. 경로 계산
        String newPath = parentPath.isEmpty() ? file.getOriginalFilename() : parentPath + "/" + file.getOriginalFilename();

        // 5. FileNode 생성
        FileNode fileNode = FileNode.builder()
                .repository(repo)
                .name(file.getOriginalFilename())
                .fileType(FileType.FILE)
                .parent(parent)
                .path(newPath)
                .build();
        fileNode = fileNodeRepository.save(fileNode);

        // 6. 파일 내용 저장
        try {
            FileContent content = FileContent.builder()
                    .fileNode(fileNode)
                    .content(file.getBytes())
                    .build();
            fileContentRepository.save(content);
        } catch (IOException e) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAIL);
        }

        // 7. 응답 반환
        return FileNodeResponse.builder()
                .fileId(fileNode.getId())
                .fileName(fileNode.getName())
                .fileType("FILE")
                .parentId(parent.getId())
                .path(fileNode.getPath())
                .build();
    }

    private void validateFileNameHasExtension(String fileName) {
        if (fileName == null || !fileName.contains(".") || fileName.startsWith(".") || fileName.endsWith(".")) {
            throw new GlobalException(ErrorCode.FILE_EXTENSION_REQUIRED);
        }
    }

}
