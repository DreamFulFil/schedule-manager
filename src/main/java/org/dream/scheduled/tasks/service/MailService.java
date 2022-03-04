package org.dream.scheduled.tasks.service;

import java.io.File;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.dream.scheduled.tasks.util.RegexValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class MailService {
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    public boolean sendEmail(List<String> to, List<String> cc, List<String> bcc, String subject, String text, List<String> pathToAttachments) {
        boolean validTo = RegexValidationUtil.validateEmails(to);
        boolean validCc = (cc == null) ? Boolean.TRUE: RegexValidationUtil.validateEmails(cc);
        boolean validBcc = (bcc == null ) ? Boolean.TRUE : RegexValidationUtil.validateEmails(bcc);
        if(!validTo || !validCc || !validBcc) {
            return false;
        }
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
            messageHelper.setTo(to.toArray(new String[0]));
            messageHelper.setSubject(subject);
            messageHelper.setText(text, true);
            
            if(!CollectionUtils.isEmpty(cc)) {
                messageHelper.setCc(cc.toArray(new String[0]));
            }
            
            if(!CollectionUtils.isEmpty(bcc)) {
                messageHelper.setBcc(bcc.toArray(new String[0]));
            }
            
            if(CollectionUtils.isEmpty(pathToAttachments)) {
                for(String pathToAttachment : pathToAttachments) {
                    FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
                    messageHelper.addAttachment(file.getFilename(), file);
                }
            }
            javaMailSender.send(message);
        }
        catch(Exception ex) {
            throw new MailSendException("Error occured while sending the mail, cause:", ex); 
        }
        return true;
    }
    
    public boolean sendEmail(List<String> to, List<String> cc, List<String> bcc, String subject, String text) {
        return sendEmail(to, cc, bcc, subject, text, null);
    }

    public boolean sendEmail(List<String> to, String subject, String text) {
        return sendEmail(to, null, null, subject, text);
    }

    public boolean sendEmail(List<String> to, List<String> cc, String subject, String text) {
        return sendEmail(to, cc, null, subject, text);
    }

}
