package com.afeng.plagchenckpro.service;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.entity.dto.PaperDto;
import com.afeng.plagchenckpro.entity.pojo.Paper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PaperService extends IService<Paper> {

    //上传论文到论文库
    Paper uploadPaper(MultipartFile file, String title, String author) throws Exception;

    /**
     * 条件查询
     * @param paperDto
     * @return
     */
    Result getList(PaperDto paperDto);

    /**
     * 根据id删除
     * @param id
     * @return
     */
    Result delete(Long id);
}
