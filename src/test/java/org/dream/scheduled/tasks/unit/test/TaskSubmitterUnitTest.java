package org.dream.scheduled.tasks.unit.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.dream.scheduled.tasks.model.entity.CronJobSchedule;
import org.dream.scheduled.tasks.model.entity.TaskSubmitter;
import org.dream.scheduled.tasks.repository.TaskSubmitterRepository;
import org.dream.scheduled.tasks.service.TaskSubmitterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskSubmitterUnitTest {
    
    @Mock
    private TaskSubmitterRepository taskSubmitterRepository;

    @InjectMocks
    private TaskSubmitterService taskSubmitterService;

    @Test
    public void givenUsername_whenFindByName_invokesRepositoryMethodOnce() {
        final String dummyName = "DUMMY";
        taskSubmitterService.findByName(dummyName);
        verify(taskSubmitterRepository, times(1)).findByName(dummyName);
    }

    @Test
    public void givenNullOrEmptyUsername_whenFindByName_throwsIllegalArgumentException () {
        final String dummyName = null;
        assertThrows(IllegalArgumentException.class, () -> {
            taskSubmitterService.findByName(dummyName);
        });
    }

    @Test
    public void givenUsernameAndSecretAndNonExistingUser_whenFindByNameAndSaveIfNotExists_callsSaveOnce() {
        final String dummyName = "DUMMY";
        final String dummySecret = "SECRET";
        TaskSubmitterService spy = spy(taskSubmitterService);
        spy.findByNameAndSaveIfNotExists(dummyName, dummySecret);
        verify(spy, times(1)).save(any());
    }

    @Test
    public void givenUsernameAndSecretAndExistingUser_whenFindByNameAndSaveIfNotExists_doNotCallSave() {
        final String dummyName = "DUMMY";
        final String dummySecret = "SECRET";
        final TaskSubmitter fakeExistingSubmitter = new TaskSubmitter();
        TaskSubmitterService spy = spy(taskSubmitterService);
        doReturn(fakeExistingSubmitter).when(spy).findByName(dummyName);
        spy.findByNameAndSaveIfNotExists(dummyName, dummySecret);
        verify(spy, times(0)).save(any());
    }

    @Test
    public void givenNullOrEmptyUsernameOrSecret_whenFindByNameAndSaveIfNotExists_throwsIllegalArgumentException () {
        final String dummyName = "DUMMY";
        final String dummySecret = null;
        assertThrows(IllegalArgumentException.class, () -> {
            taskSubmitterService.findByNameAndSaveIfNotExists(dummyName, dummySecret);
        });
    }

    @Test
    public void givenValidTaskSubmitter_whenSave_thenInvokeRepositoryMethodOnce() {
        final TaskSubmitter fakeExistingSubmitter = new TaskSubmitter();
        taskSubmitterService.save(fakeExistingSubmitter);
        verify(taskSubmitterRepository, times(1)).save(fakeExistingSubmitter);
    }

    @Test
    public void givenNullOrEmptyTaskSubmitter_whenSave_thenThrowsInvalidArgumentException() {
        final TaskSubmitter fakeExistingSubmitter = null;
        assertThrows(IllegalArgumentException.class, () -> {
            taskSubmitterService.save(fakeExistingSubmitter);
        });
    }

    @Test
    public void givenValidCronSchedule_whenfindByCronJobSchedule_thenInvokeRepositoryMethodOnce() {
        final CronJobSchedule fakeCronJobSchedule = new CronJobSchedule();
        taskSubmitterService.findByCronJobSchedule(fakeCronJobSchedule);
        verify(taskSubmitterRepository, times(1)).findInCronJobSchedule(fakeCronJobSchedule);
    }

}
