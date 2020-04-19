package com.zxq.config;

import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;

/**
 * @author zxq
 * @date 2020/3/24 15:16
 * springboot 2.x中，已经默认支持了quartz，提供了调度器工厂（SchedulerFactory）和调度器的bean的定义
 **/
@Deprecated
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
