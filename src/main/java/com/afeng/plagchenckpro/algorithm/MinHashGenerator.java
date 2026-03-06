package com.afeng.plagchenckpro.algorithm;

import java.util.Arrays;
import java.util.Set;

/**
 * MinHash算法实现
 * 用于快速计算文本集合的相似度，减少精确比对次数
 * 
 * MinHash原理：
 * 1. 将文本转换为词集合
 * 2. 使用多个哈希函数计算每个词的最小哈希值
 * 3. 相似文本集合有更高概率产生相同的最小哈希值
 * 
 * @author afeng
 */
public class MinHashGenerator {
    
    /** 哈希函数数量，越多越精确但计算量越大 */
    private final int numHashFunctions;
    
    /** 哈希种子，用于生成不同的哈希函数 */
    private final int[] hashSeeds;
    
    /** 默认哈希函数数量 */
    private static final int DEFAULT_NUM_HASH_FUNCTIONS = 128;
    
    /**
     * 构造函数，使用默认哈希函数数量
     */
    public MinHashGenerator() {
        this(DEFAULT_NUM_HASH_FUNCTIONS);
    }
    
    /**
     * 构造函数
     * @param numHashFunctions 哈希函数数量
     */
    public MinHashGenerator(int numHashFunctions) {
        this.numHashFunctions = numHashFunctions;
        this.hashSeeds = new int[numHashFunctions];
        
        // 初始化哈希种子，使用质数确保哈希函数的随机性
        for (int i = 0; i < numHashFunctions; i++) {
            hashSeeds[i] = 31 * (i + 1) + 17; // 使用质数生成种子
        }
    }
    
    /**
     * 为词集合生成MinHash签名
     * 
     * @param words 词集合
     * @return MinHash签名字符串
     */
    public String generateMinHash(Set<String> words) {
        if (words == null || words.isEmpty()) {
            return "";
        }
        
        // 初始化最小哈希值数组
        int[] minHashes = new int[numHashFunctions];
        Arrays.fill(minHashes, Integer.MAX_VALUE);
        
        // 对每个词计算所有哈希函数的最小值
        for (String word : words) {
            for (int i = 0; i < numHashFunctions; i++) {
                int hash = hash(word, hashSeeds[i]);
                minHashes[i] = Math.min(minHashes[i], hash);
            }
        }
        
        // 将最小哈希值数组转换为字符串
        return Arrays.toString(minHashes);
    }
    
    /**
     * 计算两个MinHash签名的相似度
     * 
     * @param signature1 第一个MinHash签名
     * @param signature2 第二个MinHash签名
     * @return 相似度 (0.0-1.0)
     */
    public double calculateSimilarity(String signature1, String signature2) {
        if (signature1 == null || signature2 == null || 
            signature1.isEmpty() || signature2.isEmpty()) {
            return 0.0;
        }
        
        try {
            // 解析MinHash签名
            int[] hashes1 = parseSignature(signature1);
            int[] hashes2 = parseSignature(signature2);
            
            if (hashes1.length != hashes2.length) {
                return 0.0;
            }
            
            // 计算相同哈希值的数量
            int sameCount = 0;
            for (int i = 0; i < hashes1.length; i++) {
                if (hashes1[i] == hashes2[i]) {
                    sameCount++;
                }
            }
            
            // 返回相似度比例
            return (double) sameCount / hashes1.length;
            
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * 哈希函数实现
     * 使用线性同余生成器确保哈希的均匀分布
     * 
     * @param word 待哈希的词
     * @param seed 哈希种子
     * @return 哈希值
     */
    private int hash(String word, int seed) {
        if (word == null) {
            return 0;
        }
        
        int hash = seed;
        for (char c : word.toCharArray()) {
            hash = hash * 31 + c;
        }
        
        // 确保返回正数
        return Math.abs(hash);
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
     * 获取哈希函数数量
     * @return 哈希函数数量
     */
    public int getNumHashFunctions() {
        return numHashFunctions;
    }
}
