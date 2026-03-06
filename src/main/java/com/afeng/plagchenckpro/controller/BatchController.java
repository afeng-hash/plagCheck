package com.afeng.plagchenckpro.controller;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.entity.vo.BatchUploadResult;
import com.afeng.plagchenckpro.entity.vo.BatchUploadStatus;
import com.afeng.plagchenckpro.service.BatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/batch")
@CrossOrigin(origins = "*")
@Slf4j
public class BatchController {

    @Autowired
    private BatchService batchService;


    /**
     * 批量上传PDF文件
     */
    @PostMapping("/upload")
    public Result batchUploadPDF(@RequestParam("files") List<MultipartFile> files) {
        try {
            BatchUploadResult result = batchService.processBatchUpload(files);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("上传失败: {}", e.getMessage());
            BatchUploadResult errorResult = new BatchUploadResult();
            errorResult.setSuccess(false);
            errorResult.setMessage("上传失败: " + e.getMessage());
            return Result.fail(errorResult);
        }
    }

    /**
     * 获取批量上传任务状态
     */
    @GetMapping("/status/{taskId}")
    public Result<BatchUploadStatus> getBatchUploadStatus(@PathVariable String taskId) {
        log.info("获取批量上传任务状态: {}", taskId);
        BatchUploadStatus status = batchService.getBatchUploadStatus(taskId);
        if (status == null) {
            return Result.fail();
        }
        return Result.ok(status);
    }

    /**
     * 获取批量处理结果
     */
    @GetMapping("/result/{taskId}")
    public Result getBatchResults(@PathVariable String taskId) {
        log.info("获取批量处理结果: {}", taskId);
        return batchService.getBatchResults(taskId);
    }
}
