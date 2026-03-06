package com.afeng.plagchenckpro.entity.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 句子实体类
 * 存储论文中每个句子的信息和元数据
 * 
 * @author afeng
 */
@TableName("sentences")
@AllArgsConstructor
@Data
@NoArgsConstructor
@ToString
public class Sentence {
    
    /** 句子ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 所属论文ID */
    @TableField("paper_id")
    private Long paperId;
    
    /** 句子文本内容 */
    @TableField("sentence_text")
    private String sentenceText;
    
    /** 句子在论文中的索引位置 */
    @TableField("sentence_index")
    private Integer sentenceIndex;
    
    /** 句子字数 */
    @TableField("word_count")
    private Integer wordCount;
    
    /** MinHash签名，用于快速相似度计算 */
    @TableField("minhash_signature")
    private String minhashSignature;
    
    /** LSH桶标识，用于预筛选相似句子 */
    @TableField("lsh_bucket")
    private String lshBucket;
    
    /** 创建时间 */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 句子的平均词向量
     */
//    private byte[] vectorBlob;

    @TableField("sentence_vector")
    private String sentenceVector; // 句子向量的Base64编码

    @TableField("key_terms")
    private String keyTerms; // 关键术语集合

    @TableField("semantic_hash")
    private String semanticHash; // 语义哈希值
}
