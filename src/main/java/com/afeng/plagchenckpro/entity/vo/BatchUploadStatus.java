package com.afeng.plagchenckpro.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BatchUploadStatus {
    // 任务ID
    private String taskId;
    private String status; // pending, processing, completed, failed
    private int progress; // 0-100
    private String message;
}
