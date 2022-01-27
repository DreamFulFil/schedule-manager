package org.dream.scheduled.tasks.model.dto;

import java.util.Map;

public interface RequestKeyValuePair {
    
    String getQueryString();

    Map<String, String> toMap();
    
}
