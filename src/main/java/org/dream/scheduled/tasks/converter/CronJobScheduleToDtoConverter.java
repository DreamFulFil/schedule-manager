package org.dream.scheduled.tasks.converter;

import org.dream.scheduled.tasks.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.form.CronJobScheduleForm;
import org.dream.scheduled.tasks.util.AESUtil;
import org.springframework.stereotype.Component;

@Component
public class CronJobScheduleToDtoConverter {

    public CheckinParamsDto convert(CronJobScheduleForm cronJobScheduleForm) {
        CheckinParamsDto checkinParams = new CheckinParamsDto();
        checkinParams.setUsername(cronJobScheduleForm.getUsername());
        checkinParams.setDescription(cronJobScheduleForm.getDescription());
        checkinParams.setOvertime(cronJobScheduleForm.getOvertime());
        checkinParams.setSecret(AESUtil.encrypt(cronJobScheduleForm.getSecret()));
        return checkinParams;
    }
    
}
