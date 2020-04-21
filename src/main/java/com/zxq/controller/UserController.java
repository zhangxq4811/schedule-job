package com.zxq.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.StrUtil;
import com.zxq.constant.JobConstant;
import com.zxq.model.po.User;
import com.zxq.model.query.UserQuery;
import com.zxq.model.vo.PageVO;
import com.zxq.model.vo.ResultVO;
import com.zxq.service.UserService;
import com.zxq.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户处理控制器
 * @author zxq
 * @date 2020/3/23 18:29
 **/
@Controller
@RequestMapping("user")
@Slf4j
public class UserController {

    /**
     * 用户service
     */
    @Autowired
    private UserService userService;

    /**
     * 获取验证码
     * @param response
     */
    @RequestMapping("/getVerityCode")
    public void getVerityCode(HttpServletResponse response) throws IOException {
        //定义图形验证码的长和宽
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100);
        String code = captcha.getCode();
        log.info("user getVerityCode = {}", code);
        //将验证码存放至session中
        SessionUtil.putVerityCode(code);
        //图形验证码写出到流
        captcha.write(response.getOutputStream());
    }

    /**
     * 登录验证
     * @param username
     * @param password
     * @param verityCode
     */
    @RequestMapping("/auth")
    @ResponseBody
    public ResultVO auth(@RequestParam String username, @RequestParam String password, @RequestParam String verityCode) {
        String verityCodeInSession = SessionUtil.getVerityCode();
        if (StrUtil.isBlank(verityCodeInSession) || !verityCodeInSession.equals(verityCode)) {
            return ResultVO.failure("验证码错误");
        } else {
            User user = userService.auth(username, password);
            if (user != null) {
                SessionUtil.putUserInfo(user);
                SessionUtil.removeVerityCode();
                return ResultVO.success();
            } else {
                return ResultVO.failure("用户名或密码错误");
            }
        }
    }

    /**
     * 修改密码
     * @param oldPwd
     * @param newPwd
     * @return
     */
    @RequestMapping("/editPwd")
    @ResponseBody
    public ResultVO editPwd(@RequestParam String oldPwd, @RequestParam String newPwd) {
        if (StrUtil.isBlank(oldPwd) || StrUtil.isBlank(newPwd)) {
            return ResultVO.failure("参数缺失");
        }
        String res = userService.editPwd(oldPwd, newPwd);
        if (JobConstant.SUCCESS_CODE.equals(res)) {
            return ResultVO.success("修改成功");
        } else {
            return ResultVO.failure(res);
        }
    }

    /**
     * 分页获取用户数据
     * @param userQuery
     * @return
     */
    @RequestMapping("/pageUser")
    @ResponseBody
    public ResultVO pageUser(UserQuery userQuery) {
        PageVO<User> page = userService.selectUser(userQuery);
        return ResultVO.success(page);
    }

    /**
     * 新增用户
     * @param user
     * @return
     */
    @RequestMapping("/addUser")
    @ResponseBody
    public ResultVO addUser(User user) {
        if (StrUtil.isBlank(user.getUsername())) {
            return ResultVO.failure("用户名不能为空");
        }
        if (StrUtil.isBlank(user.getPassword())) {
            return ResultVO.failure("登录密码不能为空");
        }
        if (user.getRole() == null) {
            return ResultVO.failure("请设置用户角色");
        }
        String res = userService.insertUser(user);
        if (JobConstant.SUCCESS_CODE.equals(res)) {
            return ResultVO.success();
        } else {
            return ResultVO.failure(res);
        }
    }

    /**
     * 删除用户 - 普通用户无删除权限
     * @param userId
     * @return
     */
    @RequestMapping("/removeUser")
    @ResponseBody
    public ResultVO pageUser(@RequestParam Integer userId) {
        String res = userService.deleteUser(userId);
        if (JobConstant.SUCCESS_CODE.equals(res)) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.failure(res);
        }
    }

}
