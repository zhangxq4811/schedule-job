package com.zxq.cloud.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.zxq.cloud.constant.JobConstant;
import com.zxq.cloud.dao.UserMapper;
import com.zxq.cloud.model.po.User;
import com.zxq.cloud.util.SessionUtil;
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

    /**
     * 修改密码
     * @param oldPwd
     * @param newPwd
     * @return
     */
    public String editPwd(String oldPwd, String newPwd) {
        User userInfo = SessionUtil.getUserInfo();
        String oldSign = StrUtil.concat(false, userInfo.getUsername(), oldPwd);
        String oldPassword = SecureUtil.md5(oldSign);
        if (!userInfo.getPassword().equals(oldPassword)) {
            return "原始密码不正确";
        }
        if (oldPwd.equals(newPwd)) {
            return "新密码不能与原始密码一致";
        }
        String newSign = StrUtil.concat(false, userInfo.getUsername(), newPwd);
        String newPassword = SecureUtil.md5(newSign);
        userInfo.setPassword(newPassword);
        userMapper.updateByPrimaryKeySelective(userInfo);
        // 更新session中userInfo
        SessionUtil.putUserInfo(userInfo);
        return JobConstant.SUCCESS_CODE;
    }
}
