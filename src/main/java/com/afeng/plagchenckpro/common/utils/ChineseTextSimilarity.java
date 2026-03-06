package com.afeng.plagchenckpro.common.utils;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 计算句子向量
 */
@Component
public class ChineseTextSimilarity {

    // 词向量模型实例
    private final WordVectors wordVectors;  // 使用 WordVectors 接口

    public ChineseTextSimilarity() throws Exception {
        ClassPathResource resource = new ClassPathResource("wordvec/70000-small.txt");
        InputStream inputStream = resource.getInputStream();
        this.wordVectors = WordVectorSerializer.loadStaticModel(inputStream);
        System.out.println("词向量模型加载完成！词汇表大小: " + wordVectors.vocab().numWords());
    }

    /**
     * 获取词的向量
     * @param word 输入词
     * @return 向量数组，若词不存在返回null
     */
    public double[] getWordVector(String word) {
        INDArray vector = wordVectors.getWordVectorMatrix(word);
        return vector != null ? vector.toDoubleVector() : null;
    }

    /**
     * 计算两个词的余弦相似度
     * @param word1 词1
     * @param word2 词2
     * @return 相似度（-1到1之间），若任一词不存在返回-2
     */
    public double calculateWordSimilarity(String word1, String word2) {
        INDArray vec1 = wordVectors.getWordVectorMatrix(word1);
        INDArray vec2 = wordVectors.getWordVectorMatrix(word2);

        if (vec1 == null || vec2 == null) {
            System.err.println("警告: 词不在词汇表中 - " +
                    (vec1 == null ? word1 : "") + " " + (vec2 == null ? word2 : ""));
            return -2.0;
        }

        return Transforms.cosineSim(vec1, vec2);
    }

    /**
     * 计算句子相似度（词向量平均）
     * @param sentence1 句子1
     * @param sentence2 句子2
     * @return 相似度（0到1之间）
     */
    public double calculateSentenceSimilarity(String sentence1, String sentence2) {
        INDArray vec1 = getSentenceVector(sentence1);
        INDArray vec2 = getSentenceVector(sentence2);

        if (vec1 == null || vec2 == null) {
            System.err.println("警告: 句子中无有效词汇");
            return 0.0;
        }

        return Transforms.cosineSim(vec1, vec2);
    }

    /**
     * 计算句子相似度重载
     */
    public double calculateSentenceSimilarity(INDArray vec1,INDArray vec2){
        if (vec1 == null || vec2 == null) {
            System.err.println("警告: 句子中无有效词汇");
            return 0.0;
        }

        return Transforms.cosineSim(vec1, vec2);
    }

    /**
     * 获取句子的向量（词向量平均）
     * @param sentence 输入句子
     * @return 句子向量，若无效返回null
     */
    public INDArray getSentenceVector(String sentence) {
        //进行分词
        List<String> words = TextProcess.segmentWords(sentence);
        INDArray sentenceVec = null;
        int validWordCount = 0;

        for (String word : words) {
            INDArray wordVec = wordVectors.getWordVectorMatrix(word);
            if (wordVec != null) {
                if (sentenceVec == null) {
                    sentenceVec = wordVec.dup(); // 初始化
                } else {
                    sentenceVec.addi(wordVec);  // 累加
                }
                validWordCount++;
            }
        }

        if (validWordCount > 0 && sentenceVec != null) {
            return sentenceVec.divi(validWordCount); // 平均
        }
        return null;
    }

    /**
     * 查找与目标词最相似的词
     * @param word 目标词
     * @param topN 返回最相似的N个词
     * @return 相似词列表
     */
    public List<String> findNearestWords(String word, int topN) {
        if (!wordVectors.hasWord(word)) {
            System.err.println("错误: 词不在词汇表中 - " + word);
            return List.of();
        }
        return (List<String>) wordVectors.wordsNearest(word, topN);
    }




