package com.afeng.plagchenckpro.entity.vo;

import com.afeng.plagchenckpro.entity.pojo.BatchUploadFile;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BatchUploadResult {

    //任务ID
    private String taskId;
    //任务状态
    private boolean success;
    private String message;
    //文件总数
    private int totalFiles;
    //成功文件数
    private int successCount;
    //失败文件数
    private int failedCount;
    //失败的文件列表
    private List<BatchUploadFile> failedFiles;
}
