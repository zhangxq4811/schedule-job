package com.zxq.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.zxq.constant.JobConstant;
import com.zxq.constant.JobEnums;
import com.zxq.model.po.JobInfo;
import org.quartz.CronExpression;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.text.ParseException;
import java.util.Date;

/**
 * @author zxq
 */
public class JobUtil {

    /**
     * 获取 JobKey
     * @param jobInfo
     * @return
     */
    public static JobKey getJobKey(JobInfo jobInfo) {
        String jobKey = JobConstant.JOB_KEY_PREFIX + jobInfo.getId();
        return new JobKey(jobKey);
    }

    /**
     * 获取TriggerKey
     * @param jobInfo
     * @return
     */
    public static TriggerKey getTriggerKey(JobInfo jobInfo) {
        String triggerKey = JobConstant.TRIGGER_KEY_PREFIX + jobInfo.getId();
        return new TriggerKey(triggerKey);
    }

    /**
     * 判断任务是否正常执行中
     * @param scheduler
     * @param triggerKey
     * @return
     */
    public static boolean isNormal(Scheduler scheduler, TriggerKey triggerKey) {
        try {
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            if (Trigger.TriggerState.NORMAL.equals(triggerState)) {
                return true;
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断任务是否暂停
     * @param scheduler
     * @param triggerKey
     * @return
     */
    public static boolean isPaused(Scheduler scheduler, TriggerKey triggerKey) {
        try {
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            if (Trigger.TriggerState.PAUSED.equals(triggerState)) {
                return true;
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取任务下一次运行时间
     * @param jobInfo
     * @return
     * @throws ParseException
     */
    public static Date getNextExecuteTime(JobInfo jobInfo) {
        if (jobInfo != null && StrUtil.isNotBlank(jobInfo.getCron())) {
            try {
                CronExpression cronExpression = new CronExpression(jobInfo.getCron());
                return cronExpression.getNextValidTimeAfter(DateUtil.date());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 判断该是否为已删除的任务
     * @param jobInfo
     * @return
     */
    public static boolean isDeletedJob(JobInfo jobInfo) {
        if (jobInfo != null && !JobEnums.JobStatus.DELETED.status().equals(jobInfo.getStatus())) {
            return false;
        }
        return true;
    }

}
