package com.afeng.plagchenckpro.controller;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.common.reuslt.ResultCodeEnum;
import com.afeng.plagchenckpro.entity.vo.CheckResult;
import com.afeng.plagchenckpro.service.PlagiarismCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 查重检测控制器
 * 提供高效的论文查重检测REST API接口
 *
 * @author afeng
 */
@RestController
@RequestMapping("/api/plagiarism")
@CrossOrigin(origins = "*")
@Slf4j
public class PlagiarismController {

    @Autowired
    private PlagiarismCheckService plagiarismCheckService;

    /**
     * 查重检测接口
     * 上传文件与数据库中的论文进行比对查重
     *
     * @param file 待检测的论文文件
     * @param threshold 相似度阈值（0.0-1.0，默认0.8）
     * @return 查重结果
     */
    @PostMapping("/check")
    public Result checkPlagiarism(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "threshold", defaultValue = "0.8") double threshold) {

        try {
            if (file == null || file.isEmpty()) {
                return Result.build(null, ResultCodeEnum.FILE_NULL);
            }

            if (threshold < 0.0 || threshold > 1.0) {
                return Result.build(null,ResultCodeEnum.THRESHOLD_EOOR);
            }

            log.info("收到查重检测请求，文件名: {}, 阈值: {}", file.getOriginalFilename(), threshold);

            // 执行查重检测
            long start = System.currentTimeMillis();
            CheckResult result = plagiarismCheckService.checkPlagiarism(file, threshold);
            long end = System.currentTimeMillis();

            log.info("查重检测完成，查重率: {}%, 相似句子数: {}，耗时：{}ms",
                    result.getSimilarityRate() * 100, result.getSimilarSentences().size(), end - start);

            return Result.ok(result);

        } catch (Exception e) {
            log.error("查重检测失败: {}", e.getMessage(), e);
            return Result.fail();
        }
    }
}
