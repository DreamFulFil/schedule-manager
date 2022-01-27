package org.dream.scheduled.tasks.service.rest;

import java.util.List;

import org.dream.scheduled.tasks.converter.CronJobScheduleToDtoConverter;
import org.dream.scheduled.tasks.model.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.model.entity.CronJobSchedule;
import org.dream.scheduled.tasks.model.entity.TaskSubmitter;
import org.dream.scheduled.tasks.model.form.CronJobScheduleInsertForm;
import org.dream.scheduled.tasks.model.form.CronJobScheduleUpdateForm;
import org.dream.scheduled.tasks.service.CronJobScheduleService;
import org.dream.scheduled.tasks.service.TaskSubmitterService;
import org.dream.scheduled.tasks.util.JSONUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("task")
public class TaskService {

    private CronJobScheduleService cronJobScheduleService;
    private TaskSubmitterService taskSubmitterService;
    private CronJobScheduleToDtoConverter cronJobScheduleToDtoConverter;
    
    /**
     * Constructor for injection services
     * 
     * @param cronJobScheduleService
     * @param taskSubmitterService
     * @param cronJobScheduleToDtoConverter
     */
    public TaskService(
            CronJobScheduleService cronJobScheduleService, 
            TaskSubmitterService taskSubmitterService,
            CronJobScheduleToDtoConverter cronJobScheduleToDtoConverter) {
        this.cronJobScheduleService = cronJobScheduleService;
        this.taskSubmitterService = taskSubmitterService;
        this.cronJobScheduleToDtoConverter = cronJobScheduleToDtoConverter;
    }

    @PostMapping("/")
    public String registerTask(@RequestBody CronJobScheduleInsertForm form) {
        CheckinParamsDto checkinParams = cronJobScheduleToDtoConverter.convert(form);
        TaskSubmitter submitter = taskSubmitterService.findByNameAndSaveIfNotExists(checkinParams.getUsername(), checkinParams.getSecret());
        String taskParams = JSONUtil.toJsonString(checkinParams);
        CronJobSchedule cronSchedule = cronJobScheduleService.setupCronSchedule(form.getUsername(), taskParams, form.getCronExpression());
        submitter.addCronJobSchedule(cronSchedule);
        taskSubmitterService.save(submitter);
        
        String format = "成功註冊任務:%s";
        return String.format(format, cronSchedule.getUniqueName());
    }
    
    @PutMapping("/")
    public String updateTask(@RequestBody CronJobScheduleUpdateForm form) {
        CheckinParamsDto checkinParams = cronJobScheduleToDtoConverter.convert(form);
        String taskParams = JSONUtil.toJsonString(checkinParams);
        boolean updated = cronJobScheduleService.updateCronSchedule(form.getIdentifier(), taskParams, form.getCronExpression(), form.isDisable());
        if(updated) {
            return "成功更新任務";
        }
        return "更新任務失敗";
    }
    
    @DeleteMapping("/{identifier}")
    public String deleteTask(@PathVariable String identifier) {
        boolean removed = cronJobScheduleService.removeCronSchedule(identifier);
        if(removed) {
            return "成功刪除任務" + identifier;
        }
        return "刪除任務失敗";
    }
    
    @GetMapping("/{name}")
    public List<CronJobSchedule> getAllSchedulesByUser(@PathVariable String name) {
        return cronJobScheduleService.findAllCronJobSchedulesByName(name);
    }
}
