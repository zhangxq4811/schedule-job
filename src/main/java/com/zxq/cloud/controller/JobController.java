package com.zxq.cloud.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zxq.cloud.constant.JobConstant;
import com.zxq.cloud.constant.JobEnums;
import com.zxq.cloud.model.bo.JobInfoBO;
import com.zxq.cloud.model.bo.JobLogBO;
import com.zxq.cloud.model.query.JobInfoQuery;
import com.zxq.cloud.model.query.JobLogQuery;
import com.zxq.cloud.model.vo.PageVO;
import com.zxq.cloud.model.vo.ResultVO;
import com.zxq.cloud.service.JobManagerService;
import com.zxq.cloud.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zxq
 * @date 2020/3/24 15:08
 **/
@RestController
@Slf4j
public class JobController {

    /**
     * 调度器
     */
    @Autowired
    private Scheduler scheduler;

    /**
     * JobManagerService
     */
    @Autowired
    private JobManagerService jobManagerService;

    /**
     * JobService
     */
    @Autowired
    private JobService jobService;

    /**
     * 新增一个http定时任务
     * @param jobInfoBO
     * @return
     */
    @RequestMapping("/addJob")
    public ResultVO addJob(JobInfoBO jobInfoBO) {
        if (StrUtil.isBlank(jobInfoBO.getTitle())) {
            return ResultVO.failure("任务名称不能为空");
        }
        if (StrUtil.isBlank(jobInfoBO.getUrl())) {
            return ResultVO.failure("URL地址不能为空");
        }
        if (StrUtil.isBlank(jobInfoBO.getMethod())) {
            return ResultVO.failure("请求方式不能为空");
        }
        if (StrUtil.isBlank(jobInfoBO.getJobGroupName()) && jobInfoBO.getJobGroupId() == null) {
            return ResultVO.failure("业务部门不能为空");
        }
        // 校验corn表达式
        if(!CronExpression.isValidExpression(jobInfoBO.getCron())) {
            return ResultVO.failure("非法的任务corn表达式");
        }
        // 有参数，校验参数是否为json格式
        if (StrUtil.isNotBlank(jobInfoBO.getParams()) && JSONUtil.isJson(jobInfoBO.getParams())) {
            return ResultVO.failure("非法的任务参数格式");
        }
        String res = jobManagerService.addJob(scheduler, jobInfoBO);
        if (JobConstant.SUCCESS_CODE.equals(res)) {
            return ResultVO.success("添加成功");
        } else {
            return ResultVO.failure(res);
        }
    }

    /**
     * 暂停一个http定时任务
     * @return
     * @throws SchedulerException
     */
    @RequestMapping("/pauseJob")
    public ResultVO pauseJob(@RequestParam(name = "jobInfoId") Integer jobInfoId) {
        String res = jobManagerService.pauseOrRemoveOrRestoreJob(scheduler, jobInfoId, JobEnums.JobStatus.PAUSING.status());
        if (JobConstant.SUCCESS_CODE.equals(res)) {
            return ResultVO.success("操作成功");
        } else {
            return ResultVO.failure(res);
        }
    }

    /**
     * 恢复一个http定时任务
     * @param jobInfoId
     * @return
     */
    @RequestMapping("/restoreJob")
    public ResultVO restoreJob(@RequestParam(name = "jobInfoId") Integer jobInfoId) {
        String res = jobManagerService.pauseOrRemoveOrRestoreJob(scheduler, jobInfoId, JobEnums.JobStatus.RUNNING.status());
        if (JobConstant.SUCCESS_CODE.equals(res)) {
            return ResultVO.success("操作成功");
        } else {
            return ResultVO.failure(res);
        }
    }

    /**
     * 删除一个http定时任务
     * @param jobInfoId
     * @return
     */
    @RequestMapping("/removeJob")
    public ResultVO removeJob(@RequestParam(name = "jobInfoId") Integer jobInfoId) {
        String res = jobManagerService.pauseOrRemoveOrRestoreJob(scheduler, jobInfoId, JobEnums.JobStatus.DELETED.status());
        if (JobConstant.SUCCESS_CODE.equals(res)) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.failure(res);
        }
    }

    /**
     * 触发执行一次任务
     * @param jobInfoId
     * @return
     */
    @RequestMapping("/executeJob")
    public ResultVO executeJob(@RequestParam(name = "jobInfoId") Integer jobInfoId) {
        String res = jobManagerService.executeJob(scheduler, jobInfoId);
        if (JobConstant.SUCCESS_CODE.equals(res)) {
            return ResultVO.success("执行成功");
        } else {
            return ResultVO.failure(res);
        }
    }

    /**
     * 分页获取http任务列表
     * @param jobInfoQuery
     * @return
     */
    @RequestMapping("/pageJob")
    public ResultVO pageJob(JobInfoQuery jobInfoQuery) {
        PageVO<JobInfoBO> page = jobService.selectJob(jobInfoQuery);
        return ResultVO.success(page);
    }

    /**
     * 分页查询指定任务的执行日志
     * @param jobLogQuery
     * @return
     */
    @RequestMapping("/pageJobLog")
    public ResultVO pageJobLog(JobLogQuery jobLogQuery) {
        if (jobLogQuery.getJobInfoId() == null) {
            return ResultVO.failure("param jobInfoId is empty");
        }
        PageVO<JobLogBO> page = jobService.selectJobLog(jobLogQuery);
        return ResultVO.success(page);
    }

}
