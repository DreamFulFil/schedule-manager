package org.dream.scheduled.tasks.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
public class SecurityConfigurations extends WebSecurityConfigurerAdapter {

    // 僅開放外部系統呼叫的 REST API Security 設定
    @Configuration
    @Order(1)
    public static class RestApiSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        
        @Bean(name = "restApiPasswordEncoder")
        public PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
        
        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            // REST API用
            auth.inMemoryAuthentication()
                .withUser("remoteUser")
                  .password(passwordEncoder().encode("remoteSecret")).authorities("REST")
                .and()
                .withUser("waker")
                  .password(passwordEncoder().encode("d2FrZXVw")).authorities("HEART");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
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
                  .antMatchers(
                      "/task/**",
                      "/heartbeat/**"
                  )
              .and()
              .authorizeRequests()
                  .antMatchers("/task/**").hasAuthority("REST")
                  .antMatchers("/heartbeat/**").hasAuthority("HEART")
              .and()
              
              .httpBasic()
              .and()
              .csrf()
                  .disable();
        }
    }
    
}