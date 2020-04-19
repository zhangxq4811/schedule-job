package com.zxq.interceptor;

import com.zxq.model.po.User;
import com.zxq.util.SessionUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 * @author zxq
 * @date 2020/4/16 15:23
 **/
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User userInfo = SessionUtil.getUserInfo();
        // session中用户信息为空，跳转到登录页面
        if (userInfo == null) {
            response.sendRedirect(request.getContextPath() + "/login");
        }
        return true;

    }
}
