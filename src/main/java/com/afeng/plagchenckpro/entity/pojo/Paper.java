package com.afeng.plagchenckpro.entity.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 论文实体类
 * 存储论文的基本信息和元数据
 *
 * @author afeng
 */
@TableName("papers")
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paper {

    /**
     * 论文ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 论文标题
     */
    @TableField("title")
    private String title;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 摘要
     */
    @TableField("abstract")
    private String abstractText;

    /**
     * 论文全文内容
     */
    @TableField("content")
    private String content;

    /**
     * 文件路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件类型 (PDF, DOC, DOCX)
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    /**
     * 字数统计
     */
    @TableField("word_count")
    private Integer wordCount;

    /**
     * MinHash签名，用于快速相似度计算
     */
    @TableField("minhash_signature")
    private String minhashSignature;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
//    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 关联的句子列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Sentence> sentences;

    @TableField("tf_idf_vector")
    private String tfIdfVector; // TF-IDF向量，用于快速相似度计算

    @TableField("keywords")
    private String keywords; // 关键词集合，JSON格式存储

    @TableField("category")
    private String category; // 论文分类标签

    @TableField("complexity_score")
    private Double complexityScore; // 文本复杂度评分
}