package com.zxq.cloud.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zxq.cloud.dao.JobGroupMapper;
import com.zxq.cloud.dao.JobInfoMapper;
import com.zxq.cloud.dao.JobLogMapper;
import com.zxq.cloud.model.bo.JobInfoBO;
import com.zxq.cloud.model.po.JobGroup;
import com.zxq.cloud.model.po.JobLog;
import com.zxq.cloud.model.query.JobInfoQuery;
import com.zxq.cloud.model.query.JobLogQuery;
import com.zxq.cloud.model.vo.PageVO;
import com.zxq.cloud.util.JobUtil;
import lombok.extern.slf4j.Slf4j;
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
     * 查询http任务列表
     * @param jobInfoQuery
     * @return
     */
    public PageVO<JobInfoBO> selectJob(JobInfoQuery jobInfoQuery) {
        PageVO<JobInfoBO> page = new PageVO<>();
        int pageNum = Optional.ofNullable(jobInfoQuery.getPage()).map(v -> v-1).orElse(0);
        int pageSize = Optional.ofNullable(jobInfoQuery.getLimit()).orElse(10);
        PageHelper.startPage(pageNum, pageSize);
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
    public PageVO<JobLog> selectJobLog(JobLogQuery jobLogQuery) {
        PageVO<JobLog> page = new PageVO<>();
        Example example = new Example(JobLog.class);
        example.setOrderByClause("execute_time desc");
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("jobInfoId", jobLogQuery.getJobInfoId());

        int pageNum = Optional.ofNullable(jobLogQuery.getPage()).map(v -> v-1).orElse(0);
        int pageSize = Optional.ofNullable(jobLogQuery.getLimit()).orElse(10);
        PageHelper.startPage(pageNum, pageSize);
        List<JobLog> jobLogs = jobLogMapper.selectByExample(example);
        PageInfo<JobLog> pageInfo = new PageInfo<>(jobLogs);
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
}
