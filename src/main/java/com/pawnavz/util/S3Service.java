package com.pawnavz.util;

import com.pawnavz.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    @Value("${aws.s3.bucket:pawnavz-dev}")
    private String bucket;

    @Value("${aws.s3.region:ap-south-1}")
    private String region;

    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final String CDN_BASE = "https://cdn.pawnavz.com";

    /**
     * Uploads a file. Replace body with real AWS SDK v2 call in production.
     */
    public String upload(MultipartFile file, String folder) {
        validate(file);
        String ext = getExtension(file.getOriginalFilename());
        String key = folder + "/" + UUID.randomUUID() + "." + ext;

        // --- PRODUCTION: uncomment and wire AWS SDK v2 ---
        // S3Client s3 = S3Client.builder().region(Region.of(region)).build();
        // s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
        //         .contentType(file.getContentType()).build(),
        //     RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        // return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        // -------------------------------------------------

        log.info("[S3-MOCK] Uploading {} bytes → s3://{}/{}", file.getSize(), bucket, key);
        return CDN_BASE + "/" + key;
    }

    /**
     * Deletes a file by its CDN URL.
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(CDN_BASE)) return;
        String key = fileUrl.replace(CDN_BASE + "/", "");

        // --- PRODUCTION: uncomment and wire AWS SDK v2 ---
        // S3Client s3 = S3Client.builder().region(Region.of(region)).build();
        // s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        // -------------------------------------------------

        log.info("[S3-MOCK] Deleted s3://{}/{}", bucket, key);
    }

    // Alias kept for backward-compat with any code that calls uploadFile / deleteFile
    public String uploadFile(MultipartFile file, String folder) { return upload(file, folder); }
    public void deleteFile(String fileUrl) { delete(fileUrl); }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new BadRequestException("File must not be empty");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new BadRequestException("File type not allowed. Accepted: JPEG, PNG, WebP");
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new BadRequestException("File exceeds 5 MB limit");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
