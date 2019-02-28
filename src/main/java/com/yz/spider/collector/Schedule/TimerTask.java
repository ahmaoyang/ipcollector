package com.yz.spider.collector.Schedule;

import com.yz.spider.collector.Crawler.IPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 调度任务类
 *
 * @author maoyang
 * @data 2019-02-26
 */
@Configuration
@EnableScheduling
public class TimerTask implements SchedulingConfigurer {


    Logger logger = LoggerFactory.getLogger(TimerTask.class);
    private static String cron = "0 0 1 * * ?";
    private final IPService iPService;

    public TimerTask(IPService iPService) {
        this.iPService = iPService;
    }

    /**
     * 定时爬ip
     */
    public void copyIps() {
        logger.info("开始爬取Ip:" +LocalDateTime.now());
        try {
            iPService.copyIps();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
        logger.info("爬取Ip结束:" + LocalDateTime.now());
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                // 任务逻辑
                copyIps();
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                // 任务触发，可修改任务的执行周期
                CronTrigger trigger = new CronTrigger(cron);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
            }
        });
    }
}
