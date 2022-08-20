package com.qc.mvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 功能描述: task service
 *
 * @author lijinhua
 * @date 2022/8/20 11:19
 */
@Component
public class TaskService {
    //logger
    final Logger logger = LoggerFactory.getLogger(getClass());

    //启动延迟60s,并以60s的间隔执行任务
    @Scheduled(initialDelay = 60_000, fixedRate = 60_000)
    /**  fixedDelay不包含任务时间,rate包含任务时间*/
    public void checkSystemStatusEveryMinute() {
        logger.info("Start check system status...");
    }

    //时间统一配置,,冒号后是默认时间
    @Scheduled(initialDelay = 30_000,fixedDelayString = "${task.checkDiskSpace:30000}")
    public void checkDiskSpaceEveryMinute() {
        logger.info("Start check disk space...");
    }

    //定时执行一次任务
    /** 秒 分 小时 天 月份 星期 年(可省略)
     *  如果每天都来可以给天加通配符，其他也一样
     * */

    //每天2:15执行
    @Scheduled(cron = "${task.report:0 15 2 * * *}")
    public void cronDailyReport() {
        logger.info("Start daily report task...");
    }

    //周一12:00执行
    @Scheduled(cron = "${task.weekday:0 0 12 * * MON-FRI}")
    public void cronWeekdayTask() {
        logger.info("Start weekday task...");
    }

    /**
     * 在Spring中使用定时任务和Cron任务都十分简单，但是要注意到，这些任务的调度都是在每个JVM进程中的。
     * 如果在本机启动两个进程，或者在多台机器上启动应用，这些进程的定时任务和Cron任务都是独立运行的，互不影响。
     * Quartz
     * 这是分布式服务的协同问题了，zookeeper就是一个协调者，不过对于廖老师提的这个问题要数据持久化，
     * 那zookeeper存储的数据量有限，可能不合适。
     * 可以使用数据库，把每个任务的执行情况记录下，
     * 多台JVM执行同一个定时任务时，并发在DB中创建一个记录，使用唯一键保证只有一个创建成功。
     * Quartz好像也是类似的实现机制吧
     *
     * or
     * 集群中固定一台主机执行定时任务，根据配置文件固定一台主机
     *
     * */












}
