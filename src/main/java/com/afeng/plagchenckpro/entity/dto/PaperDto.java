package com.afeng.plagchenckpro.entity.dto;

import com.afeng.plagchenckpro.common.reuslt.PageParam;
import lombok.Data;

/**
 * 文章查询对象
 */
@Data
public class PaperDto extends PageParam {

    private String author;

    private String title;
}
