package com.zxq.service;

import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageInfo;
import com.zxq.constant.JobEnums;
import com.zxq.dao.JobGroupMapper;
import com.zxq.dao.JobInfoMapper;
import com.zxq.dao.JobLogMapper;
import com.zxq.dao.JobLogReportMapper;
import com.zxq.model.bo.JobInfoBO;
import com.zxq.model.bo.JobLogBO;
import com.zxq.model.po.JobGroup;
import com.zxq.model.po.JobInfo;
import com.zxq.model.po.JobLogReport;
import com.zxq.model.query.JobInfoQuery;
import com.zxq.model.query.JobLogQuery;
import com.zxq.model.vo.PageVO;
import com.zxq.util.JobUtil;
import com.zxq.util.PageHelperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zxq
 * @date 2020/3/24 16:36
 **/
@Slf4j
@Service
public class JobService {

    /**
     * 任务 dao
     */
    @Resource
    private JobInfoMapper jobInfoMapper;

    /**
     * 任务执行日志 dao
     */
    @Resource
    private JobLogMapper jobLogMapper;

    /**
     * 任务执行日志报表 dao
     */
    @Resource
    private JobLogReportMapper jobLogReportMapper;

    /**
     * 任务分组 dao
     */
    @Resource
    private JobGroupMapper jobGroupMapper;

    /**
     * 查询http任务列表
     * @param jobInfoQuery
     * @return
     */
    public PageVO<JobInfoBO> selectJob(JobInfoQuery jobInfoQuery) {
        PageVO<JobInfoBO> page = new PageVO<>();
        PageHelperUtil.startPage(jobInfoQuery.getPage(), jobInfoQuery.getLimit());
        List<JobInfoBO> jobInfoBOs = jobInfoMapper.selectJobInfo(jobInfoQuery);
        PageInfo<JobInfoBO> pageInfo = new PageInfo<>(jobInfoBOs);
        if (pageInfo != null && pageInfo.getTotal() > 0) {
            // 遍历设置下一次运行时间
            jobInfoBOs.forEach(bo -> {
                bo.setNextExecuteTime(JobUtil.getNextExecuteTime(bo));
            });
            page.setList(jobInfoBOs);
            page.setTotal((int)pageInfo.getTotal());
        }
        return page;
    }

    /**
     * 查询指定任务的日志
     * @param jobLogQuery
     * @return
     */
    public PageVO<JobLogBO> selectJobLog(JobLogQuery jobLogQuery) {
        PageVO<JobLogBO> page = new PageVO<>();
        PageHelperUtil.startPage(jobLogQuery.getPage(), jobLogQuery.getLimit());
        List<JobLogBO> jobLogs = jobLogMapper.selectJobLog(jobLogQuery);
        PageInfo<JobLogBO> pageInfo = new PageInfo<>(jobLogs);
        if (pageInfo != null && pageInfo.getTotal() > 0) {
            page.setList(jobLogs);
            page.setTotal((int)pageInfo.getTotal());
        }

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

    /**
     * 获取任务数统计
     * @return
     */
    public Map<String,Integer> getJobInfoAmountStatistic() {
        HashMap<String, Integer> res = new HashMap<>(4);
        JobInfo search = new JobInfo();
        // 总任务数
        int totalCount = jobInfoMapper.selectCount(search);

        // 正常运行任务数
        search.setStatus(JobEnums.JobStatus.RUNNING.status());
        int normalCount = jobInfoMapper.selectCount(search);

        // 已暂停任务数
        search.setStatus(JobEnums.JobStatus.PAUSING.status());
        int pausedCount = jobInfoMapper.selectCount(search);

        // 已删除任务数
        search.setStatus(JobEnums.JobStatus.DELETED.status());
        int deletedCount = jobInfoMapper.selectCount(search);

        res.put("totalCount", totalCount);
        res.put("normalCount", normalCount);
        res.put("pausedCount", pausedCount);
        res.put("deletedCount", deletedCount);
        return res;
    }

    /**
     * 查询指定时间范围内的任务执行数据报表
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<String, Object> getReportStatistic(Date startDate, Date endDate) {
        // 折线图x轴数据
        List<String> line_x = new ArrayList<>();
        // 折线图-执行中-y轴数据
        List<Integer> line_running_y = new ArrayList<>();
        // 折线图-执行成功-y轴数据
        List<Integer> line_success_y = new ArrayList<>();
        // 折线图-执行失败-y轴数据
        List<Integer> line_fail_y = new ArrayList<>();

        // 饼图数据
        Integer pie_running_r = 0;
        Integer pie_success_r = 0;
        Integer pie_fail_r = 0;

        Example example = new Example(JobLogReport.class);
        example.setOrderByClause("day");
        example.createCriteria().andBetween("day", startDate, endDate);
        List<JobLogReport> reports = jobLogReportMapper.selectByExample(example);
        while (DateUtil.compare(startDate, endDate) < 0) {
            line_x.add(DateUtil.formatDate(startDate));
            int runningCount = 0;
            int successCount = 0;
            int failCount = 0;
            if (reports != null) {
                final Date compareDate = startDate;
                Optional<JobLogReport> first = reports.stream().filter(v -> DateUtil.compare(v.getDay(), compareDate) == 0).findFirst();
                if (first.isPresent()) {
                    JobLogReport findInDB = first.get();
                    runningCount = findInDB.getRunningCount();
                    successCount = findInDB.getSuccessCount();
                    failCount = findInDB.getFailCount();
                }
            }
            line_running_y.add(runningCount);
            line_success_y.add(successCount);
            line_fail_y.add(failCount);

            pie_running_r += runningCount;
            pie_success_r += successCount;
            pie_fail_r += failCount;

            startDate = DateUtil.offsetDay(startDate, 1);
        }

        Map<String, Object> result = new HashMap<>();
        // 折线图数据
        result.put("line_x", line_x);
        result.put("line_running_y", line_running_y);
        result.put("line_success_y", line_success_y);
        result.put("line_fail_y", line_fail_y);

        // 饼图数据
        result.put("pie_running_r", pie_running_r);
        result.put("pie_success_r", pie_success_r);
        result.put("pie_fail_r", pie_fail_r);

        return result;
    }

    /**
     * 根据id查找指定任务
     * @param jobInfoId
     */
    public JobInfo selectJobInfoById(Integer jobInfoId) {
        return jobInfoMapper.selectByPrimaryKey(jobInfoId);
    }
}
