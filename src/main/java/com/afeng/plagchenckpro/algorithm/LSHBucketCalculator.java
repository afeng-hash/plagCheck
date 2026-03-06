package com.afeng.plagchenckpro.algorithm;

import java.util.Arrays;

/**
 * LSH (Locality Sensitive Hashing) 桶计算器
 * 用于将MinHash签名映射到LSH桶中，实现快速预筛选
 * 
 * LSH原理：
 * 1. 将MinHash签名分成多个带(band)
 * 2. 每个带内的哈希值组合成一个桶标识
 * 3. 相似文档有更高概率映射到相同的桶中
 * 4. 只对同桶文档进行精确相似度计算
 * 
 * @author afeng
 */
public class LSHBucketCalculator {
    
    /** 带的数量 */
    private final int numBands;
    
    /** 每个带的行数 */
    private final int rowsPerBand;
    
    /** 默认带数量 */
    private static final int DEFAULT_NUM_BANDS = 16;
    
    /** 默认每带行数 */
    private static final int DEFAULT_ROWS_PER_BAND = 8;
    
    /**
     * 构造函数，使用默认参数
     */
    public LSHBucketCalculator() {
        this(DEFAULT_NUM_BANDS, DEFAULT_ROWS_PER_BAND);
    }
    
    /**
     * 构造函数
     * @param numBands 带的数量
     * @param rowsPerBand 每个带的行数
     */
    public LSHBucketCalculator(int numBands, int rowsPerBand) {
        this.numBands = numBands;
        this.rowsPerBand = rowsPerBand;
    }
    
    /**
     * 计算MinHash签名对应的LSH桶标识
     * 
     * @param minHashSignature MinHash签名字符串
     * @return LSH桶标识
     */
    public String calculateBucket(String minHashSignature) {
        if (minHashSignature == null || minHashSignature.isEmpty()) {
            return "";
        }
        
        try {
            // 解析MinHash签名
            int[] signature = parseSignature(minHashSignature);
            
            // 计算总哈希函数数量
            int totalHashes = signature.length;
            int expectedHashes = numBands * rowsPerBand;
            
        // 如果哈希函数数量不足，调整参数
        if (totalHashes < expectedHashes) {
            return calculateBucketWithAdjustment(signature);
        }
            
            // 计算每个带的桶标识
            StringBuilder bucket = new StringBuilder();
            
            for (int band = 0; band < numBands; band++) {
                int start = band * rowsPerBand;
                int end = Math.min(start + rowsPerBand, signature.length);
                
                // 提取当前带的哈希值
                int[] bandHashes = Arrays.copyOfRange(signature, start, end);
                
                // 计算当前带的哈希值
                int bandHash = Arrays.hashCode(bandHashes);
                
                // 添加到桶标识中
                bucket.append(bandHash).append("_");
            }
            
            return bucket.toString();
            
        } catch (Exception e) {
            // 如果解析失败，返回空字符串
            return "";
        }
    }
    
    /**
     * 当哈希函数数量不足时的调整计算
     * 
     * @param signature MinHash签名数组
     * @return LSH桶标识
     */
    private String calculateBucketWithAdjustment(int[] signature) {
        
        // 动态调整带的数量和每带行数
        int actualBands = Math.min(numBands, signature.length);
        int actualRowsPerBand = Math.max(1, signature.length / actualBands);
        
        StringBuilder bucket = new StringBuilder();
        
        for (int band = 0; band < actualBands; band++) {
            int start = band * actualRowsPerBand;
            int end = Math.min(start + actualRowsPerBand, signature.length);
            
            int[] bandHashes = Arrays.copyOfRange(signature, start, end);
            int bandHash = Arrays.hashCode(bandHashes);
            
            bucket.append(bandHash).append("_");
        }
        
        return bucket.toString();
    }
    
    /**
     * 解析MinHash签名字符串
     * 
     * @param signature MinHash签名字符串
     * @return 哈希值数组
     */
    private int[] parseSignature(String signature) {
        // 移除方括号并分割
        String content = signature.substring(1, signature.length() - 1);
        String[] parts = content.split(", ");
        
        int[] hashes = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            hashes[i] = Integer.parseInt(parts[i].trim());
        }
        
        return hashes;
    }
    
    /**
     * 计算两个LSH桶的相似度
     * 通过比较桶标识中相同带的数量来判断相似度
     * 
     * @param bucket1 第一个LSH桶标识
     * @param bucket2 第二个LSH桶标识
     * @return 相似度 (0.0-1.0)
     */
    public double calculateBucketSimilarity(String bucket1, String bucket2) {
        if (bucket1 == null || bucket2 == null || 
            bucket1.isEmpty() || bucket2.isEmpty()) {
            return 0.0;
        }
        
        // 分割桶标识
        String[] bands1 = bucket1.split("_");
        String[] bands2 = bucket2.split("_");
        
        if (bands1.length != bands2.length) {
            return 0.0;
        }
        
        // 计算相同带的数量
        int sameBands = 0;
        for (int i = 0; i < bands1.length; i++) {
            if (bands1[i].equals(bands2[i])) {
                sameBands++;
            }
        }
        
        return (double) sameBands / bands1.length;
    }
    
    /**
     * 检查两个文档是否在同一个LSH桶中
     * 
     * @param bucket1 第一个LSH桶标识
     * @param bucket2 第二个LSH桶标识
     * @return 是否在同一桶中
     */
    public boolean isInSameBucket(String bucket1, String bucket2) {
        return bucket1 != null && bucket2 != null && bucket1.equals(bucket2);
    }
    
    /**
     * 获取带的数量
     * @return 带的数量
     */
    public int getNumBands() {
        return numBands;
    }
    
    /**
     * 获取每个带的行数
     * @return 每个带的行数
     */
    public int getRowsPerBand() {
        return rowsPerBand;
    }
}
