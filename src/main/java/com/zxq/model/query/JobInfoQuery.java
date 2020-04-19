package com.zxq.model.query;

import com.zxq.model.vo.PageVO;
import lombok.Data;

/**
 * @author zxq
 * @date 2020/3/25 14:29
 **/
@Data
public class JobInfoQuery extends PageVO {

    /**
     * http任务标题
     */
    private String title;

    /**
     * http任务状态
     */
    private Integer status;

    /**
     * http任务分组id
     */
    private Integer jobGroupId;

}
