package com.zxq.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zxq
 * @date 2020/3/25 14:32
 **/
@Data
public class PageVO<T> {

    /**
     * 页序数
     */
    private Integer page;

    /**
     * 当页记录数量
     */
    private Integer limit;

    /**
     * 数据集
     */
    private List<T> list;

    /**
     * 总条数
     */
    private Integer total;

}
