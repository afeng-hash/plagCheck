package com.afeng.plagchenckpro.common.utils;

import com.afeng.plagchenckpro.algorithm.LSHBucketCalculator;
import com.afeng.plagchenckpro.algorithm.MinHashGenerator;
import com.afeng.plagchenckpro.entity.pojo.Sentence;
import com.afeng.plagchenckpro.entity.pojo.Word;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本预处理工具类
 * 使用HanLP进行中文分词，实现文本清洗、分词、分句等功能
 *
 * @author afeng
 */
@Slf4j
public class TextProcess {

    /** MinHash生成器 */
    private static MinHashGenerator minHashGenerator = new MinHashGenerator();

    /** LSH桶计算器 */
    private static LSHBucketCalculator lshBucketCalculator = new LSHBucketCalculator();

    /** 停用词集合 */
    private static Set<String> stopWords = initializeStopWords();

    /** 句子分割正则表达式 */
    private static final Pattern SENTENCE_PATTERN = Pattern.compile(
            "[。！？；\\.!?;]\\s*"
    );

    /** 文本清洗正则表达式 */
    private static final Pattern CLEAN_PATTERN = Pattern.compile(
            "[\\s\\r\\n\\t]+|[\u00A0\u2000-\u200F\u2028-\u202F\u205F-\u206F\u3000\uFEFF]+"
    );

    /** 特殊字符正则表达式 */
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile(
            "[\\p{Punct}&&[^。！？；\\.!?;]]+"
    );


    /**
     * 清洗文本内容
     * 去除特殊字符、多余空格、统一编码等
     *
     * @param text 原始文本
     * @return 清洗后的文本
     */
//    public static String cleanText(String text) {
//        if (StringUtils.isBlank(text)) {
//            return "";
//        }
//
//        // 去除多余的空白字符
//        String cleaned = CLEAN_PATTERN.matcher(text).replaceAll(" ");
//
//        // 去除特殊字符（保留句号、问号、感叹号等）
//        cleaned = SPECIAL_CHARS_PATTERN.matcher(cleaned).replaceAll(" ");
//
//        // 去除多余空格
//        cleaned = cleaned.replaceAll("\\s+", " ");
//
//        // 去除首尾空格
//        cleaned = cleaned.trim();
//
//        log.debug("文本清洗完成，原始长度: {}, 清洗后长度: {}", text.length(), cleaned.length());
//        return cleaned;
//    }

    /**
     * 将文本分割成句子
     * 基于标点符号进行句子分割
     *
     * @param text 文本内容
     * @return 句子列表
     */
//    public static List<String> splitIntoSentences(String text) {
//        if (StringUtils.isBlank(text)) {
//            return new ArrayList<>();
//        }
//
//        // 清洗文本
//        String cleanedText = cleanText(text);
//
//        // 使用正则表达式分割句子
//        String[] sentences = SENTENCE_PATTERN.split(cleanedText);
//
//        List<String> result = new ArrayList<>();
//        for (String sentence : sentences) {
//            String trimmed = processSpacesBasedOnEnglish(sentence);
//            if(!containsChineseOrEnglish(trimmed)){
//                continue;
//            }
//            if (StringUtils.isNotBlank(trimmed) && trimmed.length() > 2) {
//                result.add(trimmed);
//            }
//        }
//
//        log.debug("句子分割完成，共分割出 {} 个句子", result.size());
//        return result;
//    }

    /**
     * 检测文本是否包含英文或者中文
     * @param text
     * @return
     */
    public static boolean containsChineseOrEnglish(String text) {
        // 匹配中文或英文
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5a-zA-Z]");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }


    /**
     * 如果字符串包含英文字母，则保留空格；否则去除所有空格
     * @param input 输入字符串
     * @return 处理后的字符串
     */
    public static String processSpacesBasedOnEnglish(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 检查是否包含至少一个英文字母
        boolean hasEnglish = input.matches(".*[a-zA-Z].*");

        if (!hasEnglish) {
            return StringUtils.deleteWhitespace(input); // 无英文时去除空格
        } else {
            return input; // 有英文时保留空格
        }
    }


