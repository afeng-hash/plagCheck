package com.afeng.plagchenckpro.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 相似句子信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SimilarSentence {
    /**
     * 原句子
     */
    private String originalSentence;

    /**
     * 相似句子
     */
    private String similarSentence;

    /**
     * 相似度
     */
    private double similarity;

    /**
     * 来源论文ID
     */
    private Long sourcePaperId;

    /**
     * 来源论文标题
     */
    private String sourcePaperTitle;

    /**
     * 来源论文作者
     */
    private String sourcePaperAuthor;
}
