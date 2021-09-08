package org.dream.scheduled.tasks.integration.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Clock;

import org.dream.scheduled.tasks.configuration.properties.CheckinConfigurationProperties;
import org.dream.scheduled.tasks.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.dto.ResultDto;
import org.dream.scheduled.tasks.service.CheckinService;
import org.dream.scheduled.tasks.service.HttpService;
import org.dream.scheduled.tasks.util.AESUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(value = "test")
public class CheckinServiceIntegrationTest {

    @Autowired private CheckinService checkinService;
    @Autowired private CheckinConfigurationProperties checkinConfigurationProperties;
    @Autowired private HttpService httpService;
    private Clock clock = Clock.systemDefaultZone();
    
    @Test
    public void serviceInjected_true() {
        assertNotNull(checkinService);
        assertNotNull(checkinConfigurationProperties);
        assertNotNull(httpService);
    }
    
    @Test
    public void givenValidCheckinTime_whenCheckin_returnSuccessCheckinResult() throws IOException {
        // Arrange
        String expected = "打卡成功";
        
        checkinService.setCheckinConfigurationProperties(checkinConfigurationProperties);
        checkinService.setHttpService(httpService);
        checkinService.setClock(clock);
        
        CheckinParamsDto dto = new CheckinParamsDto();
        dto.setUsername("george_chou");
        dto.setSecret(AESUtil.encrypt("tZp0tayQUlijevdNx5o6Pe4VW6na5sPprSpPCBPm9ohn2CAKHXJY3BmLvtdTG7a4"));
        dto.setCheckinTime("08:00");
        dto.setDescription("上班打卡");
        dto.setMail("george_chou@gss.com.tw");
        dto.setOvertime("N");
        
        // Act
        checkinService.checkin(dto);
        ResultDto result = checkinService.getValidateCheckinTimeResultThreadLocal().get();
        String actual = result.getMessage();
        
        // Assert
        assertTrue(actual.indexOf(expected) >=0 );
        
    }
    
}
