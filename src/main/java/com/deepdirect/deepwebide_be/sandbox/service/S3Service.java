package com.deepdirect.deepwebide_be.sandbox.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String uuid) throws IOException {
        String key = "sandbox-projects/" + uuid + ".zip";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(bucket, key, file.getInputStream(), metadata);
        return amazonS3.getUrl(bucket, key).toString();
    }
}

