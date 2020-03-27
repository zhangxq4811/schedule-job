package com.zxq.cloud.config;

import org.quartz.Scheduler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;

/**
 * @author zxq
 * @date 2020/3/24 15:16
 **/
@Configuration
public class QuartzConfig {

    /**
     * 通过SchedulerFactoryBean获取Scheduler的实例
     * @return
     * @throws IOException
     */
    public Scheduler scheduler() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        return factory.getScheduler();
    }
}
