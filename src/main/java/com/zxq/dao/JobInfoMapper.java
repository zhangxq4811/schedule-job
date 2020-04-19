package com.zxq.dao;

import com.zxq.model.bo.JobInfoBO;
import com.zxq.model.po.JobInfo;
import com.zxq.model.query.JobInfoQuery;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zxq
 */
public interface JobInfoMapper extends Mapper<JobInfo> {

    /**
     * 查询jobInfo
     * @param query
     * @return
     */
    List<JobInfoBO> selectJobInfo(@Param(value = "query") JobInfoQuery query);

}