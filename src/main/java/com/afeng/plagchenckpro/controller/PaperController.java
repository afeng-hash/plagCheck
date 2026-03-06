package com.afeng.plagchenckpro.controller;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.common.reuslt.ResultCodeEnum;
import com.afeng.plagchenckpro.entity.dto.PaperDto;
import com.afeng.plagchenckpro.entity.pojo.Paper;
import com.afeng.plagchenckpro.service.FileService;
import com.afeng.plagchenckpro.service.PaperService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 论文管理控制器
 * 提供论文上传、查询、删除等REST API接口
 *
 * @author afeng
 */
@RequestMapping("/api/papers")
@CrossOrigin(origins = "*")
@Slf4j
@RestController
public class PaperController {

    @Autowired
    private PaperService paperService;

    @Autowired
    private FileService fileService;

    /**
     * 上传论文文件
     * @param file 论文文件
     * @param title 论文标题
     * @param author 作者
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result uploadPaper(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title",required = false) String title,
            @RequestParam(value = "author", required = false) String author) {

        try {
            if (file == null || file.isEmpty()) {
                return Result.build(null, ResultCodeEnum.FILE_NULL);
            }

            log.info("收到论文上传请求，文件名: {}, 标题: {}, 作者: {}",
                    file.getOriginalFilename(), title, author);

            // 上传论文
            Paper paper = paperService.uploadPaper(file, title, author);

            log.info("论文上传成功，ID: {}, 标题: {}", paper.getId(), paper.getTitle());

            return Result.ok();
        } catch (Exception e) {
            log.error("论文上传失败: {}", e.getMessage(), e);
            return Result.fail();
        }
    }

    /**
     * 分页查询
     */
    @GetMapping("/list")
    public Result list(PaperDto paperDto){
        log.info("分页查询：{},{},{},{}",paperDto.getAuthor(),paperDto.getTitle(),paperDto.getPageSize(),paperDto.getPageNum());

        return paperService.getList(paperDto);
    }

    /**
     * 根据id删除文章
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        log.info("删除，id；{}",id);
        return paperService.delete(id);
    }




    /**
     * 下载文章
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam String url, HttpServletResponse response) {
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            log.info("下载文件，文件名：{}",fileName);
            InputStream inputStream = fileService.downloadFileByUrl(url);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            IOUtils.copy(inputStream, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            } catch (IOException ioException) {
                // 记录日志，便于排查问题
                log.info("发送错误响应失败: " + ioException.getMessage());
            }
        }
    }

}
