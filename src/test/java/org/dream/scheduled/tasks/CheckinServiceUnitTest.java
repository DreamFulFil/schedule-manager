package org.dream.scheduled.tasks;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.dream.scheduled.tasks.service.CheckinService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CheckinServiceUnitTest {

    @Autowired
    private CheckinService checkinService;
    
	@Test
	public void givenUsernameAndSecret_whenGetBearerToken_thenReturnsNonemptyString() throws IOException {
	    // Arrange
	    String username = "george_chou";
	    String secret = "61N8eo8GZju10B3fbxFaS0XmO5Oz9m2tWsSW2Wlb9N7BEey3OONur04P8JoW2w44";
	    
	    // Act
	    String bearerToken = checkinService.getBearerToken(username, secret);
	    System.err.println(bearerToken);
	    
	    // Assert
	    assertTrue(bearerToken != null && 
	               !bearerToken.equals(""));
	    
	}

}
