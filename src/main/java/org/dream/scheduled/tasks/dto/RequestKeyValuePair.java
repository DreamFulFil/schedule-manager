package org.dream.scheduled.tasks.dto;

import java.util.Map;

public interface RequestKeyValuePair {
    
    String getQueryString();

    Map<String, String> toMap();
    
}
