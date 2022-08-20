package org.dream.scheduled.tasks.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * 換成 Spring Security 5.7.0 的設定方式
 * 詳細請參考官方說明文件: 
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 */
@Configuration
public class SecurityConfigurations {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(c -> {
            CorsConfigurationSource source = request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
                config.setAllowedOrigins(List.of("http://localhost:4200"));
                config.setAllowedMethods(List.of("GET", "POST"));
                return config;
            };
            c.configurationSource(source);
        });

        http
          .requestMatchers()
              .antMatchers("/task/**", "/heartbeat/**")
          .and()
          .authorizeRequests()
              .antMatchers("/task/**").hasAuthority("REST")
              .antMatchers("/heartbeat/**").hasAuthority("HEART")
          .and()
          
          .httpBasic()
          .and()
          .csrf()
              .disable();
        
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        List<UserDetails> userDetails = 
            Arrays.asList(
                User.builder()
                    .username("remoteUser")
                    .password("{noop}remoteSecret")
                    .authorities("REST")
                    .build(),

                User.builder()
                    .username("waker")
                    .password("{noop}d2FrZXVw")
                    .authorities("HEART")
                    .build()
            );
        return new InMemoryUserDetailsManager(userDetails);
    }
}