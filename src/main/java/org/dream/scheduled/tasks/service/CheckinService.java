package org.dream.scheduled.tasks.service;

import java.io.IOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.dream.scheduled.tasks.configuration.properties.CheckinConfigurationProperties;
import org.dream.scheduled.tasks.dto.CheckinParamsDto;
import org.dream.scheduled.tasks.dto.ResultDto;
import org.dream.scheduled.tasks.dto.UrlEncodedPostParam;
import org.dream.scheduled.tasks.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CheckinService {
    
    private CheckinConfigurationProperties checkinConfigurationProperties;
    private HttpService httpService;
    private Clock clock = Clock.systemDefaultZone();
    @Getter private ThreadLocal<ResultDto> validateCheckinTimeResultThreadLocal = ThreadLocal.withInitial(ResultDto::new);
    
    @Autowired
    public void setCheckinConfigurationProperties(CheckinConfigurationProperties checkinConfigurationProperties) {
        this.checkinConfigurationProperties = checkinConfigurationProperties;
    }

    @Autowired
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }
    
    public void setClock(Clock clock) {
        this.clock = clock;
    }
    
    public String getBearerToken(String username, String secret) throws IOException {
        String url = checkinConfigurationProperties.getTokenUrl();
        String format = "grant_type=password&username=%s&password=%s";
        String params = String.format(format, username, secret); 
        
        return httpService.makeUrlEncodedPost(new UrlEncodedPostParam(url, params, false, null));
    }
    
    public void checkin(CheckinParamsDto checkinParams) throws IOException {
        log.info("打卡時收到的參數:" + checkinParams);
        
        ResultDto validateCheckinTimeResult = validateCheckinTimeResultThreadLocal.get();
        String checkinUrl = checkinConfigurationProperties.getCheckinUrl();
        String checkinTime = this.getCheckinTime(checkinParams);
        this.validateCheckinTime(checkinTime);

        if(validateCheckinTimeResult.isPass()) {
            String token = this.getBearerToken(checkinParams.getUsername(), AESUtil.decrypt(checkinParams.getSecret()));
            String checkinResult = httpService.makeUrlEncodedPost(new UrlEncodedPostParam(checkinUrl, checkinParams.getQueryString(), true, token));
            if(!Objects.isNull(checkinResult))
                validateCheckinTimeResult.setMessage(checkinResult);
        }
    }

    private String getCheckinTime(CheckinParamsDto checkinParams) {
        String checkinTime = checkinParams.getCheckinTime();
        if(checkinTime == null) {
            checkinTime = this.getDefaultCheckinTime();
            checkinParams.setCheckinTime(checkinTime);
        }
        return checkinTime;
    }
    
    /**
     * 預設是「上」班打卡限定在 07:59:999 與 17:30:00:000 之間
     * 預設是「下」班打卡限定在 17:30:00:000 之後
     */
    private String getDefaultCheckinTime() {
        String checkinTime = null;
        
        LocalDateTime now = LocalDateTime.now(clock);
        int year = now.getYear();
        int monthValue = now.getMonthValue();
        int dayOfMonth = now.getDayOfMonth();
        
        if(now.isAfter(LocalDateTime.of(year, monthValue, dayOfMonth, 7, 59, 59, 999)) && 
           now.isBefore(LocalDateTime.of(year, monthValue, dayOfMonth, 17, 30, 0, 0))) {
            checkinTime = checkinConfigurationProperties.getCheckinTime();
        }
        else if(now.isAfter(LocalDateTime.of(year, monthValue, dayOfMonth, 17, 30, 0, 0))){
            checkinTime = checkinConfigurationProperties.getCheckoutTime();
        }
        
        return checkinTime;
    }
    
    private boolean isNationalHoliday() {
        LocalDate now = LocalDate.now(clock);
        List<String> holidayUnparsed = checkinConfigurationProperties.getHolidays();
        
        // 只比對「年月日」
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
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
                try {
                    LocalDate currentHoliday = LocalDate.parse(holiday,formatter);
                    if(now.isEqual(currentHoliday)) {
                        return true;
                    }
                }
                catch(Exception ex) {
                    return false;
                }
            }
        }
        return false;
    }
    
    private boolean isWeekend() {
        LocalDate now = LocalDate.now(clock);
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        if(Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(dayOfWeek)) {
            return true;
        }
        return false;
    }

    private boolean isMakeUpWorkDay() {
        LocalDate now = LocalDate.now(clock);
        List<String> makeUpWorkDaysUnparsed = checkinConfigurationProperties.getMakeUpWorkDays();
        
        // 只比對「年月日」
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");

        for(String makeUpWorkDayUnParsed : makeUpWorkDaysUnparsed) {
            // 通常只補一天
            try {
                LocalDate currentMakeUpWorkDay = LocalDate.parse(makeUpWorkDayUnParsed,formatter);
                if(now.isEqual(currentMakeUpWorkDay)) {
                    return true;
                }
            }
            catch(Exception ex) {
                return false;
            }
        }
        return false;
    }

    private boolean canCheckinNow(String checkinTime) {
        // 不在可打卡時間內無法打卡
        return checkinTime == null || checkinTime == "";
    }

    private void validateCheckinTime(String checkinTime) {
        ResultDto validateCheckinTimeResult = validateCheckinTimeResultThreadLocal.get();
        
        if(!checkinConfigurationProperties.isEnabled()) {
            validateCheckinTimeResult.setPass(false);
            validateCheckinTimeResult.setMessage("自動打卡功能未開啟");
            return;
        }
        if(this.isMakeUpWorkDay()) {
            validateCheckinTimeResult.setPass(true);
            validateCheckinTimeResult.setMessage("地獄補班日");;
            return;
        }
        if(this.isWeekend()) { 
            validateCheckinTimeResult.setPass(false);
            validateCheckinTimeResult.setMessage("週末打什麼卡");
            return;
        }
        if(this.isNationalHoliday()) { 
            validateCheckinTimeResult.setPass(false);
            validateCheckinTimeResult.setMessage("國定假日不打卡！");
            return;
        }
        if(this.canCheckinNow(checkinTime)) {
            validateCheckinTimeResult.setPass(false);
            validateCheckinTimeResult.setMessage("還不能打卡喔！(開放時間為0800)");
            return;
        }
    }

}
