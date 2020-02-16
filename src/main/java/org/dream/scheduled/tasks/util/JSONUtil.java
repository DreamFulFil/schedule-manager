package org.dream.scheduled.tasks.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JSONUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static <T> String toJsonString(T object) {
        String taskParams = null;
        try {
            taskParams = mapper.writeValueAsString(object);
        }
        catch(Exception ex) {
            log.error("物件轉JSON時發生錯誤", ex);
        }
        return taskParams;
    }
    
    public static <T> T fromJsonString(String json, Class<T> targetClass) {
        T readValue = null;
        try {
            readValue = mapper.readValue(json, targetClass);
        }
        catch(JsonProcessingException ex) {
            log.error("JSON轉物件時發生錯誤", ex);
        }
        return readValue;
    }
    
}
