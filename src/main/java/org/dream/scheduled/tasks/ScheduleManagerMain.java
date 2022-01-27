package org.dream.scheduled.tasks;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan(
    basePackages = { "org.dream.scheduled.tasks.configuration" }
)
public class ScheduleManagerMain {

	public static void main(String[] args) {
		SpringApplication.run(ScheduleManagerMain.class, args);
	}

	@PostConstruct
    public void initTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
    }
	
}
