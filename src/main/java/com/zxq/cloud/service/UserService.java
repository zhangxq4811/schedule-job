package com.zxq.cloud.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.github.pagehelper.PageInfo;
import com.zxq.cloud.constant.JobConstant;
import com.zxq.cloud.dao.UserMapper;
import com.zxq.cloud.model.po.User;
import com.zxq.cloud.model.query.UserQuery;
import com.zxq.cloud.model.vo.PageVO;
import com.zxq.cloud.util.PageHelperUtil;
import com.zxq.cloud.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 分页获取用户列表
     * @param userQuery
     * @return
     */
    public PageVO<User> selectUser(UserQuery userQuery) {
        PageVO<User> page = new PageVO<>();
        Example example = new Example(User.class);
        if (userQuery != null && StrUtil.isNotBlank(userQuery.getUsername())) {
            example.createCriteria().andLike("username", "%" + userQuery.getUsername() + "%");
        }
        PageHelperUtil.startPage(userQuery.getPage(), userQuery.getLimit());
        List<User> users = userMapper.selectByExample(example);
        PageInfo<User> pageInfo = new PageInfo<>(users);
        if (pageInfo != null && pageInfo.getTotal() > 0) {
            page.setList(users);
            page.setTotal((int)pageInfo.getTotal());
        }
        return page;
    }

    /**
     * 删除用户
     * @param userId
     * @return
     */
    public String deleteUser(Integer userId) {
        User userInfo = SessionUtil.getUserInfo();
        if (!SessionUtil.isSuperUser()) {
            return "权限不足";
        } else if (userInfo.getId().equals(userId)) {
            return "无法删除自己";
        }
        User userInDB = userMapper.selectByPrimaryKey(userId);
        if (userInDB == null) {
            return "删除用户不存在";
        }
        userMapper.deleteByPrimaryKey(userId);
        return JobConstant.SUCCESS_CODE;
    }

    /**
     * 新增用户
     * @param user
     * @return
     */
    public String insertUser(User user) {
        if (!SessionUtil.isSuperUser()) {
            return "权限不足";
        }
        User query = new User();
        query.setUsername(user.getUsername());
        int count = userMapper.selectCount(query);
        if (count > 0) {
            return "该用户名已被占用";
        }
        // 密码加密
        String sign = StrUtil.concat(false, user.getUsername(), user.getPassword());
        String pwd = SecureUtil.md5(sign);
        user.setPassword(pwd);
        user.setCreateTime(DateUtil.date());
        userMapper.insertSelective(user);
        return JobConstant.SUCCESS_CODE;
    }
}
