package com.zxq.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zxq.constant.JobConstant;
import com.zxq.constant.JobEnums;
import com.zxq.dao.JobGroupMapper;
import com.zxq.dao.JobInfoMapper;
import com.zxq.job.HttpJob;
import com.zxq.model.bo.JobInfoBO;
import com.zxq.model.po.JobGroup;
import com.zxq.model.po.JobInfo;
import com.zxq.util.JobUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 判断是否需要创建新的任务分组
     * @param jobInfoBO
     * @return
     */
    private String checkCreateJobGroup(JobInfoBO jobInfoBO) {
        if (jobInfoBO.getJobGroupId() == null && StrUtil.isNotBlank(jobInfoBO.getJobGroupName())) {
            JobGroup findOne = new JobGroup();
            findOne.setName(jobInfoBO.getJobGroupName());
            int count = jobGroupMapper.selectCount(findOne);
            if (count == 0) {
                findOne.setCreateTime(DateUtil.date());
                jobGroupMapper.insertSelective(findOne);
                jobInfoBO.setJobGroupId(findOne.getId());
            } else {
                return "该分组名已存在";
            }
        }
        return JobConstant.SUCCESS_CODE;
    }

    /**
     * 添加一个任务,并开启执行
     * @param jobInfoBO
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public String addJob(Scheduler scheduler, JobInfoBO jobInfoBO) {
        // 1.判断是否需要生成新的任务分组
        String checkRes = checkCreateJobGroup(jobInfoBO);
        if (!JobConstant.SUCCESS_CODE.equals(checkRes)) {
            return checkRes;
        }
        // 2.新增jonInfo
        JobInfo checkJobTitle = new JobInfo();
        checkJobTitle.setTitle(jobInfoBO.getTitle());
        int count = jobInfoMapper.selectCount(checkJobTitle);
        if (count > 0) {
            return "该任务名称已被占用";
        }
        jobInfoBO.setStatus(JobEnums.JobStatus.RUNNING.status());
        jobInfoBO.setCreateTime(DateUtil.date());
        jobInfoMapper.insertSelective(jobInfoBO);
        int jobInfoId = jobInfoBO.getId();
        jobInfoBO.setId(jobInfoId);

        // 3.增加一个quartz的定时任务
        JobDetail jobDetail = JobBuilder.newJob(HttpJob.class).withIdentity(JobUtil.getJobKey(jobInfoBO)).build();
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
            log.error("JobManagerService addJob scheduleJob occur exception : {}", e);
            // 手动回滚事务
            throw new RuntimeException(e);
        }
    }

    /**
     * 暂停、删除、恢复任务
     * @param scheduler
     * @param jobInfoId
     * @return
     */
    public String pauseOrRemoveOrRestoreJob(Scheduler scheduler, Integer jobInfoId, Integer status) {
        JobInfo jobInfo = jobInfoMapper.selectByPrimaryKey(jobInfoId);
        if (jobInfo == null || JobUtil.isDeletedJob(jobInfo)) {
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
            log.error("jobInfoId = {} pauseOrRemoveOrRestoreJob error : {}", jobInfoId, e);
            return e.getMessage();
        }
    }

    /**
     * 立即执行任务
     * @param scheduler
     * @param jobInfoId
     * @return
     */
    public String executeJob(Scheduler scheduler, Integer jobInfoId) {
        JobInfo jobInfo = jobInfoMapper.selectByPrimaryKey(jobInfoId);
        if (jobInfo == null || JobUtil.isDeletedJob(jobInfo)) {
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
            log.error("jobInfoId = {} executeJob error : {}", jobInfoId, e);
            return e.getMessage();
        }
    }

    /**
     * 修改任务
     *    每次编辑，都是新增一个调度任务替换旧的调度任务
     * @param scheduler
     * @param jobInfoBO
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public String editJob(Scheduler scheduler, JobInfoBO jobInfoBO) {
        JobInfo jobInfoInDB = jobInfoMapper.selectByPrimaryKey(jobInfoBO.getId());
        if (jobInfoInDB == null) {
            return "无法匹配指定任务";
        }
        if (JobUtil.isDeletedJob(jobInfoInDB)) {
            return "无法编辑已删除的任务";
        }
        // 判断是否需要生成新的任务分组
        String checkRes = checkCreateJobGroup(jobInfoBO);
        if (!JobConstant.SUCCESS_CODE.equals(checkRes)) {
            return checkRes;
        }
        jobInfoMapper.updateByPrimaryKeySelective(jobInfoBO);
        try {
            JobKey jobKey = JobUtil.getJobKey(jobInfoBO);
            // 删除旧任务
            scheduler.deleteJob(jobKey);
            // 新增任务
            JobDetail jobDetail = JobBuilder.newJob(HttpJob.class).withIdentity(JobUtil.getJobKey(jobInfoBO)).build();
            jobDetail.getJobDataMap().put(JobConstant.JOB_INFO_IN_JOB_DATA_MAP_KEY, JSONUtil.toJsonStr(jobInfoBO));
            CronTrigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(JobUtil.getTriggerKey(jobInfoBO))
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobInfoBO.getCron()).withMisfireHandlingInstructionDoNothing())
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            // 原任务为暂停状态，新增任务状态要与原来保持一致
            if (JobEnums.JobStatus.PAUSING.status().equals(jobInfoInDB.getStatus())) {
                scheduler.pauseJob(jobKey);
            }
        } catch (Exception e) {
            log.error("jobInfoId = {} editJob error : {}", jobInfoInDB.getId(), e);
            // 手动回滚事务
            throw new RuntimeException(e);
        }
        return JobConstant.SUCCESS_CODE;
    }

}