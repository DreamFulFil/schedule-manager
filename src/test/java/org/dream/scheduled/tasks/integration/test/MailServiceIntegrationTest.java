package org.dream.scheduled.tasks.integration.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dream.scheduled.tasks.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(value = "test")
public class MailServiceIntegrationTest {
    
    @Autowired
    private MailService mailService;

    private File getAttachment01(){
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource("testFiles" + File.separator + "attachment01.txt").getFile());
    }
    private File getAttachment02(){
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource("testFiles" + File.separator + "attachment02.txt").getFile());
    }

    private List<String> getFilePath() {
        return Arrays.asList(getAttachment01().getAbsolutePath(), getAttachment02().getAbsolutePath());
    }
    @Test
    
    public void serviceInjected_true() {
        assertNotNull(mailService);
    }
    
    @Test
    public void givenValidSingleToAndSubjectAndText_WhenSendEmail_returnsTrue() {
        //Arrange
        final String to = "george_chou@gss.com.tw";
        final String subject = "Test Email";
        final String text = "This is a test email from mail-sender";
        final boolean expected = true;
        
        //Act
        boolean actual = mailService.sendEmail(Arrays.asList(to), subject, text);
        
        //Assert
        assertEquals(expected, actual);
    }
    
    @Test
    public void givenValidSingleToAndSubjectAndHtmlBody_whenSendEmail_returnsTrue() {
        //Arrange
        final String to = "george_chou@gss.com.tw";
        final String subject = "Test Email";
        
        final StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("<h1>This is a test email from mail-sender</h1><br />");
        textBuilder.append("<h2>This is a test email from mail-sender</h2><br />");
        textBuilder.append("<h3>This is a test email from mail-sender</h3>");
        
        final boolean expected = true;
        
        //Act
        boolean actual = mailService.sendEmail(Arrays.asList(to), subject, textBuilder.toString());
        
        //Assert
        assertEquals(expected, actual);
    }
    
    @Test
    public void givenValidSingleToAndSubjectAndTextAndMultipleAttachments_whenSendEmail_returnsTrue() {
        //Arrange
        final String to = "george_chou@gss.com.tw";
        final String subject = "Test Email";
        final String text = "<h1>This is a test email with attachments from mail-sender</h1>";
        final List<String> pathToAttachments = getFilePath();
        final boolean expected = true;
        
        //Act
        boolean actual = mailService.sendEmail(Arrays.asList(to), null, null, subject, text, pathToAttachments);
        
        //Assert
        assertEquals(expected, actual);
    }
    
    @Test
    public void givenValidSingleToAndCcAndBccAndSubjectAndTextAndMultipleAttachments_whenSendEmail_returnsTrue() {
        //Arrange
        final List<String> to = Arrays.asList("unciax_wu@gss.com.tw");
        final List<String> cc = Arrays.asList("sandra_yang@gss.com.tw");
        final List<String> bcc = Arrays.asList("george_chou@gss.com.tw");
        final String subject = "Test Email";
        final String text = "This is a test email with attachments from mail-sender";
        final List<String> pathToAttachments = getFilePath();
        final boolean expected = true;
        
        //Act
        boolean actual = mailService.sendEmail(to, cc, bcc, subject, text, pathToAttachments);
        
        //Assert
        assertEquals(expected, actual);
    }
    
}
