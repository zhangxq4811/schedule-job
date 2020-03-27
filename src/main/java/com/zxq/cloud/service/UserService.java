package com.zxq.cloud.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.zxq.cloud.dao.UserMapper;
import com.zxq.cloud.model.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zxq
 * @date 2020/3/23 18:30
 **/
@Service
@Slf4j
public class UserService {

    /**
     * UserMapper
     */
    @Resource
    public UserMapper userMapper;

    /**
     * 用户名、密码校验
     * @param username
     * @param password
     * @return
     */
    public User auth(String username, String password) {
        if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
            String sign = StrUtil.concat(false, username, password);
            String pwd = SecureUtil.md5(sign);
            User query = new User();
            query.setUsername(username);
            query.setPassword(pwd);
            return userMapper.selectOne(query);
        }
        return null;
    }
}
