package com.zxq.cloud.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zxq.cloud.constant.JobConstant;
import com.zxq.cloud.constant.JobEnums;
import com.zxq.cloud.dao.JobInfoMapper;
import com.zxq.cloud.dao.JobLogMapper;
import com.zxq.cloud.job.HttpJob;
import com.zxq.cloud.model.po.JobInfo;
import com.zxq.cloud.model.po.JobLog;
import com.zxq.cloud.model.query.JobInfoQuery;
import com.zxq.cloud.model.query.JobLogQuery;
import com.zxq.cloud.model.vo.PageVO;
import com.zxq.cloud.util.RowBoundsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.utils.Key;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zxq
 * @date 2020/3/24 16:36
 **/
@Slf4j
@Service
public class JobService {

    /**
     * JobInfoMapper
     */
    @Resource
    private JobInfoMapper jobInfoMapper;

    /**
     * JobLogMapper
     */
    @Resource
    private JobLogMapper jobLogMapper;

    /**
     * 添加一个任务,并开启执行
     * @param jobInfo
     * @return
     */
    public String addJob(Scheduler scheduler, JobInfo jobInfo) {
        // 生成jobKey,group
        String group = StrUtil.concat(false, JobConstant.JOB_GROUP_PREFIX, jobInfo.getJobGroupId().toString());
        String jobKey = Key.createUniqueName(group);
        // 保存该任务信息
        jobInfo.setJobKey(jobKey);
        // 新建任务默认为运行中
        jobInfo.setStatus(JobEnums.JobStatus.RUNNING.status());
        jobInfo.setCreateTime(DateUtil.date());
        int jobInfoId = jobInfoMapper.insertSelective(jobInfo);
        jobInfo.setId(jobInfoId);

        // 增加一个quartz的定时任务
        JobDetail jobDetail = JobBuilder.newJob(HttpJob.class).withIdentity(jobKey, group).build();
        // 将任务执行的数据存放在JobDataMap中
        jobDetail.getJobDataMap().put(JobConstant.JOB_INFO_IN_JOB_DATA_MAP_KEY, JSONUtil.toJsonStr(jobInfo));
        CronTrigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail)
                // withMisfireHandlingInstructionDoNothing ：服务重启后不会执行已过期的任务，只会执行下一周期的任务
                .withSchedule(CronScheduleBuilder.cronSchedule(jobInfo.getCorn()).withMisfireHandlingInstructionDoNothing())
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
            return e.getMessage();
        }
    }

    /**
     * 查询http任务列表
     * @param jobInfoQuery
     * @return
     */
    public PageVO<JobInfo> selectJob(JobInfoQuery jobInfoQuery) {
        PageVO<JobInfo> page = new PageVO<>();

        Example example = new Example(JobInfo.class);
        example.setOrderByClause("create_time desc");
        Example.Criteria criteria = example.createCriteria();
        criteria.andNotEqualTo("status", JobEnums.JobStatus.DELETED.status());
        if (StrUtil.isNotBlank(jobInfoQuery.getTitle())) {
            criteria.andLike("title", StrUtil.format("%{}%", jobInfoQuery.getTitle()));
        }
        if (jobInfoQuery.getStatus() != null) {
            criteria.andEqualTo("status", jobInfoQuery.getStatus());
        }
        if (jobInfoQuery.getJobGroupId() != null) {
            criteria.andEqualTo("jobGroupId", jobInfoQuery.getJobGroupId());
        }

        RowBounds rowBounds = RowBoundsUtil.getRowBounds(jobInfoQuery.getPage(), jobInfoQuery.getSize());
        List<JobInfo> data = jobInfoMapper.selectByExampleAndRowBounds(example, rowBounds);
        int total = jobInfoMapper.selectCountByExample(example);

        page.setList(data);
        page.setTotal(total);

        return page;
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
        JobKey jobKey = getJobKeyFromJobInfo(jobInfo);
        try {
            if (status.equals(JobEnums.JobStatus.PAUSING.status())) {
                // 暂停调度任务
                scheduler.pauseJob(jobKey);
            } else if (status.equals(JobEnums.JobStatus.DELETED.status())) {
                if (JobEnums.JobStatus.RUNNING.status().equals(jobInfo.getStatus())) {
                    // 暂停调度任务
                    scheduler.pauseJob(jobKey);
                }
                // 删除调度任务
                scheduler.deleteJob(jobKey);
            } else if (status.equals(JobEnums.JobStatus.RUNNING.status())) {
                // 恢复一个任务
                scheduler.resumeJob(jobKey);
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
     * 查询指定任务的日志
     * @param jobLogQuery
     * @return
     */
    public PageVO<JobLog> selectJobLog(JobLogQuery jobLogQuery) {
        PageVO<JobLog> page = new PageVO<>();

        Example example = new Example(JobLog.class);
        example.setOrderByClause("execute_time desc");
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("jobInfoId", jobLogQuery.getJobInfoId());

        RowBounds rowBounds = RowBoundsUtil.getRowBounds(jobLogQuery.getPage(), jobLogQuery.getSize());
        List<JobLog> data = jobLogMapper.selectByExampleAndRowBounds(example, rowBounds);
        int total = jobLogMapper.selectCountByExample(example);

        page.setList(data);
        page.setTotal(total);

        return page;
    }

    private JobKey getJobKeyFromJobInfo(JobInfo jobInfo) {
        if (jobInfo != null && StrUtil.isNotBlank(jobInfo.getJobKey()) && jobInfo.getJobGroupId() != null) {
            String jobKey = jobInfo.getJobKey();
            String group = StrUtil.concat(false, JobConstant.JOB_GROUP_PREFIX, jobInfo.getJobGroupId().toString());
            return new JobKey(jobKey, group);
        }
        return null;
    }

}
