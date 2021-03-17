package org.dream.scheduled.tasks.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "checkin-config")
public class CheckinConfigurationProperties {

    /** 拿 OAuth Bearer token 的 url */
    private String tokenUrl;
    
    /** 打卡 url */
    private String checkinUrl;
    
    /** 使用者帳號 */
    private String user;
    
    /** 使用者密碼 */
    private String secret;
    
    /** 上班打卡時間(e.g. 08:30) */
    private String checkinTime;
    
    /** 下班打卡時間(e.g. 17:30) */
    private String checkoutTime;
    
    /** 國定假日 */
    private String holidays;
    
    /** 開關打卡(測試用) */
    private boolean enabled;
    
    /** 收到成功通知信的使用者 */
    private String notificationEmail;
    
}
