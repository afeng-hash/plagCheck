package com.afeng.plagchenckpro.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文档解析服务接口
 * 负责从PDF、Word等文档中提取文本内容
 * 
 * @author afeng
 */
public interface DocumentParseService {
    
    /**
     * 解析文档并提取文本内容
     * 
     * @param file 上传的文档文件
     * @return 提取的文本内容
     * @throws IOException 文件读取异常
     */
    String parseDocument(MultipartFile file) throws IOException;

    
    /**
     * 检查文件类型是否支持
     * 
     * @param file 文件
     * @return 是否支持
     */
    boolean isSupportedFileType(MultipartFile file);
    
    /**
     * 检查文件类型是否支持
     * 
     * @param fileName 文件名
     * @return 是否支持
     */
    boolean isSupportedFileType(String fileName);
    
    /**
     * 获取文件类型
     * 
     * @param file 文件
     * @return 文件类型
     */
    String getFileType(MultipartFile file);
    
    /**
     * 获取文件类型
     * 
     * @param fileName 文件名
     * @return 文件类型
     */
    String getFileType(String fileName);
}
