package com.zxq.util;

import com.github.pagehelper.PageHelper;

import java.util.Optional;

/**
 * 分页工具类
 * @author zxq
 */
public class PageHelperUtil {

    /**
     * 开启分页
     * @param page
     * @param limit
     */
    public static void startPage(Integer page, Integer limit) {
        int pageNum = Optional.ofNullable(page).orElse(1);
        int pageSize = Optional.ofNullable(limit).orElse(10);
        PageHelper.startPage(pageNum, pageSize);
    }

}
