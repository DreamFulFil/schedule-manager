package org.dream.scheduled.tasks.form;

import lombok.Data;

@Data
public class CronJobScheduleUpdateForm {

    private String identifier;
    private String username;
    private String secret;
    private String description;
    private String overtime;
    private String cronExpression;
    private String mail;
    private boolean disable;
    
}
