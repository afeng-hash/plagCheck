package com.afeng.plagchenckpro.service.impl;

import com.afeng.plagchenckpro.entity.pojo.Paper;
import com.afeng.plagchenckpro.entity.pojo.Sentence;
import com.afeng.plagchenckpro.entity.vo.CheckResult;
import com.afeng.plagchenckpro.entity.vo.PaperVo;
import com.afeng.plagchenckpro.entity.vo.SimilarResult;
import com.afeng.plagchenckpro.entity.vo.SimilarSentence;
import com.afeng.plagchenckpro.mapper.PaperMapper;
import com.afeng.plagchenckpro.mapper.SentenceMapper;
import com.afeng.plagchenckpro.service.DocumentParseService;
import com.afeng.plagchenckpro.service.PlagiarismCheckService;
import com.afeng.plagchenckpro.common.utils.ChineseTextSimilarity;
import com.afeng.plagchenckpro.common.utils.TextProcess;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 高效查重服务实现类
 * 使用LSH预筛选和分批处理，避免内存溢出，提升查重效率
 *
 * @author afeng
 */
@Service
@Slf4j
public class PlagiarismCheckServiceImpl implements PlagiarismCheckService {

    /** 批处理大小，避免内存溢出 */
    private static final int BATCH_SIZE = 100;

    @Autowired
    private DocumentParseService documentParseService;

    @Autowired
    private ChineseTextSimilarity chineseTextSimilarity;

    @Autowired
    private SentenceMapper sentenceMapper;

    @Autowired
    private PaperMapper paperMapper;

    // 相似度缓存（带过期时间）
    private final Map<String, CacheEntry> similarityCache = new ConcurrentHashMap<>();

    // 缓存过期时间（毫秒）
    private static final long CACHE_EXPIRE_TIME = 30 * 60 * 1000; // 30分钟

    // 缓存最大大小
    private static final int MAX_CACHE_SIZE = 10000;


