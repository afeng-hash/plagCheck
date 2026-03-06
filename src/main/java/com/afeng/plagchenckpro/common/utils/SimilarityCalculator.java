package com.afeng.plagchenckpro.common.utils;

import java.util.*;

/**
 * 相似度计算工具类
 * 
 * 提供多种文本相似度计算算法，用于论文查重系统中的文本对比分析。
 * 支持多种相似度计算方法，包括余弦相似度、Jaccard相似度、编辑距离等。
 * 
 * 主要算法：
 * - 余弦相似度：基于词频向量的余弦距离，适用于文本向量化对比
 * - Jaccard相似度：基于集合的交并比，适用于词汇集合对比
 * - 编辑距离：基于字符串编辑操作的距离，适用于字符串相似度
 * - N-gram相似度：基于N-gram特征的相似度，适用于局部特征匹配
 * - 综合相似度：多种算法的加权平均，提高查重精度
 * 
 * 应用场景：
 * - 论文查重检测
 * - 文本相似度分析
 * - 重复内容识别
 * 
 * @author AFeng
 * @version 1.0
 * @since 2024
 */
public class SimilarityCalculator {
    
    /**
     * 计算余弦相似度
     * 
     * 基于词频向量计算两个文本的余弦相似度。余弦相似度衡量的是两个向量
     * 在方向上的相似程度，取值范围为[0,1]，值越大表示越相似。
     * 
     * 计算公式：
     * similarity = (A · B) / (||A|| × ||B||)
     * 其中A·B表示向量点积，||A||和||B||表示向量的模长
     * 
     * @param vector1 第一个文本的词频向量
     * @param vector2 第二个文本的词频向量
     * @return double 余弦相似度值，范围[0,1]，1表示完全相同，0表示完全不同
     */
    public static double calculateCosineSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }
        
        // 获取所有词汇的并集
        Set<String> allWords = new HashSet<>(vector1.keySet());
        allWords.addAll(vector2.keySet());
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String word : allWords) {
            int freq1 = vector1.getOrDefault(word, 0);
            int freq2 = vector2.getOrDefault(word, 0);
            
            dotProduct += freq1 * freq2;
            norm1 += freq1 * freq1;
            norm2 += freq2 * freq2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 计算Jaccard相似度
     */
    public static double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1 == null || set2 == null || (set1.isEmpty() && set2.isEmpty())) {
            return 0.0;
        }
        
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * 计算编辑距离（Levenshtein距离）
     */
    public static int calculateEditDistance(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null || str1.isEmpty()) {
            return str2 == null ? 0 : str2.length();
        }
        if (str2 == null || str2.isEmpty()) {
            return str1.length();
        }
        
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        
        // 初始化
        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }
        
        // 填充动态规划表
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + 1);
                }
            }
        }
        
        return dp[str1.length()][str2.length()];
    }
    
    /**
     * 计算基于编辑距离的相似度
     */
    public static double calculateEditSimilarity(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return 1.0;
        }
        if (str1 == null || str2 == null || str1.isEmpty() || str2.isEmpty()) {
            return 0.0;
        }
        
        int editDistance = calculateEditDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        
        return 1.0 - (double) editDistance / maxLength;
    }
    
    /**
     * 计算N-gram相似度
     */
    public static double calculateNGramSimilarity(List<String> ngrams1, List<String> ngrams2) {
        if (ngrams1 == null || ngrams2 == null || ngrams1.isEmpty() || ngrams2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> set1 = new HashSet<>(ngrams1);
        Set<String> set2 = new HashSet<>(ngrams2);
        
        return calculateJaccardSimilarity(set1, set2);
    }
    
    /**
     * 计算综合相似度（多种算法的加权平均）
     */
    public static double calculateOverallSimilarity(
            Map<String, Integer> freq1, Map<String, Integer> freq2,
            List<String> ngrams1, List<String> ngrams2,
            String text1, String text2) {
        
        double cosineSim = calculateCosineSimilarity(freq1, freq2);
        double ngramSim = calculateNGramSimilarity(ngrams1, ngrams2);
        double editSim = calculateEditSimilarity(text1, text2);
        
        // 加权平均：余弦相似度权重最高，N-gram次之，编辑距离最低
        return cosineSim * 0.5 + ngramSim * 0.3 + editSim * 0.2;
    }
    
    /**
     * 计算滑动窗口相似度（用于检测局部相似性）
     */
    public static List<SimilaritySegment> calculateSlidingWindowSimilarity(
            List<String> words1, List<String> words2, int windowSize) {
        
        List<SimilaritySegment> segments = new ArrayList<>();
        
        if (words1 == null || words2 == null || words1.isEmpty() || words2.isEmpty()) {
            return segments;
        }
        
        for (int i = 0; i <= words1.size() - windowSize; i++) {
            List<String> window1 = words1.subList(i, i + windowSize);
            
            for (int j = 0; j <= words2.size() - windowSize; j++) {
                List<String> window2 = words2.subList(j, j + windowSize);
                
                double similarity = calculateWindowSimilarity(window1, window2);
                
                if (similarity > 0.7) { // 只记录高相似度的片段
                    segments.add(new SimilaritySegment(
                        i, i + windowSize - 1, j, j + windowSize - 1, similarity
                    ));
                }
            }
        }
        
        return segments;
    }
    
    /**
     * 计算窗口相似度
     */
    private static double calculateWindowSimilarity(List<String> window1, List<String> window2) {
        Map<String, Integer> freq1 = new HashMap<>();
        Map<String, Integer> freq2 = new HashMap<>();
        
        for (String word : window1) {
            freq1.put(word, freq1.getOrDefault(word, 0) + 1);
        }
        
        for (String word : window2) {
            freq2.put(word, freq2.getOrDefault(word, 0) + 1);
        }
        
        return calculateCosineSimilarity(freq1, freq2);
    }
    
    /**
     * 相似度片段类
     */
    public static class SimilaritySegment {
        private int start1, end1, start2, end2;
        private double similarity;
        
        public SimilaritySegment(int start1, int end1, int start2, int end2, double similarity) {
            this.start1 = start1;
            this.end1 = end1;
            this.start2 = start2;
            this.end2 = end2;
            this.similarity = similarity;
        }
        
        // Getters
        public int getStart1() { return start1; }
        public int getEnd1() { return end1; }
        public int getStart2() { return start2; }
        public int getEnd2() { return end2; }
        public double getSimilarity() { return similarity; }
        
        @Override
        public String toString() {
            return String.format("Segment[%d-%d vs %d-%d]: %.4f", 
                start1, end1, start2, end2, similarity);
        }
    }
    
    /**
     * 计算TF-IDF相似度
     */
    public static double calculateTFIDFSimilarity(
            Map<String, Double> tfidf1, Map<String, Double> tfidf2) {
        
        if (tfidf1 == null || tfidf2 == null || tfidf1.isEmpty() || tfidf2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> allWords = new HashSet<>(tfidf1.keySet());
        allWords.addAll(tfidf2.keySet());
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String word : allWords) {
            double tfidf1Value = tfidf1.getOrDefault(word, 0.0);
            double tfidf2Value = tfidf2.getOrDefault(word, 0.0);
            
            dotProduct += tfidf1Value * tfidf2Value;
            norm1 += tfidf1Value * tfidf1Value;
            norm2 += tfidf2Value * tfidf2Value;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 计算最长公共子序列相似度
     */
    public static double calculateLCSSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        if (text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }
        
        int lcsLength = calculateLCSLength(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        
        return (double) lcsLength / maxLength;
    }
    
    /**
     * 计算最长公共子序列长度
     */
    private static int calculateLCSLength(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }
}
