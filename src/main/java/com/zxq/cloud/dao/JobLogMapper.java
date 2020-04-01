package com.zxq.cloud.dao;

import com.zxq.cloud.model.bo.JobLogBO;
import com.zxq.cloud.model.po.JobLog;
import com.zxq.cloud.model.query.JobLogQuery;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zxq
 */
public interface JobLogMapper extends Mapper<JobLog> {

    /**
     * 查找任务运行日志
     * @param jobLogQuery
     * @return
     */
    List<JobLogBO> selectJobLog(@Param(value = "query") JobLogQuery jobLogQuery);

}