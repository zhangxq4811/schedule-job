package com.zxq;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zxq
 * @date 2020/3/23 11:23
 **/
public class QuartzTest {

    private static Logger logger = LoggerFactory.getLogger(QuartzTest.class);

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            bindJob(scheduler, "我是第一个job");
            bindJob(scheduler, "我是另外一个job");
            scheduler.start();
        } catch (SchedulerException se) {
            logger.error("调度器初始化异常", se);
        }
    }

    public static class QuartzJob1 implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            JobDataMap mergedJobDataMap = jobExecutionContext.getMergedJobDataMap();
            Object msg = mergedJobDataMap.get("msg");
            System.out.println(msg+",被执行了一次");
        }
    }

    public static void bindJob(Scheduler scheduler, String msg) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(QuartzJob1.class).build();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("msg", msg);
            CronTrigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withSchedule(CronScheduleBuilder.cronSchedule("*/1 * * * * ?")).build();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (SchedulerException se) {
            logger.error("调度器初始化异常", se);
        }
    }

}