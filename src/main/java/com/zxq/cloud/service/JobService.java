package com.zxq.cloud.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.zxq.cloud.constant.JobConstant;
import com.zxq.cloud.constant.JobEnums;
import com.zxq.cloud.dao.JobGroupMapper;
import com.zxq.cloud.dao.JobInfoMapper;
import com.zxq.cloud.dao.JobLogMapper;
import com.zxq.cloud.job.HttpJob;
import com.zxq.cloud.model.bo.JobInfoBO;
import com.zxq.cloud.model.po.JobGroup;
import com.zxq.cloud.model.po.JobInfo;
import com.zxq.cloud.model.po.JobLog;
import com.zxq.cloud.model.query.JobInfoQuery;
import com.zxq.cloud.model.query.JobLogQuery;
import com.zxq.cloud.model.vo.PageVO;
import com.zxq.cloud.util.JobUtil;
import com.zxq.cloud.util.RowBoundsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.quartz.*;
import org.quartz.utils.Key;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

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
        CronTrigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail)
                // withMisfireHandlingInstructionDoNothing ：服务重启后不会执行已过期的任务，只会执行下一周期的任务
                .withSchedule(CronScheduleBuilder.cronSchedule(jobInfoBO.getCorn()).withMisfireHandlingInstructionDoNothing())
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
     * 查询http任务列表
     * @param jobInfoQuery
     * @return
     */
    public PageVO<JobInfoBO> selectJob(JobInfoQuery jobInfoQuery) {
        PageVO<JobInfoBO> page = new PageVO<>();
        int pageNum = Optional.ofNullable(jobInfoQuery.getPage()).map(v -> v-1).orElse(0);
        int pageSize = Optional.ofNullable(jobInfoQuery.getLimit()).orElse(10);
        PageHelper.startPage(pageNum, pageSize);
        List<JobInfoBO> jobInfoBOS = jobInfoMapper.selectJobInfo(jobInfoQuery);
        if (jobInfoBOS != null && !jobInfoBOS.isEmpty()) {
            page.setList(jobInfoBOS);
            page.setTotal(jobInfoBOS.size());
        }
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
        JobKey jobKey = JobUtil.getJobKeyFromJobInfo(jobInfo);
        try {
            if (status.equals(JobEnums.JobStatus.PAUSING.status())) {
                // 暂停调度任务
                scheduler.pauseJob(jobKey);
            } else if (status.equals(JobEnums.JobStatus.DELETED.status())) {
                // 删除任务
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

        RowBounds rowBounds = RowBoundsUtil.getRowBounds(jobLogQuery.getPage(), jobLogQuery.getLimit());
        List<JobLog> data = jobLogMapper.selectByExampleAndRowBounds(example, rowBounds);
        int total = jobLogMapper.selectCountByExample(example);

        page.setList(data);
        page.setTotal(total);

        return page;
    }

    /**
     * 获取数据库中已有任务分组
     * @return
     */
    public List<JobGroup> selectJobGroup() {
        Example example = new Example(JobGroup.class);
        example.setOrderByClause("create_time desc");
        return jobGroupMapper.selectByExample(example);
    }
}
