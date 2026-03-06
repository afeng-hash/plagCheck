package com.afeng.plagchenckpro.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 相似句子实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarResult {
    List<SimilarSentence> list;
    long count;
}