    /**
     * 对句子进行分词
     * 使用HanLP进行中文分词
     *
     * @param sentence 句子文本
     * @return 词列表
     */
    public static List<String> segmentWords(String sentence) {
        if (StringUtils.isBlank(sentence)) {
            return new ArrayList<>();
        }

        try {
            // 使用HanLP进行分词
            List<Term> terms = HanLP.segment(sentence);

            List<String> words = new ArrayList<>();

            for (Term term : terms) {
                String word = term.word.trim();
                // 过滤掉长度小于2的词和标点符号
                if (word.length() >= 2 && !isPunctuation(word)) {
                    words.add(word);
                }
            }

            log.debug("分词完成，句子: {}, 词数: {}", sentence, words.size());
            return words;

        } catch (Exception e) {
            log.error("分词失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 判断是否为标点符号
     */
    private static boolean isPunctuation(String word) {
        return word.matches("\\p{Punct}+");
    }

    /**
     * 过滤停用词
     * 移除停用词，保留有意义的词
     *
     * @param words 词列表
     * @return 过滤后的词列表
     */
    public static List<String> filterStopWords(List<String> words) {
        if (words == null || words.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> filtered = words.stream()
                .filter(StringUtils::isNotBlank)
                .filter(word -> !isStopWord(word))
                .collect(Collectors.toList());

        log.debug("停用词过滤完成，原始词数: {}, 过滤后词数: {}", words.size(), filtered.size());
        return filtered;
    }

    /**
     * 检查词是否为停用词
     *
     * @param word 词
     * @return 是否为停用词
     */
    public static boolean isStopWord(String word) {
        if (StringUtils.isBlank(word)) {
            return true;
        }

        String lowerWord = word.toLowerCase().trim();

        // 检查是否为停用词
        if (stopWords.contains(lowerWord)) {
            return true;
        }

        // 检查是否为单字符（除了中文字符）
        if (lowerWord.length() == 1 && !isChinese(lowerWord.charAt(0))) {
            return true;
        }

        // 检查是否为纯数字
        if (lowerWord.matches("\\d+")) {
            return true;
        }

        return false;
    }

    /**
     * 处理论文文本，生成句子和词数据
     * 包含完整的文本预处理流程
     *
     * @param paperId 论文ID
     * @param content 论文内容
     * @return 处理后的句子列表
     */
    public static List<Sentence> processPaperText(Long paperId, String content) {
        if (paperId == null || StringUtils.isBlank(content)) {
            return new ArrayList<>();
        }

        log.info("开始处理论文文本，论文ID: {}, 内容长度: {}", paperId, content.length());

        // 分割句子
        List<String> sentenceTexts = splitIntoSentences(content);

        List<Sentence> sentences = new ArrayList<>();
        for (int i = 0; i < sentenceTexts.size(); i++) {
            String sentenceText = sentenceTexts.get(i);

            // 创建句子对象
            Sentence sentence = new Sentence();
            sentence.setSentenceText(sentenceText);
            sentence.setPaperId(paperId);
            sentence.setSentenceIndex(i);

            // 分词
            List<String> words = segmentWords(sentenceText);
            sentence.setWordCount(words.size());

            // 过滤停用词
            List<String> filteredWords = filterStopWords(words);

            // 生成MinHash签名
            String minhashSignature = generateMinhashSignature(filteredWords);
            if(minhashSignature==null || minhashSignature.isEmpty()){
                continue;
            }
            sentence.setMinhashSignature(minhashSignature);

            // 生成LSH桶标识
            String lshBucket = generateLSHBucket(minhashSignature);
            if(lshBucket==null || lshBucket.isEmpty()){
                continue;
            }
            sentence.setLshBucket(lshBucket);

            sentences.add(sentence);
        }

        log.info("论文文本处理完成，共生成 {} 个句子", sentences.size());
        return sentences;
    }

    /**
     * 处理单个句子，生成词数据
     *
     * @param sentenceId 句子ID
     * @param sentenceText 句子文本
     * @return 处理后的词列表
     */
    public static List<Word> processSentence(Long sentenceId, String sentenceText) {
        if (sentenceId == null || StringUtils.isBlank(sentenceText)) {
            return new ArrayList<>();
        }

        // 分词
        List<String> words = segmentWords(sentenceText);

        List<Word> wordList = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String wordText = words.get(i);
            boolean isStopWord = isStopWord(wordText);

            Word word = new Word(sentenceId, wordText, i, isStopWord);
            wordList.add(word);
        }

        return wordList;
    }

    /**
     * 生成句子的MinHash签名
     *
     * @param words 词列表
     * @return MinHash签名
     */
    public static String generateMinhashSignature(List<String> words) {
        if (words == null || words.isEmpty()) {
            return "";
        }

        Set<String> wordSet = new HashSet<>(words);
        return minHashGenerator.generateMinHash(wordSet);
    }

    /**
     * 生成句子的LSH桶标识
     *
     * @param minhashSignature MinHash签名
     * @return LSH桶标识
     */
    public static String generateLSHBucket(String minhashSignature) {
        if (StringUtils.isBlank(minhashSignature)) {
            return "";
        }

        return lshBucketCalculator.calculateBucket(minhashSignature);
    }

    /**
     * 计算两个句子的相似度
     * 使用余弦相似度算法
     *
     * @param words1 第一个句子的词列表
     * @param words2 第二个句子的词列表
     * @return 相似度 (0.0-1.0)
     */
    public static double calculateSimilarity(List<String> words1, List<String> words2) {
        if (words1 == null || words2 == null || words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }

        // 转换为词集合
        Set<String> set1 = new HashSet<>(words1);
        Set<String> set2 = new HashSet<>(words2);

        // 计算交集
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // 计算并集
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        // 计算Jaccard相似度
        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * 获取MinHash生成器实例
     *
     * @return MinHash生成器
     */
    public static MinHashGenerator getMinHashGenerator() {
        return minHashGenerator;
    }

    /**
     * 获取LSH桶计算器实例
     *
     * @return LSH桶计算器
     */
    public static LSHBucketCalculator getLSHBucketCalculator() {
        return lshBucketCalculator;
    }

    /**
     * 初始化停用词集合
     *
     * @return 停用词集合
     */
    private static Set<String> initializeStopWords() {
        Set<String> stopWords = new HashSet<>();

        // 添加常用停用词
        String[] commonStopWords = {
                "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这", "那", "个", "他", "她", "它", "们", "我们", "你们", "他们", "她们", "它们",
                "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "will", "with", "the", "this", "these", "those", "they", "them", "their", "there", "then", "than", "or", "but", "if", "when", "where", "why", "how", "what", "who", "which", "whose", "whom"
        };

        for (String word : commonStopWords) {
            stopWords.add(word.toLowerCase());
        }

        log.info("停用词初始化完成，共加载 {} 个停用词", stopWords.size());
        return stopWords;
    }

    /**
     * 判断字符是否为中文字符
     *
     * @param c 字符
     * @return 是否为中文字符
     */
    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FFF;
    }






    // 在 TextProcess 类中添加综合相似度计算方法
    public static double calculateComprehensiveSimilarity(String sentence1, String sentence2) {
        // 词法相似度（编辑距离）
        double lexicalSim = calculateEditDistanceSimilarity(sentence1, sentence2);

        // Jaccard相似度（基于分词）
        List<String> words1 = segmentWords(sentence1);
        List<String> words2 = segmentWords(sentence2);
        double jaccardSim = calculateSimilarity(words1, words2);

        // 长度相似度（避免长度差异过大的误判）
        double lengthSim = calculateLengthSimilarity(sentence1, sentence2);

        // 综合评分（可根据实际效果调整权重）
        return 0.3 * lexicalSim + 0.5 * jaccardSim + 0.2 * lengthSim;
    }

    private static double calculateEditDistanceSimilarity(String s1, String s2) {
        int distance = editDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }

    private static double calculateLengthSimilarity(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 && len2 == 0) return 1.0;
        if (len1 == 0 || len2 == 0) return 0.0;
        return 1.0 - (double) Math.abs(len1 - len2) / Math.max(len1, len2);
    }

    private static int editDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i-1][j-1] + (s1.charAt(i-1) == s2.charAt(j-1) ? 0 : 1),
                            dp[i-1][j] + 1,
                            dp[i][j-1] + 1);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * 增强文本清洗
     */
    public static String cleanText(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        // 去除多余空白字符
        String cleaned = CLEAN_PATTERN.matcher(text).replaceAll(" ");

        // 去除特殊字符
        cleaned = SPECIAL_CHARS_PATTERN.matcher(cleaned).replaceAll(" ");

        // 去除引用标记
        cleaned = cleaned.replaceAll("\\[\\d+\\]", "");

        // 去除多余空格
        cleaned = cleaned.replaceAll("\\s+", " ");

        // 去除首尾空格
        cleaned = cleaned.trim();

        return cleaned;
    }

