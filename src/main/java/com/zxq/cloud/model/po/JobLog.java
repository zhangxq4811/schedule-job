package com.zxq.cloud.model.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author zxq
 */
@Table(name = "zxq_job_log")
@Data
public class JobLog {
    /**
     * 主键
     */
    @Id
    @GeneratedValue(generator="JDBC")
    private Integer id;

    /**
     * 任务id，关联zxq_job_detail
     */
    @Column(name = "job_info_id")
    private Integer jobInfoId;

    /**
     * 执行状态:0-执行失败 1-执行成功
     */
    @Column(name = "execute_status")
    private Integer executeStatus;

    /**
     * 执行参数
     */
    @Column(name = "execute_params")
    private String executeParams;

    /**
     * 执行失败原因
     */
    @Column(name = "execute_fail_msg")
    private String executeFailMsg;

    /**
     * 执行时间
     */
    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 任务执行消耗时间 单位：毫秒
     */
    @Column(name = "consume_time")
    private Long consumeTime;

}