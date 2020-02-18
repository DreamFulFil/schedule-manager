package org.dream.scheduled.tasks.service;

import org.dream.scheduled.tasks.entity.CronJobSchedule;
import org.dream.scheduled.tasks.entity.TaskSubmitter;
import org.dream.scheduled.tasks.repository.TaskSubmitterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskSubmitterService {

    @Autowired
    private TaskSubmitterRepository taskSubmitterRepository;
    
    public TaskSubmitter findByName(String name) {
        return taskSubmitterRepository.findByName(name);
    }
    
    /**
     * 依註冊者名稱查找資料
     * 如果註冊者不存在就建一個新的
     * 存在就直接回傳，用來綁定 submitter 與 cronJob
     * 
     * @param name   註冊者名稱
     * @param secret 註冊者秘密
     * 
     * @return 註冊者
     */
    public TaskSubmitter findByNameAndSaveIfNotExists(String name, String secret) {
        TaskSubmitter submitter = this.findByName(name);
        if(submitter == null) {
            submitter = new TaskSubmitter();
            submitter.setName(name);
            submitter.setSecret(secret);
            return this.save(submitter);
        }
        return submitter;
    }
    
    public TaskSubmitter save(TaskSubmitter taskSubmitter) {
        return taskSubmitterRepository.save(taskSubmitter);
    }
    
    public TaskSubmitter findByCronJobSchedule(CronJobSchedule cronJobSchedule) {
        return taskSubmitterRepository.findInCronJobSchedule(cronJobSchedule);
    }
    
}
