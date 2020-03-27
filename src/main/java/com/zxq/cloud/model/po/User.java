package com.zxq.cloud.model.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author zxq
 */
@Table(name = "zxq_job_user")
@Data
public class User {
    /**
     * 主键
     */
    @Id
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

}