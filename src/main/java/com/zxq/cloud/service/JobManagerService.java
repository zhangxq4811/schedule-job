package com.zxq.cloud.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zxq.cloud.constant.JobConstant;
import com.zxq.cloud.constant.JobEnums;
import com.zxq.cloud.dao.JobGroupMapper;
import com.zxq.cloud.dao.JobInfoMapper;
import com.zxq.cloud.job.HttpJob;
import com.zxq.cloud.model.bo.JobInfoBO;
import com.zxq.cloud.model.po.JobGroup;
import com.zxq.cloud.model.po.JobInfo;
import com.zxq.cloud.util.JobUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.utils.Key;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zxq
 * @date 2020/4/1 14:12
 **/
@Slf4j
@Service
public class JobManagerService {

    /**
     * JobInfoMapper
     */
    @Resource
    private JobInfoMapper jobInfoMapper;

    /**
     * JobGroupMapper
     */
    @Resource
    private JobGroupMapper jobGroupMapper;

    /**
     * 添加一个任务,并开启执行
     * @param jobInfoBO
     * @return
     */
    public String addJob(Scheduler scheduler, JobInfoBO jobInfoBO) {
        // 判断是否需要生成新的任务分组
        boolean createFlag = checkCreateNewJobGroup(jobInfoBO);
        // 生成jobKey,group
        String group = StrUtil.concat(false, JobConstant.JOB_GROUP_PREFIX, jobInfoBO.getJobGroupId().toString());
        String jobKey = Key.createUniqueName(group);
        // 保存该任务信息
        jobInfoBO.setJobKey(jobKey);
        // 新建任务默认为运行中
        jobInfoBO.setStatus(JobEnums.JobStatus.RUNNING.status());
        jobInfoBO.setCreateTime(DateUtil.date());
        int jobInfoId = jobInfoMapper.insertSelective(jobInfoBO);
        jobInfoBO.setId(jobInfoId);

        // 增加一个quartz的定时任务
        JobDetail jobDetail = JobBuilder.newJob(HttpJob.class).withIdentity(jobKey, group).build();
        // 将任务执行的数据存放在JobDataMap中
        jobDetail.getJobDataMap().put(JobConstant.JOB_INFO_IN_JOB_DATA_MAP_KEY, JSONUtil.toJsonStr(jobInfoBO));
        CronTrigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(JobUtil.getTriggerKey(jobInfoBO))
                // withMisfireHandlingInstructionDoNothing ：服务重启后不会执行已过期的任务，只会执行下一周期的任务
                .withSchedule(CronScheduleBuilder.cronSchedule(jobInfoBO.getCron()).withMisfireHandlingInstructionDoNothing())
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            // 判断调度器是否执行
            if (!scheduler.isStarted()) {
                scheduler.start();
            }
            return JobConstant.SUCCESS_CODE;
        } catch (SchedulerException e) {
            log.error("JobService addJob scheduleJob occur exception : {}", e);
            // 手动回滚事务
            jobInfoMapper.deleteByPrimaryKey(jobInfoId);
            if (createFlag) {
                jobGroupMapper.deleteByPrimaryKey(jobInfoBO.getJobGroupId());
            }
            return e.getMessage();
        }
    }

    /**
     * 检查是否需要生成新的任务分组
     * @param jobInfoBO
     * @return
     */
    private boolean checkCreateNewJobGroup(JobInfoBO jobInfoBO) {
        if (jobInfoBO.getJobGroupId() == null && StrUtil.isNotBlank(jobInfoBO.getJobGroupName())) {
            JobGroup findOne = new JobGroup();
            findOne.setName(jobInfoBO.getJobGroupName());
            int count = jobGroupMapper.selectCount(findOne);
            if (count == 0) {
                findOne.setCreateTime(DateUtil.date());
                jobGroupMapper.insertSelective(findOne);
                jobInfoBO.setJobGroupId(findOne.getId());
                return true;
            }
        }
        return false;
    }

    /**
     * 暂停、删除、恢复任务
     * @param scheduler
     * @param jobInfoId
     * @return
     */
    public String pauseOrRemoveOrRestoreJob(Scheduler scheduler, Integer jobInfoId, Integer status) {
        JobInfo jobInfo = jobInfoMapper.selectByPrimaryKey(jobInfoId);
        if (jobInfo == null) {
            return "no http job matched";
        }
        JobKey jobKey = JobUtil.getJobKey(jobInfo);
        TriggerKey triggerKey = JobUtil.getTriggerKey(jobInfo);
        try {
            if (status.equals(JobEnums.JobStatus.PAUSING.status())) {
                if (status.equals(jobInfo.getStatus())) {
                    return "该任务为停止状态,不可重复操作";
                }
                // 暂停调度任务
                if (!JobUtil.isPaused(scheduler, triggerKey)) {
                    scheduler.pauseJob(jobKey);
                }
            } else if (status.equals(JobEnums.JobStatus.DELETED.status())) {
                // 暂停调度任务
                if (!JobUtil.isPaused(scheduler, triggerKey)) {
                    scheduler.pauseJob(jobKey);
                }
                // 删除调度任务
                scheduler.deleteJob(jobKey);
            } else if (status.equals(JobEnums.JobStatus.RUNNING.status())) {
                if (status.equals(jobInfo.getStatus())) {
                    return "该任务为运行状态,不可重复操作";
                }
                // 恢复一个任务
                if (!JobUtil.isNormal(scheduler, triggerKey)) {
                    scheduler.resumeJob(jobKey);
                }
            } else {
                return "not support status";
            }
            // 更新jobInfo执行状态
            JobInfo update = new JobInfo();
            update.setId(jobInfo.getId());
            update.setStatus(status);
            jobInfoMapper.updateByPrimaryKeySelective(update);
            return JobConstant.SUCCESS_CODE;
        } catch (SchedulerException e) {
            log.error("jobInfoId = {} pauseOrRemoveOrRestoreJob fail : {}", jobInfoId, e);
            return e.getMessage();
        }
    }

    /**
     * 执行一次任务
     * @param scheduler
     * @param jobInfoId
     * @return
     */
    public String executeJob(Scheduler scheduler, Integer jobInfoId) {
        JobInfo jobInfo = jobInfoMapper.selectByPrimaryKey(jobInfoId);
        if (jobInfo == null) {
            return "no http job matched";
        }
        JobKey jobKey = JobUtil.getJobKey(jobInfo);
        TriggerKey triggerKey = JobUtil.getTriggerKey(jobInfo);
        try {
            if (JobUtil.isPaused(scheduler, triggerKey)) {
                return "该任务为停止状态,无法立即运行";
            }
            scheduler.triggerJob(jobKey);
            return JobConstant.SUCCESS_CODE;
        } catch (SchedulerException e) {
            log.error("jobInfoId = {} executeJob fail : {}", jobInfoId, e);
            return e.getMessage();
        }
    }
}
