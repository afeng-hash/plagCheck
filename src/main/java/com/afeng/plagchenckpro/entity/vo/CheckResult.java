package com.afeng.plagchenckpro.entity.vo;

import com.afeng.plagchenckpro.entity.pojo.Paper;
import com.afeng.plagchenckpro.entity.vo.SimilarSentence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;


/**
 * 查重检测结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CheckResult {
    /** 查重率 (0.0-1.0) */
    private double similarityRate;

    /** 相似句子列表 */
    private List<SimilarSentence> similarSentences;

    /** 检测的论文ID */
    private Long paperId;

    /** 检测时间 */
    private String checkTime;

    /**
     * 标准
     */
    private String standard;

    /**
     * 来源最多的论文
     */
    List<PaperVo> topPapers;
}
