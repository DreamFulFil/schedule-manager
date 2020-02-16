package org.dream.scheduled.tasks.service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.dream.scheduled.tasks.configuration.properties.CheckinConfigurationProperties;
import org.dream.scheduled.tasks.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CheckinService {
    
    @Autowired
    private CheckinConfigurationProperties checkinConfigurationProperties;
    
    @Autowired
    private HttpService httpService;
    
    
    /**
     * @author George-Chou
     * @return 取得 OAuth 的 Bearer token
     * @throws IOException
     * @param username 使用者名稱
     * @param secret   使用者秘密
     * 
     */
    public String getBearerToken(String username, String secret) throws IOException {
        String destination = checkinConfigurationProperties.getTokenUrl();
        String format = "grant_type=password&username=%s&password=%s";
        String params = String.format(format, username, secret); 
        
        return httpService.makeUrlEncodedPost(destination, params, false, null);
    }
    
    /**
     * @author George-Chou
     * @return 實際打卡
     * @throws IOException
     * 
     * STEP 1: 取得打卡 URL 及 時間
     * STEP 2: 檢查是否為週末或是國定假日
     * STEP 3: 檢查是不是還不到可以打卡的時間
     */
    public String checkin(CheckinParamsDto checkinParams) throws IOException {
        // STEP 1
        String destination = checkinConfigurationProperties.getCheckinUrl();
        String checkinTime = checkinParams.getCheckinTime();
        if(checkinTime == null) {
            checkinTime = getDefaultCheckinTime();
            checkinParams.setCheckinTime(checkinTime);
        }
        
        // STEP 2
//        if(isWeekend()) { return "週末打什麼卡"; }
        if(isNationalHoliday()) { return "Today is national holiday, no need to check in"; }
        
        // STEP 3
        if(checkinTime == null || checkinTime == "") {
            return "Can't check in yet";
        }
        else {
            if(checkinConfigurationProperties.isEnabled()) {
                String token = getBearerToken(checkinParams.getUsername(), AESUtil.decrypt(checkinParams.getSecret()));
                log.info("Token:" + token);
                return httpService.makeUrlEncodedPost(destination, checkinParams.getQueryString(), true, token);
            }
            else {
                return "Automated checkin not enabled";
            }
        }
    }
    
    /**
     * @author George-Chou
     * @return 打卡時間
     * 
     * 預設是「上」班打卡限定在 08:29:59:999 與 17:30:00:000 之間
     * 預設是「下」班打卡限定在 17:30:00:000 之後
     */
    private String getDefaultCheckinTime() {
        String checkinTime = null;
        
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int monthValue = now.getMonthValue();
        int dayOfMonth = now.getDayOfMonth();
        
        if(now.isAfter(LocalDateTime.of(year, monthValue, dayOfMonth, 8, 29, 59, 999)) && 
           now.isBefore(LocalDateTime.of(year, monthValue, dayOfMonth, 17, 30, 0, 0))) {
            checkinTime = checkinConfigurationProperties.getCheckinTime();
        }
        else if(now.isAfter(LocalDateTime.of(year, monthValue, dayOfMonth, 17, 30, 0, 0))){
            checkinTime = checkinConfigurationProperties.getCheckoutTime();
        }
        
        return checkinTime;
    }
    
    /**
     * @author George-Chou
     * @return 是否為「國定」假日
     * 
     * 預設是讀 application.yml 裡設定的假日清單
     */
    private boolean isNationalHoliday() {
        LocalDate now = LocalDate.now();
        String holidays = checkinConfigurationProperties.getHolidays();
        
        // 只比對「年月日」
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
        String[] holidayUnparsed = holidays.split(",", -1);
        for(String holiday : holidayUnparsed) {
            // 連續假日
            if(holiday.contains("~")) {
                String[] range = holiday.split("~");
                String start = range[0];
                String end = range[1];
                
                LocalDate startTime = LocalDate.parse(start,formatter);
                LocalDate endTime = LocalDate.parse(end,formatter);
                
                // 1. 剛好是起或訖那一天
                // 2. 在起與訖之間
                if(now.isEqual(startTime) || now.isEqual(endTime) ||   
                   (now.isAfter(startTime) && now.isBefore(endTime))) {
                    return true;
                }
            }
            else {
                // 只放一天
                LocalDate currentHoliday = LocalDate.parse(holiday,formatter);
                if(now.isEqual(currentHoliday)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @author George-Chou
     * @return 是否為週末
     * 
     */
    private boolean isWeekend() {
        LocalDate now = LocalDate.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        if(Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(dayOfWeek)) {
            return true;
        }
        return false;
    }
    
    
}
