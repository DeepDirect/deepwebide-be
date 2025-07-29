package com.deepdirect.deepwebide_be.repository.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.deepdirect.deepwebide_be.file.domain.FileContent;
import com.deepdirect.deepwebide_be.file.domain.FileNode;
import com.deepdirect.deepwebide_be.file.domain.FileType;
import com.deepdirect.deepwebide_be.file.repository.FileContentRepository;
import com.deepdirect.deepwebide_be.file.repository.FileNodeRepository;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import com.deepdirect.deepwebide_be.repository.domain.RepositoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RepositoryFileService {

    private final AmazonS3 amazonS3;
    private final FileNodeRepository fileNodeRepository;
    private final FileContentRepository fileContentRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /*** 레포지토리 생성시 호출! 템플릿 파일 구조 DB에 등록 ***/
    @Transactional
    public void initializeTemplateFiles(Repository repository) {
        String key = "templates/" + repository.getRepositoryType().name() + ".zip";

        try (S3Object s3Object = amazonS3.getObject(bucket, key);
             InputStream s3InputStream = s3Object.getObjectContent()) {

            // S3 스트림을 완전히 읽어서 메모리에 로드
            byte[] zipData = readAllBytes(s3InputStream);

            // 메모리의 데이터로 압축 해제
            Map<String, byte[]> files = unzip(zipData);

            // DB에 트리 구조로 저장
            saveToDatabase(repository, files);

        } catch (IOException e) {
            throw new RuntimeException("템플릿 압축 해제 실패", e);
        }
    }

    /*** InputStream을 완전히 읽어서 byte[]로 변환 ***/
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        return buffer.toByteArray();
    }

    /*** zip 압축 해제: byte[] → 파일경로-바이트배열 map ***/
    private Map<String, byte[]> unzip(byte[] zipData) throws IOException {
        Map<String, byte[]> files = new HashMap<>();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
             java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(bais)) {

            java.util.zip.ZipEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    files.put(entry.getName(), baos.toByteArray());
                }
                zis.closeEntry();
            }
        }

        return files;
    }

    // 나머지 메서드들은 동일...
    @Transactional
    private void saveToDatabase(Repository repository, Map<String, byte[]> files) {
        // 기존 코드와 동일
        Map<String, FileNode> folderMap = new HashMap<>();

        // 먼저 폴더부터 모두 만들어줌 (파일 경로 분석)
        for (String filePath : files.keySet()) {
            List<String> folders = getFolderPaths(filePath);
            String cumulativePath = "";
            FileNode parent = null;
            for (String folder : folders) {
                cumulativePath = cumulativePath.isEmpty() ? folder : cumulativePath + "/" + folder;
                if (!folderMap.containsKey(cumulativePath)) {
                    FileNode folderNode = FileNode.builder()
                            .repository(repository)
                            .name(folder)
                            .fileType(FileType.FOLDER)
                            .path(cumulativePath)
                            .parent(parent)
                            .build();
                    folderNode = fileNodeRepository.save(folderNode);
                    folderMap.put(cumulativePath, folderNode);
                }
                parent = folderMap.get(cumulativePath);
            }
        }

        // 파일 저장
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String fullPath = entry.getKey();
            String fileName = getFileName(fullPath);
            String parentPath = getParentPath(fullPath);
            FileNode parent = parentPath.isEmpty() ? null : folderMap.get(parentPath);

            FileNode fileNode = FileNode.builder()
                    .repository(repository)
                    .name(fileName)
                    .fileType(FileType.FILE)
                    .path(fullPath)
                    .parent(parent)
                    .build();
            fileNode = fileNodeRepository.save(fileNode);

            FileContent fileContent = FileContent.builder()
                    .fileNode(fileNode)
                    .content(entry.getValue())
                    .build();
            fileContentRepository.save(fileContent);
        }
    }

    private List<String> getFolderPaths(String filePath) {
        int idx = filePath.lastIndexOf('/');
        if (idx == -1) return Collections.emptyList();
        String folders = filePath.substring(0, idx);
        return Arrays.asList(folders.split("/"));
    }

    private String getFileName(String path) {
        int idx = path.lastIndexOf('/');
        return (idx >= 0) ? path.substring(idx + 1) : path;
    }

    private String getParentPath(String path) {
        int idx = path.lastIndexOf('/');
        if (idx <= 0) return "";
        return path.substring(0, idx);
    }
}
