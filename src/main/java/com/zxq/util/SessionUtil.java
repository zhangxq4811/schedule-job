package com.zxq.util;

import cn.hutool.core.util.StrUtil;
import com.zxq.model.po.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

/**
 * @author zxq
 * @date 2020/3/27 14:09
 **/
public class SessionUtil {

    /**
     * 登录验证码存放在session的key
     */
    public static final String VERITY_CODE_IN_SESSION_KEY = "USER_AUTH_VERITY_CODE";

    /**
     * 用户信息存放在session的key
     */
    public static final String USER_INFO_IN_SESSION_KEY = "USER_INFO";

    /**
     * 获取session
     * @return
     */
    private static HttpSession getSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs.getRequest().getSession();
    }

    /**
     * 将登录验证码存放在session中
     * @param verityCode
     */
    public static void putVerityCode(String verityCode) {
        if (StrUtil.isNotBlank(verityCode)) {
            HttpSession session = getSession();
            session.setAttribute(VERITY_CODE_IN_SESSION_KEY, verityCode);
        }
    }

    /**
     * 获取session中的登录验证码
     */
    public static String getVerityCode() {
        HttpSession session = getSession();
        return (String) session.getAttribute(VERITY_CODE_IN_SESSION_KEY);
    }

    /**
     * 删除session中的登录验证码
     */
    public static void removeVerityCode() {
        HttpSession session = getSession();
        session.removeAttribute(VERITY_CODE_IN_SESSION_KEY);
    }

    /**
     * 将用户信息存放在session中
     * @param user
     */
    public static void putUserInfo(User user) {
        if (user != null) {
            HttpSession session = getSession();
            session.setAttribute(USER_INFO_IN_SESSION_KEY, user);
        }
    }

    /**
     * 删除session中的用户信息
     */
    public static void removeUserInfo() {
        HttpSession session = getSession();
        session.removeAttribute(USER_INFO_IN_SESSION_KEY);
    }

    /**
     * 获取session中的用户信息
     * @return
     */
    public static User getUserInfo() {
        HttpSession session = getSession();
        return (User) session.getAttribute(USER_INFO_IN_SESSION_KEY);
    }

    /**
     * 判断当前登录的用户是否为管理员
     * @return
     */
    public static boolean isSuperUser() {
        User userInfo = getUserInfo();
        if (userInfo != null && userInfo.getRole() == 1) {
            return true;
        }
        return false;
    }

}
