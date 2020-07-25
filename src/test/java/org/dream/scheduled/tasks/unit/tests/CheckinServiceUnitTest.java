package org.dream.scheduled.tasks.unit.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.dream.scheduled.tasks.configuration.properties.CheckinConfigurationProperties;
import org.dream.scheduled.tasks.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.service.CheckinService;
import org.dream.scheduled.tasks.service.HttpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckinServiceUnitTest {

    // 主要要試的元件所相依的物件，要提供 Mock，注入一個假的物件
    @Mock
    private HttpService httpService;
    
    @Mock
    private CheckinConfigurationProperties checkinConfigurationProperties;
    
    
    // 主要要測試的元件
    @InjectMocks
    private CheckinService checkinService;
    
	@Test
	public void givenUsernameAndSecret_whenGetBearerToken_thenCallsHttpServiceOnce() throws IOException {
	    // Arrange
	    // 呼叫 getBearerToken 所需要的參數
	    String username = "test_user";
	    String secret = "test_secret";
	    
	    // 實際呼叫相依物件 HttpService 的 makeUrlEncodedPost 時會收到的參數
	    String destination = null;
	    String queryString = "grant_type=password&username=test_user&password=test_secret";
	    boolean requiresToken = false;
	    String token = null;
	    
	    // Act
	    checkinService.getBearerToken(username, secret);
	    
	    // Assert
	    verify(httpService, times(1)).makeUrlEncodedPost(destination, queryString, requiresToken, token);
	}
	
	@Test
	public void givenCheckinNotEnabled_whenCheckin_thenReturnsNotEnabled() throws IOException {
	    // Arrange
	    
	    // 讓這個測試不要被系統時間綁住，寫死在 2020/02/26 08:40
	    LocalDateTime fixedTime = LocalDateTime.of(2020,2,26,8,40,0,0);
	    Clock clock = Clock.fixed(fixedTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
	    checkinService.setClock(clock);
	    
	    CheckinParamsDto checkinParams = mock(CheckinParamsDto.class);
	    when(checkinConfigurationProperties.isEnabled()).thenReturn(false);
	    when(checkinConfigurationProperties.getHolidays()).thenReturn("");
	    when(checkinConfigurationProperties.getCheckinTime()).thenReturn("08:30");
	    String expectedResult = "自動打卡功能未開啟";
	    
	    // Act
	    String result = checkinService.checkin(checkinParams);
	    
	    // Assert
	    assertEquals(expectedResult,result);
	}
	
	@Test
    public void givenWeekend_whenCheckin_thenReturnsDontCheckinOnWeekends() throws IOException {
	    // 讓這個測試不要被系統時間綁住，寫死在 2020/02/29 08:40(週末)
        LocalDateTime fixedTime = LocalDateTime.of(2020,2,29,8,40,0,0);
        Clock clock = Clock.fixed(fixedTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        checkinService.setClock(clock);
        
        CheckinParamsDto checkinParams = mock(CheckinParamsDto.class);
        when(checkinConfigurationProperties.getCheckinTime()).thenReturn("08:30");
        String expectedResult = "週末打什麼卡";
        
        // Act
        String result = checkinService.checkin(checkinParams);
        
        // Assert
        assertEquals(expectedResult,result);
	}
	
	@Test
    public void givenNationalHoliday_whenCheckin_thenReturnsDontCheckinOnWeekends() throws IOException {
        // 讓這個測試不要被系統時間綁住，寫死在 2020/02/28 08:40(國定假)
        LocalDateTime fixedTime = LocalDateTime.of(2020,2,28,8,40,0,0);
        Clock clock = Clock.fixed(fixedTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        checkinService.setClock(clock);
        
        CheckinParamsDto checkinParams = mock(CheckinParamsDto.class);
        when(checkinConfigurationProperties.getHolidays()).thenReturn("2020/02/28");
        when(checkinConfigurationProperties.getCheckinTime()).thenReturn("08:30");
        String expectedResult = "國定假日不打卡！";
        
        // Act
        String result = checkinService.checkin(checkinParams);
        
        // Assert
        assertEquals(expectedResult,result);
    }

}
