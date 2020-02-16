package org.dream.scheduled.tasks.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "CRON_JOB_SCHEDULE")
public class CronJobSchedule {

    /** 流水號*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /** 此排程被設定的時間*/
    @Column(name = "SCHEDULED_DATE")
    private Date scheduledDate;
    
    /** Cron 表示式*/
    @Column(name = "CRON_EXPRESSION")
    private String cronExpression;
    
    /** Quartz Job的唯一識別值*/
    @Column(name = "SCHEDULE_UNIQUE_NAME")
    private String uniqueName;
    
    /** 暫時取消*/
    @Column(name = "IS_CANCELLED")
    private boolean cancelled = false;
    
    /** 任務資訊 (JSON格式記錄) */
    @Column(name = "CREDENTIALS")
    private String taskParameters;
    
}