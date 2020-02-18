package org.dream.scheduled.tasks.repository;

import org.dream.scheduled.tasks.entity.CronJobSchedule;
import org.dream.scheduled.tasks.entity.TaskSubmitter;
import org.springframework.data.jpa.repository.Query;

public interface TaskSubmitterRepository extends BaseRepository<TaskSubmitter, Integer> {

    TaskSubmitter findByName(String name);
    
    @Query("SELECT t FROM TaskSubmitter t WHERE ?1 MEMBER OF t.cronJobSchedules ")
    TaskSubmitter findInCronJobSchedule(CronJobSchedule cronJobSchedule);
    
}
