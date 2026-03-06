package com.afeng.plagchenckpro.entity.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@ToString
@NoArgsConstructor
public class PaperVo {
    /**
     * 论文ID，主键，自增
     */
    private Long id;

    /**
     * 论文标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;


    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型 (PDF, DOC, DOCX)
     */
    private String fileType;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    /**
     * 字数统计
     */
    private Integer wordCount;

}
