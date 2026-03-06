package com.afeng.plagchenckpro.service;


import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.entity.vo.BatchUploadResult;
import com.afeng.plagchenckpro.entity.vo.BatchUploadStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BatchService {
    /**
     * 处理批量上传
     */
    BatchUploadResult processBatchUpload(List<MultipartFile> files);

    /**
     * 异步处理单个文件
     */
    void processSingleFileAsync(String taskId, Integer fileId, byte[] fileBytes, String originalFilename);

    /**
     * 获取上传任务状态
     */
    BatchUploadStatus getBatchUploadStatus(String taskId);

    /**
     * 获取批量处理结果
     */
    Result getBatchResults(String taskId);

    /**
     * 更新任务进度
     */
    void updateTaskProgress(String taskId);
}

