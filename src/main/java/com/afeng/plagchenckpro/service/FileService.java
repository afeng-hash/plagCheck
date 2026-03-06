package com.afeng.plagchenckpro.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 上传PDF文件到MinIO
     * @param file PDF文件
     * @return 文件访问URL
     */
    public String uploadPdfFile(MultipartFile file) throws Exception {
        // 检查bucket是否存在，不存在则创建
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;

        // 上传文件
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType("application/pdf")
                        .build()
        );


        // 生成永久访问URL
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );

        return url;
    }

    /**
     * 从URL中提取文件名
     * @param url 完整的文件URL
     * @return 文件名
     */
    private String extractFileNameFromUrl(String url) throws Exception {
        try {
            // 解析URL获取文件名
            String[] parts = url.split("/");
            return parts[parts.length - 1].split("\\?")[0]; // 去除查询参数
        } catch (Exception e) {
            throw new Exception("无法从URL中提取文件名: " + e.getMessage());
        }
    }

    /**
     * 根据上传返回的URL下载文件
     * @param fileUrl 上传时返回的文件URL
     * @return 文件流
     */
    public InputStream downloadFileByUrl(String fileUrl) throws Exception {
        String fileName = extractFileNameFromUrl(fileUrl);
        return downloadFile(fileName);
    }

    /**
     * 根据上传返回的URL删除文件
     * @param fileUrl 上传时返回的文件URL
     * @return 删除结果
     */
    public boolean deleteFileByUrl(String fileUrl) throws Exception {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            throw new Exception("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件下载流
     * @param fileName 文件名
     * @return 文件流
     */
    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 删除文件
     * @param fileName 文件名
     * @return 删除结果
     */
    public boolean deleteFile(String fileName) throws Exception {
        try {
            log.info("删除文件:{}", fileName);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            log.error("文件删除失败:{}", e.getMessage());
            throw new Exception("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     * @param fileUrls 文件URL列表
     * @return 删除结果
     */
    public boolean deleteFilesByUrl(List<String> fileUrls) throws Exception {
        try {
            List<DeleteObject> objects = new ArrayList<>();
            for (String fileUrl : fileUrls) {
                String fileName = extractFileNameFromUrl(fileUrl);
                objects.add(new DeleteObject(fileName));
            }

            minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build()
            );
            return true;
        } catch (Exception e) {
            throw new Exception("批量删除文件失败: " + e.getMessage());
        }
    }



    /* 批量删除文件
     * @param fileNames 文件名列表
     * @return 删除结果
     */
    public boolean deleteFiles(List<String> fileNames) throws Exception {
        try {
            List<DeleteObject> objects = new ArrayList<>();
            for (String fileName : fileNames) {
                objects.add(new DeleteObject(fileName));
            }

            minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build()
            );
            return true;
        } catch (Exception e) {
            throw new Exception("批量删除文件失败: " + e.getMessage());
        }
    }
}
