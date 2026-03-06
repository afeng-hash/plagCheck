package com.afeng.plagchenckpro.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("batch_upload_task")
public class BatchUploadTask {

    //任务ID
    private String id;

    //总文件数
    @TableField("total_files")
    private Integer totalFiles;

    //成功文件数
    @TableField("success_count")
    private Integer successCount;

    //失败文件数
    @TableField("failed_count")
    private Integer failedCount;

    //任务状态
    @TableField("status")
    private String status; // pending, processing, completed, failed

    //任务进度
    @TableField("progress")
    private Integer progress;

    //任务消息
    @TableField("message")
    private String message;

    //创建时间
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    //更新时间
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
