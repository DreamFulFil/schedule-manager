package org.dream.scheduled.tasks.converter;

import org.dream.scheduled.tasks.model.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.model.form.CronJobScheduleInsertForm;
import org.dream.scheduled.tasks.model.form.CronJobScheduleUpdateForm;
import org.dream.scheduled.tasks.util.AESUtil;
import org.springframework.stereotype.Component;

@Component
public class CronJobScheduleToDtoConverter {

    public CheckinParamsDto convert(CronJobScheduleInsertForm cronJobScheduleForm) {
        CheckinParamsDto checkinParams = new CheckinParamsDto();
        checkinParams.setUsername(cronJobScheduleForm.getUsername());
        checkinParams.setCheckinTime(cronJobScheduleForm.getCheckinTime());
        checkinParams.setDescription(cronJobScheduleForm.getDescription());
        checkinParams.setOvertime(cronJobScheduleForm.getOvertime());
        checkinParams.setSecret(AESUtil.encrypt(cronJobScheduleForm.getSecret()));
        checkinParams.setMail(cronJobScheduleForm.getMail());
        return checkinParams;
    }
    
    public CheckinParamsDto convert(CronJobScheduleUpdateForm cronJobScheduleForm) {
        CheckinParamsDto checkinParams = new CheckinParamsDto();
        checkinParams.setUsername(cronJobScheduleForm.getUsername());
        checkinParams.setDescription(cronJobScheduleForm.getDescription());
        checkinParams.setOvertime(cronJobScheduleForm.getOvertime());
        checkinParams.setSecret(AESUtil.encrypt(cronJobScheduleForm.getSecret()));
        checkinParams.setMail(cronJobScheduleForm.getMail());
        return checkinParams;
    }
    
}
