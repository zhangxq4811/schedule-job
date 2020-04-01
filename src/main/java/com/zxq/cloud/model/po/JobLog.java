package com.zxq.cloud.model.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
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
    private Integer id;

    /**
     * 任务id，关联zxq_job_detail
     */
    @Column(name = "job_info_id")
    private Integer jobInfoId;

    /**
     * 执行状态:0-失败 1-成功
     */
    @Column(name = "execute_status")
    private Integer executeStatus;

    /**
     * 执行参数
     */
    @Column(name = "execute_params")
    private String executeParams;

    /**
     * 执行结果:成功存放执行结果，失败存放失败原因
     */
    @Column(name = "execute_result")
    private String executeResult;

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