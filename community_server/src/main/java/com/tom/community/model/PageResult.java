package com.tom.community.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 分页结果 — 用于列表接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;    // 当前页数据
    private Long total;      // 总记录数
    private Integer page;    // 当前页码
    private Integer size;    // 每页大小
}
