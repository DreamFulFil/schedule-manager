package org.dream.scheduled.tasks.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlEncodedPostParam {
    private String url; 
    private String queryString; 
    private boolean requiresToken; 
    private String token;
}