    /**
     * 计算综合句子相似度
     * @param sentence1 句子1
     * @param sentence2 句子2
     * @return 综合相似度
     */
    public double calculateComprehensiveSimilarity(String sentence1, String sentence2) {
        if (sentence1 == null || sentence2 == null ||
                sentence1.trim().isEmpty() || sentence2.trim().isEmpty()) {
            return 0.0;
        }

        // 1. 词法相似度（编辑距离）
        double lexicalSim = calculateEditDistanceSimilarity(sentence1, sentence2);

        // 2. 语义相似度（词向量）
        double semanticSim = calculateSentenceSimilarity(sentence1, sentence2);

        // 3. 长度相似度
        double lengthSim = calculateLengthSimilarity(sentence1, sentence2);

        // 4. 关键词相似度
        double keywordSim = calculateKeywordSimilarity(sentence1, sentence2);

        // 5. 使用动态权重
        double[] weights = calculateDynamicWeights(sentence1, sentence2);

        // 加权综合评分
        return weights[0] * lexicalSim + weights[1] * semanticSim + weights[2] * lengthSim + weights[3] * keywordSim;
    }


    /**
     * 计算长度相似度
     */
    private double calculateLengthSimilarity(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 && len2 == 0) return 1.0;
        if (len1 == 0 || len2 == 0) return 0.0;
        return 1.0 - (double) Math.abs(len1 - len2) / Math.max(len1, len2);
    }

    /**
     * 计算关键词相似度
     */
    private double calculateKeywordSimilarity(String s1, String s2) {
        List<String> keywords1 = extractKeywords(s1);
        List<String> keywords2 = extractKeywords(s2);

        if (keywords1.isEmpty() && keywords2.isEmpty()) return 1.0;
        if (keywords1.isEmpty() || keywords2.isEmpty()) return 0.0;

        Set<String> set1 = new HashSet<>(keywords1);
        Set<String> set2 = new HashSet<>(keywords2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    /**
     * 提取关键词
     */
    private List<String> extractKeywords(String sentence) {
        List<String> words = TextProcess.segmentWords(sentence);
        return words.stream()
                .filter(word -> word.length() >= 2)
                .filter(word -> !TextProcess.isStopWord(word))
                .limit(5) // 取前5个关键词
                .collect(Collectors.toList());
    }


    private int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
    // 优化编辑距离算法，添加早期终止条件
    private double calculateEditDistanceSimilarity(String s1, String s2) {
        // 长度差异过大时直接返回
        int len1 = s1.length();
        int len2 = s2.length();
        if (Math.abs(len1 - len2) > Math.max(len1, len2) * 0.5) {
            return 0.0;
        }

        // 使用优化的编辑距离算法
        int distance = optimizedEditDistance(s1, s2);
        int maxLength = Math.max(len1, len2);
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }

    // 优化的编辑距离实现
    private int optimizedEditDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        // 使用一维数组优化空间复杂度
        int[] dp = new int[len2 + 1];
        for (int j = 0; j <= len2; j++) {
            dp[j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            int prev = dp[0];
            dp[0] = i;

            for (int j = 1; j <= len2; j++) {
                int temp = dp[j];
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[j] = prev;
                } else {
                    dp[j] = Math.min(Math.min(prev, dp[j]), dp[j-1]) + 1;
                }
                prev = temp;
            }
        }

        return dp[len2];
    }


    // 根据句子特征动态调整权重
    private double[] calculateDynamicWeights(String sentence1, String sentence2) {
        int len1 = sentence1.length();
        int len2 = sentence2.length();
        int avgLength = (len1 + len2) / 2;

        double[] weights = new double[4];

        if (avgLength < 20) {
            // 短句更重视词法相似度
            weights[0] = 0.3; // 词法相似度
            weights[1] = 0.4; // 语义相似度
            weights[2] = 0.15; // 长度相似度
            weights[3] = 0.15; // 关键词相似度
        } else if (avgLength > 100) {
            // 长句更重视语义相似度
            weights[0] = 0.1; // 词法相似度
            weights[1] = 0.6; // 语义相似度
            weights[2] = 0.15; // 长度相似度
            weights[3] = 0.15; // 关键词相似度
        } else {
            // 中等长度句子使用默认权重
            weights[0] = 0.2;
            weights[1] = 0.5;
            weights[2] = 0.15;
            weights[3] = 0.15;
        }

        return weights;
    }

}
