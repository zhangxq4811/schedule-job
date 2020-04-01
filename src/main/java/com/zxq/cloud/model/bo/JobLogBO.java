package com.zxq.cloud.model.bo;

import com.zxq.cloud.model.po.JobLog;
import lombok.Data;

/**
 * @author zxq
 * @date 2020/3/25 17:46
 **/
@Data
public class JobLogBO extends JobLog {

    /**
     * 分组名
     */
    public String jobGroupName;

    /**
     * 任务标题
     */
    public String title;

}
