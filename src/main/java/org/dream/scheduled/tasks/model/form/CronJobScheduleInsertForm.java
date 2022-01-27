package org.dream.scheduled.tasks.model.form;

import lombok.Data;

@Data
public class CronJobScheduleInsertForm {

    private String username;
    private String secret;
    private String description;
    private String checkinTime;
    private String overtime;
    private String cronExpression;
    private String mail;
    
}
