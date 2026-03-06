package com.afeng.plagchenckpro.service.impl;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.entity.file.ByteArrayMultipartFile;
import com.afeng.plagchenckpro.entity.pojo.BatchUploadFile;
import com.afeng.plagchenckpro.entity.pojo.BatchUploadTask;
import com.afeng.plagchenckpro.entity.vo.BatchUploadResult;
import com.afeng.plagchenckpro.entity.vo.BatchUploadStatus;
import com.afeng.plagchenckpro.mapper.BatchUploadFileMapper;
import com.afeng.plagchenckpro.mapper.BatchUploadTaskMapper;
import com.afeng.plagchenckpro.service.BatchService;
import com.afeng.plagchenckpro.service.PaperService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



@Service
@Slf4j
public class BatchServiceImpl implements BatchService {


    @Autowired
    private BatchUploadTaskMapper taskMapper;

    @Autowired
    private BatchUploadFileMapper fileMapper;


    @Autowired
    private PaperService paperService;

    // 在类中注入自身
    @Autowired
    private ApplicationContext applicationContext;


    /**
     * 处理批量上传
     */
    @Override
    public BatchUploadResult processBatchUpload(List<MultipartFile> files) {
        long start = System.currentTimeMillis();

        // 创建任务记录
        BatchUploadTask task = new BatchUploadTask();
        String taskId = UUID.randomUUID().toString();
        task.setId(taskId);
        task.setTotalFiles((int) files.stream().filter(f -> !f.isEmpty()).count());
        task.setStatus("processing");
        task.setProgress(0);
        task.setMessage("任务开始处理");
        taskMapper.insert(task);

        // 过滤掉空文件
        List<MultipartFile> nonEmptyFiles = files.stream()
                .filter(file -> !file.isEmpty())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        List<BatchUploadFile> batchFiles = nonEmptyFiles.stream().map(file -> {
            BatchUploadFile batchFile = new BatchUploadFile();
            batchFile.setTaskId(taskId);
            batchFile.setFileName(file.getOriginalFilename());
            batchFile.setFileSize(file.getSize());
            batchFile.setStatus("pending");
            batchFile.setMessage("等待处理");
            return batchFile;
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // 批量保存所有文件信息
        if (!batchFiles.isEmpty()) {
            fileMapper.batchInsert(batchFiles);
        }

        // 为每个文件启动异步处理任务
        for (int j = 0; j < nonEmptyFiles.size(); j++) {
            MultipartFile file = nonEmptyFiles.get(j);
            BatchUploadFile batchFile = batchFiles.get(j);

            try {
                // 在同步阶段读取文件字节数据
                byte[] fileBytes = file.getBytes();

                // 异步处理每一个文件（传递字节数据）
                BatchService batchService = applicationContext.getBean(BatchService.class);
                batchService.processSingleFileAsync(taskId, batchFile.getId(), fileBytes, file.getOriginalFilename());
            } catch (Exception e) {
                log.error("处理批量上传，读取文件内容失败: {}", e.getMessage(), e);
                // 更新文件状态为失败
                BatchUploadFile failedFile = new BatchUploadFile();
                failedFile.setId(batchFile.getId());
                failedFile.setStatus("failed");
                failedFile.setMessage("文件读取失败: " + e.getMessage());
                fileMapper.update(failedFile);
                updateTaskProgress(taskId);
            }
        }

        // 返回结果
        BatchUploadResult result = new BatchUploadResult();
        result.setTaskId(taskId);
        result.setTotalFiles(nonEmptyFiles.size());
        result.setSuccess(true);
        result.setMessage("任务已提交，正在后台处理");

        long end = System.currentTimeMillis();
        log.info("批量处理完成，耗时: {} ms", end - start);
        return result;
    }


    /**
     * 异步处理单个文件
     * @param taskId
     * @param fileId
     * @return
     */
    @Override
    @Async
    public void processSingleFileAsync(String taskId, Integer fileId, byte[] fileBytes, String originalFilename) {
        try {
            // 使用字节数据创建临时 MultipartFile
            MultipartFile file = new ByteArrayMultipartFile(fileBytes,originalFilename, originalFilename);

            // 更新文件状态为处理中
            BatchUploadFile batchFile = new BatchUploadFile();
            batchFile.setId(fileId);
            batchFile.setStatus("processing");
            batchFile.setMessage("正在处理");
            fileMapper.update(batchFile);


            if (originalFilename != null && originalFilename.contains("_")) {
                String[] parts = originalFilename.split("_");
                if(!"BS".equals(parts[parts.length-1])){
                    if (parts.length == 3) {
                        String author = parts[1];  // 黄子敬
                        String title = parts[2];   // 招聘数据挖掘与统计建模研究
                        // 可以去除文件扩展名（如 .docx, .pdf 等）
                        if (title.contains(".")) {
                            title = title.substring(0, title.lastIndexOf("."));
                        }
                        paperService.uploadPaper(file, title, author);
                    } else {
                        paperService.uploadPaper(file, null, null);
                    }
                }

            } else {
                paperService.uploadPaper(file, null, null);
            }

            // 更新文件处理结果
            batchFile = new BatchUploadFile();
            batchFile.setId(fileId);
            batchFile.setStatus("success");
            batchFile.setMessage("处理完成");
            batchFile.setFilePath(null);
            batchFile.setResultData("文件处理成功");
            fileMapper.update(batchFile);

        } catch (Exception e) {
            // 处理失败
            log.error("处理批量上传，处理文件失败: {}", e.getMessage(), e);
            BatchUploadFile batchFile = new BatchUploadFile();
            batchFile.setId(fileId);
            batchFile.setStatus("failed");
            batchFile.setMessage("处理失败: " + e.getMessage());
            fileMapper.update(batchFile);
        } finally {
            // 更新任务进度
            updateTaskProgress(taskId);
        }

    }


    /**
     * 获取上传任务状态
     */
    @Override
    public BatchUploadStatus getBatchUploadStatus(String taskId) {
        BatchUploadTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }

        BatchUploadStatus status = new BatchUploadStatus();
        status.setTaskId(task.getId());
        status.setStatus(task.getStatus());
        status.setProgress(task.getProgress());
        status.setMessage(task.getMessage());
        return status;
    }


    /**
     * 获取批量处理结果
     */
    @Override
    public Result getBatchResults(String taskId) {
        BatchUploadTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return Result.fail();
        }
        BatchUploadResult result = new BatchUploadResult();
        result.setTaskId(task.getId());
        result.setTotalFiles(task.getTotalFiles());
        result.setSuccessCount(task.getSuccessCount());
        result.setFailedCount(task.getFailedCount());
        result.setSuccess("completed".equals(task.getStatus()));
        result.setMessage(task.getMessage());

        if(task.getFailedCount()>0){
            LambdaQueryWrapper<BatchUploadFile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BatchUploadFile::getTaskId, taskId);
            queryWrapper.eq(BatchUploadFile::getStatus, "failed");
            result.setFailedFiles(fileMapper.selectList(queryWrapper));
        }

