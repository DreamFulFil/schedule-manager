package org.dream.scheduled.tasks.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    
//    @Configuration
//    @Order(2)
//    public static class WebPageSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
//    
//        @Autowired
//        private UserDetailsService userDetailsService;
//        
//        @Autowired
//        private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
//        
//        @Override
//        public void configure(AuthenticationManagerBuilder auth) throws Exception {
//            auth.authenticationProvider(aclAuthenticationProvider());
//        }
//        
//        @Override
//        protected void configure(HttpSecurity http) throws Exception {
//            
//            // 設定頁面需要驗證的頁面、規則、登入登出...等
//            http
//              .authorizeRequests()
//              .antMatchers("/login*").permitAll()
//              .antMatchers("/**").access("authenticated")
//              
//            .and()
//              .formLogin()
//              .loginPage("/login")
//              .loginProcessingUrl("/login_check")
//              .successHandler(customAuthenticationSuccessHandler)
//            
//            .and()
//              .logout()
//              .logoutUrl("/logout")
//              .logoutSuccessUrl("/login")
//              .invalidateHttpSession(true)
//              .deleteCookies("JSESSIONID")
//              
//            .and()
//              .exceptionHandling().authenticationEntryPoint(new AjaxAwareAuthenticationEntryPoint("/login"))
//              
//            .and()
//              .csrf()
//              .disable();
//            
//            // 設定 Session 的建立機制
//            http.sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                .maximumSessions(1)
//                .expiredUrl("/login");
//            
//        }
//        
//        @Override
//        public void configure(WebSecurity web) throws Exception {
//            // 不要驗證的url pattern
//            web.ignoring()
//               .antMatchers(
//                       "/resources/**", 
//                       "/error*", 
//                       "/global/**", 
//                       "/webjars/**"
//            );
//        }
//        
//        @Bean(name = "webPasswordEncoder")
//        public PasswordEncoder passwordEncoder() {
//            return new BCryptPasswordEncoder();
//        }
//        
//        @Bean(name = "aclAuthenticationProvider")
//        public DaoAuthenticationProvider aclAuthenticationProvider(){
//            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//            provider.setPasswordEncoder(passwordEncoder());
//            provider.setUserDetailsService(userDetailsService);
//            return provider;
//        }
//        
//        // 主要控制 session 被摧毀的事件通知給 session registry
//        // 情境是同一個 User 登入兩次的情況下(可能不同電腦)，是否要能讓 session 共存
//        @Bean
//        public HttpSessionEventPublisher httpSessionEventPublisher() {
//            return new HttpSessionEventPublisher();
//        }
//    }
    
}