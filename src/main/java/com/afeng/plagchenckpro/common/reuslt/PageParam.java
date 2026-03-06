package com.afeng.plagchenckpro.common.reuslt;

import lombok.Data;

/**
 * 分页对象
 */
@Data
public class PageParam {
    /**
     * 每页数量
     */
    Integer pageSize;

    /**
     * 第几页
     */
    Integer pageNum;


}