        return Result.ok(result);
    }

    private final Map<String, Object> taskLocks = new ConcurrentHashMap<>();

    // 更新任务为完成状态
    @Override
    public void updateTaskProgress(String taskId) {
        // 为每个任务使用独立的锁
        Object taskLock = taskLocks.computeIfAbsent(taskId, k -> new Object());
        synchronized (taskLock) {
            BatchUploadTask task = taskMapper.selectById(taskId);
            if (task == null) {
                return;
            }

            int totalFiles = task.getTotalFiles();
            int successCount = fileMapper.countSuccessByTaskId(taskId);
            int failedCount = fileMapper.countFailedByTaskId(taskId);
            int processedCount = successCount + failedCount;

            // 更新任务统计
            task.setSuccessCount(successCount);
            task.setFailedCount(failedCount);

            // 计算进度
            int progress = totalFiles > 0 ? (int) (((double) processedCount / totalFiles) * 100) : 0;
            task.setProgress(progress);

            // 更新状态
            if (processedCount >= totalFiles && totalFiles > 0) {
                task.setStatus("completed");
                task.setMessage("批量处理完成");
            } else {
                task.setStatus("processing");
                task.setMessage("处理中...");
            }

            taskMapper.update(task);

            // 清理已完成任务的锁
            if ("completed".equals(task.getStatus())) {
                taskLocks.remove(taskId);
            }
        }
    }
}

