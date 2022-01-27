package org.dream.scheduled.tasks.model.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data 
public class CheckinParamsDto implements RequestKeyValuePair{
    private String username;
    private String secret;
    private String mail;
    
    private String checkinTime;
    private String description;
    private String overtime;   // Y OR N
    
    @Override
    @JsonIgnore
    public String getQueryString() {
        String format = "time=%s&description=%s&isOT=%s";
        return String.format(format, getCheckinTime(), getDescription(), getOvertime());
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        
        map.put("username", username);
        map.put("secret", secret);
        map.put("mail", mail);
        map.put("checkinTime", checkinTime);
        map.put("description", description);
        map.put("overtime", overtime);
        
        return map;
    }
}