    /**
     * 查重
     * @param file 待检测的论文文件
     * @param threshold 相似度阈值 (0.0-1.0)
     * @return
     * @throws IOException
     */
    @Override
    public CheckResult checkPlagiarism(MultipartFile file, double threshold) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("相似度阈值必须在0.0-1.0之间");
        }

        log.info("开始高效查重检测，文件名: {}, 阈值: {}", file.getOriginalFilename(), threshold);

        try {
            // 1. 解析文档内容
            String content = documentParseService.parseDocument(file);
            if (content == null || content.trim().isEmpty()) {
                throw new IOException("文档解析失败，无法提取文本内容");
            }

            // 2. 分割句子
            List<String> sentences = TextProcess.splitIntoSentences(content);

            if (sentences.isEmpty()) {
                return new CheckResult(0.0, new ArrayList<>(), null,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),"优秀", null);
            }

            // 3. 高效查重检测
            List<SimilarSentence> allSimilarSentences = new ArrayList<>();
            int totalSentences = sentences.size();
            int similarCount = 0;
            //比对错误的句子
            long errorCount = 0;

            //并行处理句子
            List<CompletableFuture<SimilarResult>> futures = new ArrayList<>();

            for (int i = 0; i < sentences.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, sentences.size());
                List<String> batchSentences = sentences.subList(i, endIndex);

                int finalI = i;
                CompletableFuture<SimilarResult> future = CompletableFuture
                        .supplyAsync(() -> {
                            log.debug("处理句子批次: {}-{}", finalI, endIndex - 1);
                            return processBatchSentences(batchSentences);
                        });

                futures.add(future);
            }

            // 等待所有任务完成并收集结果
            List<SimilarResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            // 合并结果
            for (SimilarResult result : results) {
                errorCount += result.getCount();
                if (!result.getList().isEmpty()) {
                    allSimilarSentences.addAll(result.getList());
                }
                similarCount += result.getList().size();
            }

            log.error("句子数量：{}",totalSentences);

            // 7. 计算查重率
            totalSentences -= errorCount;
            double similarityRate = totalSentences > 0 ? (double) similarCount / totalSentences : 0.0;
            BigDecimal bd = new BigDecimal(similarityRate);
            bd = bd.setScale(2, RoundingMode.HALF_UP); // 四舍五入保留两位
            similarityRate = bd.doubleValue();

            //计算标准
            String strand = "";
            if(similarityRate <= 0.15){
                strand = "优秀";
            }else if(similarityRate <= 0.3){
                strand = "合格";
            }else if(similarityRate<=0.5){
                strand = "轻度抄袭";
            }else{
                strand = "重度抄袭";
            }

            // 8. 按相似度排序
            allSimilarSentences.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

            // 统计来源论文ID出现次数，找出前三个次数最高的来源论文ID
            Map<Long, Long> paperIdCountMap = allSimilarSentences.stream()
                    .filter(sentence -> sentence.getSourcePaperId() != null)
                    .collect(Collectors.groupingBy(SimilarSentence::getSourcePaperId, Collectors.counting()));

            List<Long> topThreePaperIds = paperIdCountMap.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            log.info("前三个高重复论文ID: {}", topThreePaperIds);

            //查询数据库找出论文信息
            List<PaperVo> paperVos;
            if (!topThreePaperIds.isEmpty()) {
                List<Paper> papers = paperMapper.selectList(new LambdaQueryWrapper<Paper>()
                        .select(Paper::getId, Paper::getTitle, Paper::getAuthor, Paper::getFilePath, Paper::getFileType, Paper::getUploadTime, Paper::getWordCount)
                        .in(Paper::getId, topThreePaperIds));

                paperVos = papers.stream().map(paper -> {
                    PaperVo paperVo = new PaperVo();
                    BeanUtils.copyProperties(paper, paperVo);
                    return paperVo;
                }).collect(Collectors.toList());

            }else{
                paperVos = new ArrayList<>();
            }

            // 9. 创建查重结果
            CheckResult result = new CheckResult(
                    similarityRate,
                    allSimilarSentences,
                    null,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    strand,
                    paperVos
            );

            log.info("高效查重检测完成，查重率: {}, 相似句子数: {},处理错误的句子:{}",
                    similarityRate * 100, allSimilarSentences.size(),errorCount);

            // 清理过期缓存
            cleanupExpiredCache();

            return result;

        } catch (Exception e) {
            log.error("高效查重检测失败: {}", e.getMessage(), e);
            throw new IOException("查重检测失败: " + e.getMessage(), e);
        }
    }

    //对候选句子进行精确相似度计算
    private SimilarResult processBatchSentences(List<String> sentences) {
        List<SimilarSentence> similarSentences = new ArrayList<>();
        long total = 0;

        try {
            // 批量预处理：一次性获取所有需要的候选句子
            Map<String, List<Sentence>> candidateMap = new HashMap<>();
            List<String> validSentences = new ArrayList<>();

            // 第一阶段：筛选有效句子并预获取候选句子
            for (String currentSentence : sentences) {
                if (!TextProcess.isValidSentence(currentSentence)) {
                    total++;
                    continue;
                }
                validSentences.add(currentSentence);
                candidateMap.put(currentSentence, getCandidatesByLSH(currentSentence));
            }

            // 批量获取所有候选句子相关的论文信息
            Set<Long> paperIds = candidateMap.values().stream()
                    .flatMap(List::stream)
                    .map(Sentence::getPaperId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, Paper> paperMap = new HashMap<>();
            if (!paperIds.isEmpty()) {
                // 批量查询论文信息，避免多次单独查询
                List<Paper> papers = paperMapper.selectBatchIds(paperIds);
                paperMap = papers.stream()
                        .collect(Collectors.toMap(Paper::getId, paper -> paper));
            }

            // 第二阶段：批量处理有效句子
            for (String currentSentence : validSentences) {
                List<Sentence> candidateSentences = candidateMap.get(currentSentence);

                if (candidateSentences.isEmpty()) {
                    continue;
                }

                // 语义哈希筛选
                candidateSentences = filterBySemanticHash(candidateSentences, currentSentence);

                if (candidateSentences.isEmpty()) {
                    continue;
                }

                // 精确相似度计算
                SimilarSentence bestMatch = findBestMatch(currentSentence, candidateSentences, paperMap);

                if (bestMatch != null) {
                    similarSentences.add(bestMatch);
                }
            }

        } catch (Exception e) {
            log.error("处理句子批次失败: {}", e.getMessage(), e);
        }

        return new SimilarResult(similarSentences, total);
    }

    /**
     * 基于LSH获取候选句子
     */
    private List<Sentence> getCandidatesByLSH(String sentence) {
        List<String> words = TextProcess.segmentWords(sentence);
        List<String> filteredWords = TextProcess.filterStopWords(words);
        String minhashSignature = TextProcess.generateMinhashSignature(filteredWords);
        String lshBucket = TextProcess.generateLSHBucket(minhashSignature);

        if (lshBucket == null || lshBucket.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Sentence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sentence::getLshBucket, lshBucket);
        return sentenceMapper.selectList(queryWrapper);
    }

    /**
     * 基于语义哈希进一步筛选候选句子
     */
    private List<Sentence> filterBySemanticHash(List<Sentence> candidates, String sentence) {
        // 生成当前句子的语义哈希
        String currentSemanticHash = generateSemanticHash(sentence);

        return candidates.stream()
                .filter(candidate -> {
                    // 如果候选句子没有语义哈希，则保留用于进一步计算
                    if (candidate.getSemanticHash() == null || candidate.getSemanticHash().isEmpty()) {
                        return true;
                    }
                    // 简单的哈希匹配，可以扩展为更复杂的相似度计算
                    return Math.abs(candidate.getSemanticHash().hashCode() - currentSemanticHash.hashCode()) < 1000;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成语义哈希
     */
    private String generateSemanticHash(String sentence) {
        INDArray vector = chineseTextSimilarity.getSentenceVector(sentence);
        if (vector == null) {
            return String.valueOf(sentence.hashCode());
        }
        // 简化的语义哈希生成
        return String.valueOf(Arrays.hashCode(vector.toDoubleVector()));
    }

    /**
     * 查找最佳匹配句子（优化版本）
     */
    private SimilarSentence findBestMatch(String currentSentence, List<Sentence> candidates, Map<Long, Paper> paperMap) {
        SimilarSentence bestMatch = null;
        double bestSimilarity = 0.0;
        double dynamicThreshold = calculateDynamicThreshold(currentSentence);

        for (Sentence candidateSentence : candidates) {
            // 检查缓存
            String cacheKey = generateCacheKey(currentSentence, candidateSentence.getSentenceText());
            CacheEntry cacheEntry = similarityCache.get(cacheKey);

            if (cacheEntry != null && !cacheEntry.isExpired()) {
                double similarity = cacheEntry.getSimilarity();
                if (similarity > bestSimilarity && similarity >= dynamicThreshold) {
                    bestSimilarity = similarity;
                    bestMatch = createSimilarSentence(currentSentence, candidateSentence, similarity, paperMap);
                }
                continue;
            }

            // 计算综合相似度
            double similarity = chineseTextSimilarity.calculateComprehensiveSimilarity(
                    currentSentence, candidateSentence.getSentenceText());

            BigDecimal bd = new BigDecimal(similarity);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            similarity = bd.doubleValue();

            // 缓存结果（带过期时间）
            if (similarityCache.size() < MAX_CACHE_SIZE) {
                similarityCache.put(cacheKey, new CacheEntry(similarity, System.currentTimeMillis() + CACHE_EXPIRE_TIME));
            }

            if (similarity >= dynamicThreshold && similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = createSimilarSentence(currentSentence, candidateSentence, similarity, paperMap);
            }
        }

        return bestMatch;
    }

    /**
     * 创建相似句子对象（优化版本）
     */
    private SimilarSentence createSimilarSentence(String currentSentence, Sentence candidateSentence,
                                                  double similarity, Map<Long, Paper> paperMap) {
        Paper paper = null;
        if (candidateSentence.getPaperId() != null) {
            paper = paperMap.get(candidateSentence.getPaperId());
        }

        return new SimilarSentence(
                currentSentence,
                candidateSentence.getSentenceText(),
                similarity,
                candidateSentence.getPaperId(),
                (paper != null && paper.getTitle() != null) ? paper.getTitle() : "未知",
                (paper != null && paper.getAuthor() != null) ? paper.getAuthor() : "未知"
        );
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String s1, String s2) {
        return (s1.hashCode() + "_" + s2.hashCode()).intern();
    }

    /**
     * 根据句子特征计算动态阈值
     */
    private double calculateDynamicThreshold(String sentence) {
        if (sentence == null || sentence.isEmpty()) {
            return 0.8;
        }

        int length = sentence.length();

        // 短句需要更高的相似度阈值
        if (length < 15) {
            return 0.9; // 短句阈值提高
        } else if (length > 100) {
            return 0.7; // 长句阈值降低
        }

        // 中等长度句子使用默认阈值
        return 0.8;
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        long currentTime = System.currentTimeMillis();
        similarityCache.entrySet().removeIf(entry ->
                entry.getValue().isExpired());
    }

    /**
     * 缓存条目类（带过期时间）
     */
    private static class CacheEntry {
        @Getter
        private final double similarity;
        private final long expireTime;

        public CacheEntry(double similarity, long expireTime) {
            this.similarity = similarity;
            this.expireTime = expireTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
