package com.afeng.plagchenckpro.common.reuslt;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private int pageNum;     // 当前页码
    private int pageSize;    // 每页数量
    private long total;      // 总记录数
    private List<T> list;    // 当前页数据

    public PageResult(int pageNum, int pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
//        this.pages = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
    }
}
