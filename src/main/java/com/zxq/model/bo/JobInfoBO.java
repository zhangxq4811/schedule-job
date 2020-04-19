package com.zxq.model.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zxq.model.po.JobInfo;
import lombok.Data;

import java.util.Date;

/**
 * @author zxq
 * @date 2020/3/25 17:46
 **/
@Data
public class JobInfoBO extends JobInfo {

    /**
     * 分组名
     */
    private String jobGroupName;

    /**
     * 下一次运行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nextExecuteTime;
}
