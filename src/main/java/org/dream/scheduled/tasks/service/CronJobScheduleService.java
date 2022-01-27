package org.dream.scheduled.tasks.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.dream.scheduled.tasks.model.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.model.dto.ResultDto;
import org.dream.scheduled.tasks.model.entity.CronJobSchedule;
import org.dream.scheduled.tasks.model.entity.TaskSubmitter;
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

    /**
     * @author George-Chou
     * @param  identifier      排程識別字元
     * @param  taskParameters  排程所需參數
     * @param  cronExp         排程的 cron expression
     * @return 成功設定的排程
     * 
     * 目的:
     * 1. 在 Scheduler 中建立一個排程，並啟用
     * 2. 在資料庫中備份排程資訊，以利系統重啟時從資料庫將排程設定同步回 Scheduler
     */
    public CronJobSchedule setupCronSchedule(String identifier, String taskParameters, String cronExp) {
        // Scheduler 中 識別排程的唯一值
        final String jobName = "[CRONJOB]-" + identifier + new Date().getTime();
        
        // 建立資料庫中的排程記錄(重啟時的依據)
        CronJobSchedule cronJobSchedule = null;
        if(identifier.indexOf("[CRONJOB]") < 0) { // 第一次註冊不會有串 [CRONJOB]
            cronJobSchedule = new CronJobSchedule();
            cronJobSchedule.setScheduledDate(new Date());
            cronJobSchedule.setCronExpression(cronExp);
            cronJobSchedule.setUniqueName(identifier.indexOf("[CRONJOB]") < 0 ? jobName : identifier);
            cronJobSchedule.setCancelled(false); // 預設啟用
            cronJobSchedule.setTaskParameters(taskParameters);
        }
        else {
            cronJobSchedule = cronJobScheduleRepository.findByUniqueName(identifier);
        }
            
        // 建立 Scheduler IN-MEMORY 的排程
        try {
            //Create JobDetail
            MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
            jobDetail.setTargetObject(new CheckinCronJob(taskParameters));
            jobDetail.setTargetMethod("execute");
            jobDetail.setName(cronJobSchedule.getUniqueName());
            jobDetail.setConcurrent(false);
            jobDetail.afterPropertiesSet();
            
            //Create Trigger
            CronTriggerFactoryBean  cronTrigger = new CronTriggerFactoryBean();
            cronTrigger.setBeanName("TASK_CRONJOB-"+ new Date().getTime());
            cronTrigger.setCronExpression(cronExp);
            cronTrigger.afterPropertiesSet();
            
            scheduler.scheduleJob((JobDetail) jobDetail.getObject(), cronTrigger.getObject());
            log.info("Scheduled job:"+ cronJobSchedule.getUniqueName());
        }
        catch(NoSuchMethodException | SchedulerException | ParseException | ClassNotFoundException ex) {
            log.error("設定排程時發生錯誤", ex);
        }
        
        return cronJobScheduleRepository.save(cronJobSchedule);
    }
    

    /**
     * @author George-Chou
     * @param  identifier      排程識別字元
     * @param  taskParameters  排程所需參數
     * @param  cronExp         排程的 cron expression
     * @param  disabled        目前此排程關閉與否(非刪除)
     * @return 更新排程的成功與否
     * 
     * 目的:
     * 1. 讓使用者能更動排程參數
     * 2. 讓使用者能開啟或關閉某個排程(非刪除)
     * 3. 讓使用者能更改排程時間
     */
    public boolean updateCronSchedule(String identifier, String taskParameters, String cronExp, boolean disabled) {
        CronJobSchedule cronJobSchedule = cronJobScheduleRepository.findByUniqueName(identifier);
        if(cronJobSchedule == null) {
            return false;
        }
        String uniqueJobName = cronJobSchedule.getUniqueName();
        JobKey jobKey = JobKey.jobKey(uniqueJobName);
        
        try {
            // 取消狀態下重啟系統，會在In memory 的 Scheduler 裡找不到該Job，
            // 所以要重新註冊一個CronJob。
            if(!scheduler.checkExists(jobKey)) {
                this.setupCronSchedule(identifier, taskParameters, cronExp);
                return true;
            }
            if(disabled) {
                log.info("重啟排程:"+jobKey);
                // 若要重新啟用排程，但Cron規則被更改，則移除本來的Job，
                // 移除的原因是沒辦法直接更動 Scheduler 中的 Cron Expression。
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
        cronJobSchedule.setCancelled(!disabled);
        cronJobSchedule.setCronExpression(cronExp);
        cronJobScheduleRepository.save(cronJobSchedule);
        return true;
    }

    /**
     * @author George-Chou
     * @param  identifier 排程的識別字元
     * @return 移除成功與否
     * 
     * 目的:
     * 在排程確實存在的情況下
     * 1. 清掉 Scheduler 中的排程
     * 2. 同步清掉資料庫中記錄的排程
     */
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
        TaskSubmitter taskSubmitter = taskSubmitterService.findByCronJobSchedule(cronJobSchedule);
        List<CronJobSchedule> cronJobSchedules = taskSubmitter.getCronJobSchedules();
        cronJobSchedules.remove(cronJobSchedule);
        taskSubmitterService.save(taskSubmitter);
        cronJobScheduleRepository.delete(cronJobSchedule);
        return true;
    }

    /**
     * @author George-Chou
     * @return 重啟的排程清單
     * 
     * 目的: 
     * 因應系統重啟時記憶體被清空的問題，
     * 依資料庫記錄的排程重新建立排程到 Scheduler 中
     * 但僅限 isCancelled 為 false 的資料會被重新建立
     * 
     * STEP 1: 找到所有的排程
     * STEP 2: 重啟未被取消的排程
     */
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
    
    /**
     * @author George-Chou
     * @param  name 註冊者
     * @return 該註冊者所註冊的所有排程
     * 
     * 目的:
     * 1. 註冊者存在的時候回傳他所註冊的排程
     * 2. 註冊者不存在時回傳空的 List
     */
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
    
    private class CheckinCronJob {
        private final String taskParameters;
        public CheckinCronJob(String taskParameters) {
            this.taskParameters = taskParameters;
        }
        
        @SuppressWarnings("unused")
        public void execute() {
            CheckinParamsDto checkinParams = JSONUtil.fromJsonString(taskParameters, CheckinParamsDto.class);
            try {
                checkinService.checkin(checkinParams);
                ResultDto validateCheckinTimeResult = checkinService.getValidateCheckinTimeResultThreadLocal().get();
                String resultMessage = validateCheckinTimeResult.getMessage();
                log.info(LocalDateTime.now().toLocalDate() + " 打卡結果:" + resultMessage);
                boolean mailSent = mailService.sendEmail(Arrays.asList(checkinParams.getMail()), "打卡結果", resultMessage);
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

