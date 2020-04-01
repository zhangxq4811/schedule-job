package com.zxq.cloud.service;

import com.github.pagehelper.PageInfo;
import com.zxq.cloud.dao.JobGroupMapper;
import com.zxq.cloud.dao.JobInfoMapper;
import com.zxq.cloud.dao.JobLogMapper;
import com.zxq.cloud.model.bo.JobInfoBO;
import com.zxq.cloud.model.bo.JobLogBO;
import com.zxq.cloud.model.po.JobGroup;
import com.zxq.cloud.model.query.JobInfoQuery;
import com.zxq.cloud.model.query.JobLogQuery;
import com.zxq.cloud.model.vo.PageVO;
import com.zxq.cloud.util.JobUtil;
import com.zxq.cloud.util.PageHelperUtil;
import lombok.extern.slf4j.Slf4j;
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
}
