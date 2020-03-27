package com.zxq.cloud.model.query;

import com.zxq.cloud.model.vo.PageVO;
import lombok.Data;

/**
 * @author zxq
 * @date 2020/3/25 16:10
 **/
@Data
public class JobLogQuery extends PageVO {

    /**
     * http任务id
     */
    private Integer jobInfoId;

}
