package org.dream.scheduled.tasks.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dream.scheduled.tasks.dto.UrlEncodedPostParam;
import org.springframework.stereotype.Service;

@Service
public class HttpService {

    public String makeUrlEncodedPost(UrlEncodedPostParam postParams) throws IOException {
        URL url = new URL(postParams.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        byte[] postData = postParams.getQueryString().getBytes( StandardCharsets.UTF_8 );

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0(X11;Linux X86_64...)Gecko/20100101 Firefox/68.0");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        
        if(postParams.isRequiresToken()) {
            conn.setRequestProperty("Authorization", "Bearer " + postParams.getToken());
        }
        
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setUseCaches(false);
        
        try(OutputStream dataOutputStream = new BufferedOutputStream(new DataOutputStream(conn.getOutputStream()))) {
            dataOutputStream.write(postData);
         }
        
        try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String output;
            StringBuilder response = new StringBuilder();
            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            if(postParams.isRequiresToken()) {
                return response.toString();
            }
            return parseTextWithSingleGroup(response.toString());
        }
    }
    
    private String parseTextWithSingleGroup(String text) {
        String patternRegex = ".*\"access_token\":\"(.*)\",\"token_type\".*";
        Pattern pattern = Pattern.compile(patternRegex);
        Matcher matcher = pattern.matcher(text);
        if(matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }
    
}
