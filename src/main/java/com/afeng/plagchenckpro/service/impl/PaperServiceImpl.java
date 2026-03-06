package com.afeng.plagchenckpro.service.impl;

import com.afeng.plagchenckpro.common.reuslt.PageResult;
import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.common.reuslt.ResultCodeEnum;
import com.afeng.plagchenckpro.common.utils.ChineseTextSimilarity;
import com.afeng.plagchenckpro.entity.dto.PaperDto;
import com.afeng.plagchenckpro.entity.pojo.Paper;
import com.afeng.plagchenckpro.entity.pojo.Sentence;
import com.afeng.plagchenckpro.entity.vo.PaperVo;
import com.afeng.plagchenckpro.mapper.PaperMapper;
import com.afeng.plagchenckpro.service.*;
import com.afeng.plagchenckpro.common.utils.TextProcess;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private DocumentParseService documentParseService;

    @Autowired
    private SentenceService sentenceService;
    @Autowired
    private ChineseTextSimilarity chineseTextSimilarity;

    // 用于JSON序列化
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FileService fileService;


    /**
     * 上传论文到论文库
     * @param file
     * @param title
     * @param author
     * @return
     */
    @Override
    public Paper uploadPaper(MultipartFile file, String title, String author) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        //保存到minio
        String filePath = "";
        try {
            filePath = fileService.uploadPdfFile(file);
        }catch (Exception e){
            log.error("上传文件失败: {}", e.getMessage());
            throw new IOException("上传文件失败");
        }

        boolean flag = false;
        if (StringUtils.isBlank(title)) {
            flag = true;
        }

        log.info("开始上传论文，文件名: {}, 标题: {}, 作者: {}",
                file.getOriginalFilename(), title, author);

        try {
            // 1. 解析文档内容
            String content = documentParseService.parseDocument(file);
            if (StringUtils.isBlank(content)) {
                throw new IOException("文档解析失败，无法提取文本内容");
            }

            // 2. 创建论文对象
            Paper paper = new Paper();
            if(flag){
                Map<String, String> map = extractPaperInfo(content);
                if (StringUtils.isBlank(map.get("title"))){
                    log.error("无法从文档中提取标题");
                    throw new IOException("无法从文档中提取标题，请尝试单独上传");
                }else {
                    paper.setTitle(map.get("title"));
                }
                if (StringUtils.isBlank(map.get("author"))){
                    log.error("无法从文档中提取作者");
                    throw new IOException("无法从文档中提取作者，请尝试单独上传");
                }else {
                    paper.setAuthor(map.get("author"));
                }

            }else{
                paper.setTitle(title);
                paper.setAuthor(author);
            }
            paper.setFilePath(file.getOriginalFilename());
            paper.setFileType(documentParseService.getFileType(file));
            paper.setUploadTime(LocalDateTime.now());
            paper.setWordCount(content.length());
            paper.setFilePath(filePath);

            // 3. 生成增强字段
            enhancePaperFields(paper, content);

            // 4. 保存论文到数据库
            paperMapper.insert(paper);
            log.info("论文保存成功，ID: {}", paper.getId());

            // 5. 处理论文内容（分词、分句、生成签名等）
            List<Sentence> sentences = processPaperContent(paper.getId(), content, false);
            log.info("论文内容处理完成，生成 {} 个句子", sentences.size());

            // 6. 生成论文的MinHash签名
            List<String> list = TextProcess.filterStopWords(
                    TextProcess.segmentWords(content));
            String paperMinhash = TextProcess.getMinHashGenerator()
                    .generateMinHash(new HashSet<>(list));
            paper.setMinhashSignature(paperMinhash);

            // 7. 更新论文的MinHash签名和其他增强字段
            if (paper.getId() == null) {
                throw new IllegalArgumentException("论文对象或ID不能为空");
            }
            paperMapper.updateById(paper);

            log.info("论文上传完成，ID: {}, 标题: {}", paper.getId(), paper.getTitle());
            return paper;

        } catch (Exception e) {
            log.error("论文上传失败: {}", e.getMessage(), e);
            throw new IOException("论文上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 增强论文字段信息
     * @param paper 论文对象
     * @param content 论文内容
     */
    private void enhancePaperFields(Paper paper, String content) {
        try {
            // 1. 生成TF-IDF向量（简化版）
            Map<String, Double> tfidfVector = generateTFIDFVector(content);
            paper.setTfIdfVector(objectMapper.writeValueAsString(tfidfVector));

            // 2. 提取关键词
            List<String> keywords = extractKeywords(content, 10);
            paper.setKeywords(objectMapper.writeValueAsString(keywords));

            // 3. 设置论文分类（可以根据内容特征进行分类）
            paper.setCategory(determinePaperCategory(content));

            // 4. 计算文本复杂度评分
            double complexityScore = calculateComplexityScore(content);
            paper.setComplexityScore(complexityScore);

        } catch (Exception e) {
            log.warn("增强论文字段信息时出错: {}", e.getMessage());
        }
    }

    /**
     * 生成TF-IDF向量
     * @param content 论文内容
     * @return TF-IDF向量
     */
    private Map<String, Double> generateTFIDFVector(String content) {
        // 简化的TF-IDF计算
        List<String> words = TextProcess.segmentWords(content);
        words = TextProcess.filterStopWords(words);

        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }

        Map<String, Double> tfidf = new HashMap<>();
        int totalWords = words.size();

        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            // 简化的TF计算
            double tf = (double) count / totalWords;
            // 简化的IDF计算（实际应用中需要基于整个语料库）
            double idf = Math.log(100.0 / 1); // 假设语料库大小为100
            tfidf.put(word, tf * idf);
        }

        return tfidf;
    }

    /**
     * 提取关键词
     * @param content 论文内容
     * @param topN 前N个关键词
     * @return 关键词列表
     */
    private List<String> extractKeywords(String content, int topN) {
        List<String> words = TextProcess.segmentWords(content);
        words = TextProcess.filterStopWords(words);

        // 基于词频提取关键词
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

    /**
     * 确定论文分类
     * @param content 论文内容
     * @return 分类标签
     */
    private String determinePaperCategory(String content) {
        // 基于关键词的简单分类
        String lowerContent = content.toLowerCase();

        if (lowerContent.contains("计算机") || lowerContent.contains("软件") ||
                lowerContent.contains("算法") || lowerContent.contains("数据")) {
            return "计算机科学";
        } else if (lowerContent.contains("经济") || lowerContent.contains("金融") ||
                lowerContent.contains("管理") || lowerContent.contains("市场")) {
            return "经济学";
        } else if (lowerContent.contains("生物") || lowerContent.contains("医学") ||
                lowerContent.contains("化学") || lowerContent.contains("物理")) {
            return "自然科学";
        } else if (lowerContent.contains("教育") || lowerContent.contains("教学") ||
                lowerContent.contains("学习")) {
            return "教育学";
        } else {
            return "其他";
        }
    }

    /**
     * 计算文本复杂度评分
     * @param content 论文内容
     * @return 复杂度评分
     */
    private double calculateComplexityScore(String content) {
        // 简化的复杂度评分计算
        List<String> sentences = TextProcess.splitIntoSentences(content);
        if (sentences.isEmpty()) {
            return 0.0;
        }

        // 平均句子长度
        double avgSentenceLength = sentences.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);

        // 词汇丰富度（不同词汇数/总词汇数）
        List<String> words = TextProcess.segmentWords(content);
        Set<String> uniqueWords = new HashSet<>(words);
        double vocabularyRichness = words.isEmpty() ? 0.0 : (double) uniqueWords.size() / words.size();

        // 综合评分（可以根据实际效果调整权重）
        return 0.6 * (avgSentenceLength / 100.0) + 0.4 * vocabularyRichness;
    }




    /**
     * 条件查询
     * @param paperDto
     * @return
     */
    @Override
    public Result getList(PaperDto paperDto) {
        //参数校验
        if(paperDto.getPageNum()==null || paperDto.getPageSize()==null){
            return Result.build(null, ResultCodeEnum.ARGUMENT_VALID_ERROR);
        }
        if(paperDto.getPageNum()<0 || paperDto.getPageNum()>100){
            paperDto.setPageNum(1);
        }
        if(paperDto.getPageSize()<0 || paperDto.getPageSize()>100){
            paperDto.setPageSize(10);
        }

        // 创建分页对象
        Page<Paper> page = new Page<>(paperDto.getPageNum(), paperDto.getPageSize());
        // 构建查询条件
        LambdaQueryWrapper<Paper> queryWrapper = new LambdaQueryWrapper<>();
        String author = paperDto.getAuthor();
        String title = paperDto.getTitle();
        queryWrapper.like(author!=null &&!author.isEmpty(),Paper::getAuthor,author);
        queryWrapper.like(title!=null&&!title.isEmpty(),Paper::getTitle,title);

        Page<Paper> paperPage = paperMapper.selectPage(page, queryWrapper);
        List<PaperVo> paperVos = paperPage.getRecords().stream()
                .map(paper -> {
                    PaperVo vo = new PaperVo();
                    BeanUtils.copyProperties(paper, vo);
                    return vo;
                })
                .collect(Collectors.toList());


        PageResult<PaperVo> paperVoPageResult = new PageResult<>(
                (int) paperPage.getCurrent(),
                (int) paperPage.getSize(),
                paperPage.getTotal(),
                paperVos);

        return Result.ok(paperVoPageResult);
    }


    /**
     * 根据id删除
     * @param id
     * @return
     */
    @Override
    public Result delete(Long id) {
        if(id==null){
            return Result.build(null,ResultCodeEnum.ARGUMENT_VALID_ERROR);
        }

        Paper paper = paperMapper.selectById(id);
        if(paper==null){
            return Result.ok();
        }

        if(paper.getFilePath() != null){
            try {
                //删除文件
                fileService.deleteFileByUrl(paper.getFilePath());
            }catch (Exception e){
                log.info("删除文件失败:{}", e);
//                throw new RuntimeException(e);
            }
        }

        paperMapper.deleteById(id);
        //删除句子
        LambdaQueryWrapper<Sentence> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Sentence::getPaperId,id);
        sentenceService.remove(lambdaQueryWrapper);

        return Result.ok();
    }




    /**
     * 处理论文文本内容
     * 包含分词、分句、生成MinHash签名等处理
     *
     * @param paperId 论文ID
     * @param content 论文内容
     * @return 处理后的句子列表
     */
    public List<Sentence> processPaperContent(Long paperId, String content,Boolean flag) throws Exception {
        if (paperId == null || StringUtils.isBlank(content)) {
            return List.of();
        }

        try {
            // 1. 处理论文文本，生成句子数据（包含增强字段）
            List<Sentence> sentences = processPaperTextWithEnhancement(paperId, content);

            // 2. 批量保存句子到数据库
            if (!sentences.isEmpty()) {
                //批量保存
                sentenceService.saveBatch(sentences);
            }

            if(flag){
                //处理论文标题,处理前四个句子
                int num = Math.min(sentences.size(), 8);
                String title = "";
                String auth = "";
                for(int i=0;i<num;i++){
                    Map<String, String> map = extractInfo(sentences.get(i).getSentenceText());
                    if(map.get("title") != null){
                        title = map.get("title");
                    }
                    if(map.get("author") != null){
                        auth = map.get("author");
                    }
                    if(!title.isEmpty() && !auth.isEmpty()){
                        //修改论文的标题与作者
                        Paper paper = new Paper();
                        paper.setAuthor(auth);
                        paper.setTitle(title);
                        paper.setId(paperId);
                        paperMapper.updateById(paper);
                        break;
                    }
                }
            }

            log.info("论文内容处理完成，论文ID: {}, 句子数: {}", paperId, sentences.size());
            return sentences;

        } catch (Exception e) {
            log.error("处理论文内容失败，论文ID: {}, 错误: {}", paperId, e.getMessage(), e);
            throw new Exception(e);
        }
    }

    /**
     * 处理论文文本并增强句子字段
     * @param paperId 论文ID
     * @param content 论文内容
     * @return 增强后的句子列表
     */
    private List<Sentence> processPaperTextWithEnhancement(Long paperId, String content) {
        List<String> sentenceTexts = TextProcess.splitIntoSentences(content);
        List<Sentence> sentences = new ArrayList<>();

        for (int i = 0; i < sentenceTexts.size(); i++) {
            String sentenceText = sentenceTexts.get(i);
            // 创建句子对象
            Sentence sentence = new Sentence();
            sentence.setSentenceText(sentenceText);
            sentence.setPaperId(paperId);
            sentence.setSentenceIndex(i);

            // 分词
            List<String> words = TextProcess.segmentWords(sentenceText);
            sentence.setWordCount(words.size());

            // 过滤停用词
            List<String> filteredWords = TextProcess.filterStopWords(words);

            // 生成MinHash签名
            String minhashSignature = TextProcess.generateMinhashSignature(filteredWords);
            if(minhashSignature==null || minhashSignature.isEmpty()){
                continue;
            }
            sentence.setMinhashSignature(minhashSignature);

            // 生成LSH桶标识
            String lshBucket = TextProcess.generateLSHBucket(minhashSignature);
            if(lshBucket==null || lshBucket.isEmpty()){
                continue;
            }
            sentence.setLshBucket(lshBucket);

            // 增强句子字段
            enhanceSentenceFields(sentence);

            sentences.add(sentence);
        }

        return sentences;
    }

    /**
     * 增强句子字段信息
     * @param sentence 句子对象
     */
    private void enhanceSentenceFields(Sentence sentence) {
        try {
            // 1. 生成句子向量
            INDArray vector = chineseTextSimilarity.getSentenceVector(sentence.getSentenceText());
            if (vector != null) {
                // 将向量转换为Base64字符串存储
                String vectorStr = Base64.getEncoder().encodeToString(vector.toDoubleVector().toString().getBytes());
                sentence.setSentenceVector(vectorStr);
            }

            // 2. 提取关键术语
            List<String> keyTerms = extractKeywords(sentence.getSentenceText(), 5);
            sentence.setKeyTerms(objectMapper.writeValueAsString(keyTerms));

            // 3. 生成语义哈希
            String semanticHash = generateSemanticHash(sentence.getSentenceText());
            sentence.setSemanticHash(semanticHash);

        } catch (Exception e) {
            log.warn("增强句子字段信息时出错: {}", e.getMessage());
        }
    }

    /**
     * 生成语义哈希
     * @param sentenceText 句子文本
     * @return 语义哈希值
     */
    private String generateSemanticHash(String sentenceText) {
        INDArray vector = chineseTextSimilarity.getSentenceVector(sentenceText);
        if (vector == null) {
            return String.valueOf(sentenceText.hashCode());
        }
        // 简化的语义哈希生成
        return String.valueOf(Arrays.hashCode(vector.toDoubleVector()));
    }


    /**
     * 从论文文本内容中提取标题和作者信息
     * @param content 解析后的论文文本内容
     * @return 包含标题和作者信息的Map
     */
    public static Map<String, String> extractPaperInfo(String content) {
        Map<String, String> paperInfo = new HashMap<>();

        if (content == null || content.isEmpty()) {
            return paperInfo;
        }

        // 提取标题 - 查找"设计（论文）题目："后的文本，直到换行或学号等信息
        String titlePattern = "设计\\（论文\\）题目[：:\\s]*([^\n]+)";
        java.util.regex.Pattern titleRegex = java.util.regex.Pattern.compile(titlePattern);
        java.util.regex.Matcher titleMatcher = titleRegex.matcher(content);
        if (titleMatcher.find()) {
            paperInfo.put("title", titleMatcher.group(1).trim());
        }

        // 提取作者姓名 - 查找"姓名"后的文本，直到学号信息
        String authorPattern = "姓\\s*名[：:\\s]*([^\\s]+)";
        java.util.regex.Pattern authorRegex = java.util.regex.Pattern.compile(authorPattern);
        java.util.regex.Matcher authorMatcher = authorRegex.matcher(content);
        if (authorMatcher.find()) {
            paperInfo.put("author", authorMatcher.group(1).trim());
        }

        return paperInfo;
    }


    /**
     * 提取标题与作者
     * @param text
     * @return
     */
    private Map<String, String> extractInfo(String text) {
        Map<String, String> result = new HashMap<>();

        // 提取标题
        int titleStart = text.indexOf("题目");
        int collegeStart = text.indexOf("学院名称");
        if (titleStart != -1 && collegeStart != -1 && titleStart + 3 < collegeStart) {
            result.put("title", text.substring(titleStart + 3, collegeStart));
        }

        // 提取作者姓名
        int nameStart = text.indexOf("姓名");
        int studentIdStart = text.indexOf("学号");
        if (nameStart != -1 && studentIdStart != -1 && nameStart + 3 < studentIdStart) {
            result.put("author", text.substring(nameStart + 3, studentIdStart));
        }

        return result;
    }

    public static void main(String[] args) {

    }
}
