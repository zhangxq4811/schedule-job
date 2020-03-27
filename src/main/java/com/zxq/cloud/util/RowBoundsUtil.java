package com.zxq.cloud.util;

import org.apache.ibatis.session.RowBounds;

import java.util.Optional;

/**
 * @author zxq
 * @date 2020/3/25 15:03
 **/
public class RowBoundsUtil {

    /**
     * 获取RowBounds
     * @param page
     * @return
     */
    public static RowBounds getRowBounds(Integer page, Integer size) {
        int offset = Optional.ofNullable(page).map(v -> v-1).orElse(0);
        int limit = Optional.ofNullable(size).orElse(10);
        RowBounds rowBounds = new RowBounds(offset, limit);
        return rowBounds;
    }

}
