package com.afeng.plagchenckpro.service;

import com.afeng.plagchenckpro.entity.vo.CheckResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 查重服务接口
 * 负责论文查重检测，包括预筛选和精确比对
 *
 * @author afeng
 */
public interface PlagiarismCheckService {


    /**
     * 对上传的论文进行查重检测
     *
     * @param file 待检测的论文文件
     * @param threshold 相似度阈值 (0.0-1.0)
     * @return 查重结果
     * @throws IOException 文件处理异常
     */
    CheckResult checkPlagiarism(MultipartFile file, double threshold) throws IOException;
}
