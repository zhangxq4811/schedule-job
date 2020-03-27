package com.zxq.cloud.controller;

import com.zxq.cloud.model.po.JobInfo;
import com.zxq.cloud.model.query.JobInfoQuery;
import com.zxq.cloud.model.vo.PageVO;
import com.zxq.cloud.service.JobService;
import com.zxq.cloud.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 页面跳转控制器
 * @author zxq
 * @date 2020/3/27 13:22
 **/
@Controller
public class ForwardController {

    @Autowired
    private JobService jobService;

    /**
     * 跳转到登录页
     * @return
     */
    @RequestMapping("/login")
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView("login");
        return modelAndView;
    }

    /**
     * 跳转到首页
     * @return
     */
    @RequestMapping("/index")
    public ModelAndView index(){
        ModelAndView modelAndView = new ModelAndView("main/index");
        modelAndView.addObject("userInfo", SessionUtil.getUserInfo());
        return modelAndView;
    }

    /**
     * 跳转到任务列表
     * @return
     */
    @RequestMapping("/jobList")
    public ModelAndView jobList(){
        ModelAndView modelAndView = new ModelAndView("main/job/job-list");
        PageVO<JobInfo> pageVO = jobService.selectJob(new JobInfoQuery());
        modelAndView.addObject("pageVO", pageVO);
        return modelAndView;
    }

}
