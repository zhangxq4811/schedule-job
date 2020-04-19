package com.zxq.dao;

import com.zxq.model.po.JobLogReport;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author zxq
 */
public interface JobLogReportMapper extends Mapper<JobLogReport> {

    /**
     * 指定报表运行中任务数量+1
     * @param id
     */
    void increaseRunningCount(Integer id);

    /**
     * 指定报表运行成功任务数量+1,运行中任务数量-1
     * @param id
     */
    void increaseSuccessCount(Integer id);

    /**
     * 指定报表运行失败任务数量+1,运行中任务数量-1
     * @param id
     */
    void increaseFailCount(Integer id);

}