package org.dream.scheduled.tasks.repository;

import org.dream.scheduled.tasks.entity.TaskSubmitter;

public interface TaskSubmitterRepository extends BaseRepository<TaskSubmitter, Integer> {

    TaskSubmitter findByName(String name);
    
}
