package com.afeng.plagchenckpro.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BatchUploadFile {

    @TableId(type = IdType.AUTO)
    private Integer id;

    //任务ID
    @TableField("task_id")
    private String taskId;

    //文件名
    @TableField("file_name")
    private String fileName;

    //文件路径
    @TableField("file_path")
    private String filePath;

    //文件大小
    @TableField("file_size")
    private Long fileSize;

    //文件状态
    @TableField("status")
    private String status; // pending, processing, success, failed

    //文件处理信息
    @TableField("message")
    private String message;

    //文件处理结果数据
    @TableField("result_data")
    private String resultData;

    //创建时间
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    //更新时间
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
