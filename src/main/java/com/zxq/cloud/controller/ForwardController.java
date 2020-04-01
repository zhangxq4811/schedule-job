package com.zxq.cloud.controller;

import com.zxq.cloud.model.po.JobGroup;
import com.zxq.cloud.service.JobService;
import com.zxq.cloud.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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
    @RequestMapping("/job-list")
    public ModelAndView jobList(){
        ModelAndView modelAndView = new ModelAndView("main/job/job-list");
        modelAndView.addObject("jobGroupList", jobService.selectJobGroup());
        return modelAndView;
    }

    /**
     * 任务日志页面
     * @param jobInfoId
     * @return
     */
    @RequestMapping("/job-log")
    public ModelAndView jobLog(@RequestParam(name = "jobInfoId") String jobInfoId){
        ModelAndView modelAndView = new ModelAndView("main/job/job-log");
        modelAndView.addObject("jobInfoId", jobInfoId);
        return modelAndView;
    }

    /**
     * 新添任务页面
     * @return
     */
    @RequestMapping("/add-job")
    public ModelAndView addJob(){
        ModelAndView modelAndView = new ModelAndView("main/job/add-job");
        List<JobGroup> groupList = jobService.selectJobGroup();
        modelAndView.addObject("jobGroupList", groupList);
        return modelAndView;
    }

    /**
     * 编辑任务页面
     * @return
     */
    @RequestMapping("/edit-job")
    public ModelAndView editJob(@RequestParam(name = "jobInfoId") String jobInfoId){
        ModelAndView modelAndView = new ModelAndView("main/job/edit-job");
        List<JobGroup> groupList = jobService.selectJobGroup();
        modelAndView.addObject("jobGroupList", groupList);
        return modelAndView;
    }

}
