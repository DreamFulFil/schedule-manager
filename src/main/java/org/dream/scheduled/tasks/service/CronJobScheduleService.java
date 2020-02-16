package org.dream.scheduled.tasks.service;


import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.dream.scheduled.tasks.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.entity.CronJobSchedule;
import org.dream.scheduled.tasks.entity.TaskSubmitter;
import org.dream.scheduled.tasks.repository.CronJobScheduleRepository;
import org.dream.scheduled.tasks.util.JSONUtil;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CronJobScheduleService {
    
    private Scheduler scheduler;
    private CronJobScheduleRepository cronJobScheduleRepository;
    private CheckinService checkinService;
    private MailService mailService;
    private TaskSubmitterService taskSubmitterService;
    
    /**
     * Constructor Injection
     * @param scheduler
     * @param cronJobScheduleRepository
     * @param checkinService
     * @param mailService
     */
    public CronJobScheduleService(
            Scheduler scheduler, 
            CronJobScheduleRepository cronJobScheduleRepository,
            CheckinService checkinService, 
            MailService mailService,
            TaskSubmitterService taskSubmitterService) {
        
        this.scheduler = scheduler;
        this.cronJobScheduleRepository = cronJobScheduleRepository;
        this.checkinService = checkinService;
        this.mailService = mailService;
        this.taskSubmitterService = taskSubmitterService;
    }

    @PostConstruct
    public void init() {
        try {
            scheduler.start();
            this.recoverCronSchedules();
        }
        catch(SchedulerException ex) {
            log.error("Error while creating scheduler:{}", ex);
        }
    }

    public CronJobSchedule setupCronSchedule(String identifier, String taskParameters, String cronExp) {
        final String jobName = "[CRONJOB]-" + identifier + new Date().getTime();
        
        CronJobSchedule cronJobSchedule = null;
        if(identifier.indexOf("[CRONJOB]") < 0) { // 第一次註冊不會有串 [CRONJOB]
            cronJobSchedule = new CronJobSchedule();
            cronJobSchedule.setScheduledDate(new Date());
            cronJobSchedule.setCronExpression(cronExp);
            cronJobSchedule.setUniqueName(identifier.indexOf("[CRONJOB]") < 0 ? jobName : identifier);
            cronJobSchedule.setCancelled(false);
            cronJobSchedule.setTaskParameters(taskParameters);
        }
        else {
            cronJobSchedule = cronJobScheduleRepository.findByUniqueName(identifier);
        }
            
        try {
            //Create JobDetail
            MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
            jobDetail.setTargetObject(new CheckinCronJob(taskParameters));
            jobDetail.setTargetMethod("execute");
            jobDetail.setName(jobName);
            jobDetail.setConcurrent(false);
            jobDetail.afterPropertiesSet();
            
            //Create Trigger
            CronTriggerFactoryBean  cronTrigger = new CronTriggerFactoryBean();
            cronTrigger.setBeanName("TASK_CRONJOB-"+ new Date());
            cronTrigger.setCronExpression(cronExp);
            cronTrigger.afterPropertiesSet();
            
            scheduler.scheduleJob((JobDetail) jobDetail.getObject(), cronTrigger.getObject());
            log.info("Scheduled job:"+jobName);
        }
        catch(NoSuchMethodException | SchedulerException | ParseException | ClassNotFoundException ex) {
            log.error("Error while setting up cron job: {}", ex);
        }
        
        return cronJobScheduleRepository.save(cronJobSchedule);
    }
    

    public boolean updateCronSchedule(String identifier, String taskParameters, String cronExp, boolean scheduled) {
        CronJobSchedule cronJobSchedule = cronJobScheduleRepository.findByUniqueName(identifier);
        if(cronJobSchedule == null) {
            return false;
        }
        String uniqueJobName = cronJobSchedule.getUniqueName();
        JobKey jobKey = JobKey.jobKey(uniqueJobName);
        
        try {
            //取消狀態下重啟系統，會在Scheduler裡找不到該Job，
            //所以要重新註冊一個CronJob。
            if(!scheduler.checkExists(jobKey)) {
                this.setupCronSchedule(identifier, taskParameters, cronExp);
                return true;
            }
            if(scheduled) {
                log.info("Resuming job:"+jobKey);
                //若要重新啟用排程，但Cron規則被更改，則移除本來的Job。
                if(!cronJobSchedule.getCronExpression().equals(cronExp)) {
                    scheduler.deleteJob(jobKey);
                    this.setupCronSchedule(identifier, taskParameters, cronExp);
                } else {
                    scheduler.resumeJob(jobKey);
                }
            } else {
                log.info("Pausing job:"+jobKey);
                scheduler.pauseJob(jobKey);
            }
        }
        catch(SchedulerException ex) {
            log.error("Error while attempting to pause cron job:", ex);
        }
        cronJobSchedule.setCancelled(!scheduled);
        cronJobSchedule.setCronExpression(cronExp);
        cronJobScheduleRepository.save(cronJobSchedule);
        return true;
    }

    public boolean removeCronSchedule(String identifier) {
        CronJobSchedule cronJobSchedule = cronJobScheduleRepository.findByUniqueName(identifier);
        if(cronJobSchedule == null) {
            return false;
        }
        String uniqueJobName = cronJobSchedule.getUniqueName();
        JobKey jobKey = JobKey.jobKey(uniqueJobName);
        try {
            if(!scheduler.checkExists(jobKey)) {
                return false;
            }
            scheduler.deleteJob(jobKey);
            log.info("Removed job:"+jobKey);
        }
        catch(SchedulerException ex) {
            log.error("Error while attempting to remove cron job:", ex);
        }
        cronJobScheduleRepository.delete(cronJobSchedule);
        return true;
    }

    public List<CronJobSchedule> recoverCronSchedules() {
        List<CronJobSchedule> cronJobSchedules = cronJobScheduleRepository.findAll();
        for(CronJobSchedule cronJobSchedule: cronJobSchedules) {
            //如果不是被取消的才啟動
            if(!cronJobSchedule.isCancelled()) {
                log.info("Recovering job:" + cronJobSchedule.getUniqueName());
                this.setupCronSchedule(cronJobSchedule.getUniqueName(), cronJobSchedule.getTaskParameters(), cronJobSchedule.getCronExpression());
            }
        }
        return cronJobSchedules;
    }
    
    public List<CronJobSchedule> findAllCronJobSchedulesByName(String name) {
        TaskSubmitter taskSubmitter = taskSubmitterService.findByName(name);
        if(taskSubmitter != null) {
            List<CronJobSchedule> cronJobSchedules = taskSubmitter.getCronJobSchedules();
            if(CollectionUtils.isEmpty(cronJobSchedules)) {
                return new ArrayList<>();
            }
            return cronJobSchedules;
        }
        return new ArrayList<>();
    }
    
    /**
     * 打卡的 Task
     * @author George-Chou
     *
     */
    private class CheckinCronJob {
        private final String taskParameters;
        public CheckinCronJob(String taskParameters) {
            this.taskParameters = taskParameters;
        }
        @SuppressWarnings("unused")
        public void execute() {
            CheckinParamsDto checkinParams = JSONUtil.fromJsonString(taskParameters, CheckinParamsDto.class);
            try {
                String result = checkinService.checkin(checkinParams);
                log.info("打卡結果:" + result);
                boolean mailSent = mailService.sendEmail(Arrays.asList(checkinParams.getMail()), "打卡結果", result);
                if(!mailSent) {
                    log.info("發信失敗");
                }
            }
            catch(IOException ex) {
                log.error("打卡時發生錯誤", ex);
            }
        }     
    }

}

