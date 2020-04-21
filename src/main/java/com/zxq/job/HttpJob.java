package com.zxq.job;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.zxq.constant.JobConstant;
import com.zxq.constant.JobEnums;
import com.zxq.dao.JobLogMapper;
import com.zxq.dao.JobLogReportMapper;
import com.zxq.model.po.JobInfo;
import com.zxq.model.po.JobLog;
import com.zxq.model.po.JobLogReport;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;

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
     * 任务日志Mapper
     */
    @Resource
    private JobLogMapper jobLogMapper;

    /**
     * 任务日志报表Mapper
     */
    @Resource
    private JobLogReportMapper jobLogReportMapper;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        // 开启计时器
        TimeInterval timer = DateUtil.timer();

        // 创建任务执行记录
        JobLog jobLog = new JobLog();
        jobLog.setCreateTime(DateUtil.date());

        // 更新当天的任务日志报表
        int reportId = this.getJobLogReportByDay(DateUtil.beginOfDay(DateUtil.date()));
        jobLogReportMapper.increaseRunningCount(reportId);

        // 获取任务执行的数据
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        String data = jobDataMap.getString(JobConstant.JOB_INFO_IN_JOB_DATA_MAP_KEY);
        log.info("jobInfo from jobDataMap = {}", data);
        JobInfo jobInfo = JSONUtil.toBean(data, JobInfo.class);

        // 执行http请求
        try {
            String executeResult = sendHttpRequest(jobInfo);
            jobLog.setExecuteStatus(JobEnums.JobLogStatus.SUCCESS.status());
            jobLogReportMapper.increaseSuccessCount(reportId);
            log.info("HttpJob title = {} execute success, HttpResponse : {}", jobInfo.getTitle(), executeResult);
        } catch (Exception e) {
            // http请求失败
            jobLog.setExecuteStatus(JobEnums.JobLogStatus.FAILURE.status());
            jobLog.setExecuteFailMsg(e.getMessage());
            jobLogReportMapper.increaseFailCount(reportId);
            log.error("HttpJob title = {} execute fail, HttpResponse : {}", jobInfo.getTitle(), e);
        }

        //计算任务执行花费时间(毫秒)
        long consumeTime = timer.interval();
        jobLog.setJobInfoId(jobInfo.getId());
        jobLog.setExecuteParams(jobInfo.getParams());
        jobLog.setConsumeTime(consumeTime);
        jobLogMapper.insertSelective(jobLog);
    }

    /**
     * 获取当天的任务日志报表id
     * @param day
     * @return
     */
    private int getJobLogReportByDay(Date day) {
        JobLogReport search = new JobLogReport();
        search.setDay(day);
        JobLogReport jobLogReport = jobLogReportMapper.selectOne(search);
        if (jobLogReport == null) {
            jobLogReportMapper.insertSelective(search);
            return search.getId();
        }
        return jobLogReport.getId();
    }

    /**
     * 根据jobInfo发送http请求
     * @param jobInfo
     * @return
     */
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
