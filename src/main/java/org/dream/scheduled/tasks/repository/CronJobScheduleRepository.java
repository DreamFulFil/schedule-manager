package org.dream.scheduled.tasks.repository;

import org.dream.scheduled.tasks.entity.CronJobSchedule;

public interface CronJobScheduleRepository extends BaseRepository<CronJobSchedule, Integer> {
    
    CronJobSchedule findByUniqueName(String uniqueName);
}
