package org.dream.scheduled.tasks.unit.tests;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.dream.scheduled.tasks.configuration.properties.CheckinConfigurationProperties;
import org.dream.scheduled.tasks.service.CheckinService;
import org.dream.scheduled.tasks.service.HttpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CheckinServiceUnitTest {

    @Mock
    private HttpService httpService;
    
    @InjectMocks
    private CheckinService checkinService;
    
    @Mock
    private CheckinConfigurationProperties checkinConfigurationProperties;
    
    @BeforeEach
    private void before() throws IOException {
        when(httpService.makeUrlEncodedPost(anyString(), anyString(), anyBoolean(), anyString()))
            .thenReturn("true");
        
        when(checkinConfigurationProperties.getTokenUrl())
            .thenReturn("");
    }
    
	@Test
	public void givenUsernameAndSecret_whenGetBearerToken_thenCallsHttpServiceOnce() throws IOException {
	    // Arrange
	    String username = "george_chou";
	    String secret = "61N8eo8GZju10B3fbxFaS0XmO5Oz9m2tWsSW2Wlb9N7BEey3OONur04P8JoW2w44";
	    
	    String destination = "";
	    String queryString = "grant_type=password&username=george_chou&password=61N8eo8GZju10B3fbxFaS0XmO5Oz9m2tWsSW2Wlb9N7BEey3OONur04P8JoW2w44";
	    boolean requiresToken = false;
	    String token = null;
	    
	    // Act
	    checkinService.getBearerToken(username, secret);
	    
	    // Assert
	    verify(httpService, times(1)).makeUrlEncodedPost(destination, queryString, requiresToken, token);
	    
	}

}