    /**
     * 改进句子分割
     */
    public static List<String> splitIntoSentences(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }

        String cleanedText = cleanText(text);
        String[] sentences = SENTENCE_PATTERN.split(cleanedText);

        List<String> result = new ArrayList<>();
        for (String sentence : sentences) {
            String trimmed = processSpacesBasedOnEnglish(sentence);

            // 增强有效性检查
            if (containsChineseOrEnglish(trimmed) && isValidSentence(trimmed)) {
                result.add(trimmed);
            }
        }

        return result;
    }

    /**
     * 检查句子有效性
     */
    public static boolean isValidSentence(String sentence) {
        if (StringUtils.isBlank(sentence) || sentence.length() < 5) {
            return false;
        }

        // 过滤过短的句子
        if (sentence.length() < 8) {
            return false;
        }

        // 过滤特定模板句子
        String lowerSentence = sentence.toLowerCase();
        String[] excludePatterns = {
                "宁波工程", "毕业设计", "论文题目", "学院名称",
                "专业", "班级", "姓名", "学号", "指导教师"
        };

        for (String pattern : excludePatterns) {
            if (lowerSentence.contains(pattern)) {
                return false;
            }
        }

        // 过滤纯数字句子
        if (sentence.matches("^\\d+$")) {
            return false;
        }

        // 过滤过短的有效字符
        long validCharCount = sentence.chars()
                .filter(c -> (c >= 0x4E00 && c <= 0x9FFF) || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
                .count();

        return validCharCount >= 3;
    }




    /**
     * 计算TF-IDF向量
     */
    public static Map<String, Double> calculateTFIDF(List<String> sentences, String targetSentence) {
        Map<String, Double> tfidf = new HashMap<>();

        // 简化的TF-IDF计算
        List<String> words = segmentWords(targetSentence);
        Map<String, Integer> wordCount = new HashMap<>();

        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }

        int totalWords = words.size();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            // 简化的TF计算
            double tf = (double) count / totalWords;
            // 简化的IDF计算（实际应用中需要基于整个语料库）
            double idf = Math.log((double) sentences.size() / 1);
            tfidf.put(word, tf * idf);
        }

        return tfidf;
    }

    /**
     * 提取关键词
     */
    public static List<String> extractKeywords(String sentence, int topN) {
        List<String> words = segmentWords(sentence);
        words = filterStopWords(words);

        // 简单的关键词提取：基于词频
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : words) {
            wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
        }

        return wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
