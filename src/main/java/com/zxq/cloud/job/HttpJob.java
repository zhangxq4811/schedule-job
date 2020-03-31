package com.zxq.cloud.job;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.zxq.cloud.constant.JobConstant;
import com.zxq.cloud.constant.JobEnums;
import com.zxq.cloud.dao.JobLogMapper;
import com.zxq.cloud.model.po.JobInfo;
import com.zxq.cloud.model.po.JobLog;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

/**
 * htt任务处理类
 * @author zxq
 * @date 2020/3/23 15:42
 *
 * @DisallowConcurrentExecution 禁止并发执行多个相同定义的JobDetail
 **/
@Slf4j
@DisallowConcurrentExecution
public class HttpJob extends QuartzJobBean {

    /**
     * JobGroupMapper
     */
    @Resource
    private JobLogMapper jobLogMapper;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        // 计时器
        TimeInterval timer = DateUtil.timer();
        // 任务执行记录
        JobLog jobLog = new JobLog();
        jobLog.setExecuteTime(DateUtil.date());
        Integer executeStatus = JobEnums.JobLogStatus.SUCCESS.status();

        String executeResult;
        JobInfo jobInfo = null;
        // 获取任务执行的数据
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        String data = jobDataMap.getString(JobConstant.JOB_INFO_IN_JOB_DATA_MAP_KEY);
        log.info("jobInfo from jobDataMap = {}", data);
        try {
            // http任务执行成功
            jobInfo = JSONUtil.toBean(data, JobInfo.class);
            if (jobInfo == null) {
                log.info("jobKey = {} not find execute data", jobExecutionContext.getJobDetail().getKey());
                return;
            }
            executeResult = sendHttpRequest(jobInfo);
        } catch (Exception e) {
            // http任务执行失败
            executeStatus = JobEnums.JobLogStatus.FAILURE.status();
            executeResult = e.getMessage();
            log.error("HttpRequest url = {} execute fail : {}", jobInfo.getUrl(), e);
        }
        log.info("jobInfoId = {} sendHttpRequest response = {}", jobInfo.getId(), executeResult);

        //花费毫秒数
        long consumeTime = timer.interval();
        jobLog.setJobInfoId(jobInfo.getId());
        jobLog.setExecuteStatus(executeStatus);
        jobLog.setExecuteParams(jobInfo.getParams());
        //根据业务考虑是否要把请求结果保存至数据库
        jobLog.setExecuteResult(executeResult);
        jobLog.setConsumeTime(consumeTime);
        jobLogMapper.insertSelective(jobLog);
    }

    private String sendHttpRequest(JobInfo jobInfo) {
        String response = null;
        String url = jobInfo.getUrl();
        String method = jobInfo.getMethod();
        String params = jobInfo.getParams();
        log.info("url = {}, method = {}, params = {}", url, method, params);
        if (method.toUpperCase().equals(HttpMethod.GET.name())) {
            // get 请求
            if (StrUtil.isNotBlank(params)) {
                response = HttpUtil.get(url, JSONUtil.parseObj(params));
            } else {
                response = HttpUtil.get(url);
            }
        } else if (method.toUpperCase().equals(HttpMethod.POST.name())) {
            // post 请求
            if (StrUtil.isNotBlank(params)) {
                response = HttpUtil.post(url, JSONUtil.parseObj(params));
            } else {
                response = HttpUtil.post(url, "");
            }
        }

        return response;
    }

}
