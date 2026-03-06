package com.afeng.plagchenckpro.service.impl;

import com.afeng.plagchenckpro.service.DocumentParseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 文档解析服务实现类
 * 使用Apache Tika进行文档解析，支持PDF、Word、TXT等多种格式
 * 
 * @author afeng
 */
@Service
@Slf4j
public class DocumentParseServiceImpl implements DocumentParseService {
    

    /** Apache Tika实例，用于文档解析 */
    private final Tika tika;
    
    /** 支持的文件类型 */
    private static final List<String> SUPPORTED_TYPES = Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain",
        "text/html"
    );
    
    /** 支持的文件扩展名 */
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
        ".pdf", ".doc", ".docx", ".txt", ".html"
    );
    
    /**
     * 构造函数，初始化Tika实例
     */
    public DocumentParseServiceImpl() {
        this.tika = new Tika();
    }
    
    @Override
    public String parseDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (!isSupportedFileType(file)) {
            throw new IllegalArgumentException("不支持的文件类型: " + getFileType(file));
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            
            // 使用Tika解析文档内容
            String content = tika.parseToString(inputStream);
            
            if (content == null || content.trim().isEmpty()) {
                throw new IOException("文档解析失败，无法提取文本内容");
            }
            
            log.info("文档解析成功，提取文本长度: {} 字符", content.length());
            return content;
            
        } catch (TikaException e) {
            log.error("Tika解析文档失败: {}", e.getMessage(), e);
            throw new IOException("文档解析失败: " + e.getMessage(), e);
        }
    }

    
    @Override
    public boolean isSupportedFileType(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }
        
        // 检查MIME类型
        String contentType = file.getContentType();
        if (contentType != null && SUPPORTED_TYPES.contains(contentType)) {
            return true;
        }
        
        // 检查文件扩展名
        String fileName = file.getOriginalFilename().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
    
    @Override
    public boolean isSupportedFileType(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(lowerFileName::endsWith);
    }
    
    @Override
    public String getFileType(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return "unknown";
        }
        
        String fileName = file.getOriginalFilename().toLowerCase();
        
        if (fileName.endsWith(".pdf")) {
            return "PDF";
        } else if (fileName.endsWith(".doc")) {
            return "DOC";
        } else if (fileName.endsWith(".docx")) {
            return "DOCX";
        } else if (fileName.endsWith(".txt")) {
            return "TXT";
        } else if (fileName.endsWith(".html")) {
            return "HTML";
        } else {
            return "UNKNOWN";
        }
    }
    
    @Override
    public String getFileType(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "unknown";
        }
        
        String lowerFileName = fileName.toLowerCase();
        
        if (lowerFileName.endsWith(".pdf")) {
            return "PDF";
        } else if (lowerFileName.endsWith(".doc")) {
            return "DOC";
        } else if (lowerFileName.endsWith(".docx")) {
            return "DOCX";
        } else if (lowerFileName.endsWith(".txt")) {
            return "TXT";
        } else if (lowerFileName.endsWith(".html")) {
            return "HTML";
        } else {
            return "UNKNOWN";
        }
    }


    public static void main(String[] args) throws IOException, TikaException {
        FileInputStream inputStream = new FileInputStream("E:\\2019\\大数据毕业论文信息汇总\\大数据毕业论文信息汇总\\2223_33_11058_080910T_19403080125_LW.pdf");


            // 使用Tika解析文档内容
            String content = new DocumentParseServiceImpl().tika.parseToString(inputStream);

        System.out.println(content);
    }
}
