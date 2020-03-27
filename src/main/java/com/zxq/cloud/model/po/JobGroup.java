package com.zxq.cloud.model.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zxq
 */
@Table(name = "zxq_job_group")
@Data
public class JobGroup {
    /**
     * 主键
     */
    @Id
    private Integer id;

    /**
     * 分组名
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

}