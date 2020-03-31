package com.zxq.cloud.util;

import cn.hutool.core.util.StrUtil;
import com.zxq.cloud.constant.JobConstant;
import com.zxq.cloud.model.po.JobInfo;
import org.quartz.JobKey;

/**
 * @author zxq
 */
public class JobUtil {

    /**
     * 获取 JobKey
     * @param jobInfo
     * @return
     */
    public static JobKey getJobKeyFromJobInfo(JobInfo jobInfo) {
        String jobKey = jobInfo.getJobKey();
        String group = StrUtil.concat(false, JobConstant.JOB_GROUP_PREFIX, jobInfo.getJobGroupId().toString());
        return new JobKey(jobKey, group);
    }

}
