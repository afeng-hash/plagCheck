package com.afeng.plagchenckpro.entity.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 分词实体类
 * 存储句子中每个词的信息，主要用于调试和分析
 * 
 * @author afeng
 */
@TableName("words")
public class Word {
    
    /** 词ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 所属句子ID */
    @TableField("sentence_id")
    private Long sentenceId;
    
    /** 词文本 */
    @TableField("word_text")
    private String wordText;
    
    /** 词在句子中的索引位置 */
    @TableField("word_index")
    private Integer wordIndex;
    
    /** 是否为停用词 */
    @TableField("is_stop_word")
    private Boolean isStopWord;
    
    /** 创建时间 */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /** 逻辑删除标记 */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
    
    /**
     * 默认构造函数
     */
    public Word() {
    }
    
    /**
     * 构造函数
     * @param sentenceId 句子ID
     * @param wordText 词文本
     * @param wordIndex 词索引
     * @param isStopWord 是否为停用词
     */
    public Word(Long sentenceId, String wordText, Integer wordIndex, Boolean isStopWord) {
        this.sentenceId = sentenceId;
        this.wordText = wordText;
        this.wordIndex = wordIndex;
        this.isStopWord = isStopWord;
    }
    
    // Getter和Setter方法
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSentenceId() {
        return sentenceId;
    }
    
    public void setSentenceId(Long sentenceId) {
        this.sentenceId = sentenceId;
    }
    
    public String getWordText() {
        return wordText;
    }
    
    public void setWordText(String wordText) {
        this.wordText = wordText;
    }
    
    public Integer getWordIndex() {
        return wordIndex;
    }
    
    public void setWordIndex(Integer wordIndex) {
        this.wordIndex = wordIndex;
    }
    
    public Boolean getIsStopWord() {
        return isStopWord;
    }
    
    public void setIsStopWord(Boolean isStopWord) {
        this.isStopWord = isStopWord;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", sentenceId=" + sentenceId +
                ", wordText='" + wordText + '\'' +
                ", wordIndex=" + wordIndex +
                ", isStopWord=" + isStopWord +
                '}';
    }
}
