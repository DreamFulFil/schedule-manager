package org.dream.scheduled.tasks.form;

import lombok.Data;

@Data
public class CronJobScheduleInsertForm {

    private String username;
    private String secret;
    private String description;
    private String overtime;
    private String cronExpression;
    private String mail;
    
}
