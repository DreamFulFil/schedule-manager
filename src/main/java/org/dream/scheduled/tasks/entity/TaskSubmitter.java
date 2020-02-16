package org.dream.scheduled.tasks.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.util.CollectionUtils;

import lombok.Data;

@Data
@Entity
@Table(name = "TASK_SUBMMITER")
public class TaskSubmitter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "submitter")
    private String name;
    
    @Column(name = "secret")
    private String secret;
    
    @OneToMany
    private List<CronJobSchedule> cronJobSchedules;
    
    public void addCronJobSchedule(CronJobSchedule cronJobSchedule) {
        if(CollectionUtils.isEmpty(cronJobSchedules)) {
            cronJobSchedules = new ArrayList<>();
        }
        cronJobSchedules.add(cronJobSchedule);
    }
    
}
