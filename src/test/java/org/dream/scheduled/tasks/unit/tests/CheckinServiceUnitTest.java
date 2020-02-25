package org.dream.scheduled.tasks.unit.tests;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.dream.scheduled.tasks.configuration.properties.CheckinConfigurationProperties;
import org.dream.scheduled.tasks.service.CheckinService;
import org.dream.scheduled.tasks.service.HttpService;
import org.junit.jupiter.api.BeforeEach;
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
    
    @BeforeEach
    private void before() throws IOException {
        when(checkinConfigurationProperties.getTokenUrl()).thenReturn("");
    }
    
	@Test
	public void givenUsernameAndSecret_whenGetBearerToken_thenCallsHttpServiceOnce() throws IOException {
	    // Arrange
	    String username = "test_user";
	    String secret = "test_secret";
	    
	    String destination = "";
	    String queryString = "grant_type=password&username=test_user&password=test_secret";
	    boolean requiresToken = false;
	    String token = null;
	    
	    // Act
	    checkinService.getBearerToken(username, secret);
	    
	    // Assert
	    verify(httpService, times(1)).makeUrlEncodedPost(destination, queryString, requiresToken, token);
	    
	}

}
