package com.zxq.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zxq
 * @date 2020/4/16 15:33
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    private final static List<String> EXCLUDE_PATH = new ArrayList<>();

    static {
        // 静态资源
        EXCLUDE_PATH.add("/css/**");
        EXCLUDE_PATH.add("/fonts/**");
        EXCLUDE_PATH.add("/images/**");
        EXCLUDE_PATH.add("/js/**");
        EXCLUDE_PATH.add("/lib/**");
        //
        EXCLUDE_PATH.add("/login");
        EXCLUDE_PATH.add("/user/auth");
        EXCLUDE_PATH.add("/user/getVerityCode");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATH);
    }

    /**
     * 允许跨域携带cookie
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }


}
