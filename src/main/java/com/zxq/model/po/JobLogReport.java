package com.zxq.model.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 任务日志报表实体
 * @author zxq
 */
@Table(name = "schedule_job_log_report")
@Data
public class JobLogReport {
    @Id
    @GeneratedValue(generator="JDBC")
    private Integer id;

    /**
     * 调度-时间
     */
    private Date day;

    /**
     * 运行中-日志数量
     */
    @Column(name = "running_count")
    private Integer runningCount;

    /**
     * 执行成功-日志数量
     */
    @Column(name = "success_count")
    private Integer successCount;

    /**
     * 执行失败-日志数量
     */
    @Column(name = "fail_count")
    private Integer failCount;

}