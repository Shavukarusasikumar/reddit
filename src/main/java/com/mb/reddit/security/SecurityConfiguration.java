package com.mb.reddit.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> {})
                //                .formLogin(form -> form
                //                        .loginPage("/user/login")
                //                        .loginProcessingUrl("/user/authenticate")
                //                        .permitAll()
                //                        .successHandler((request, response, authentication) -> {
                //                            response.sendRedirect("/");
                //                        })
                ////                )
                //                .logout(logout -> logout
                //                        .logoutUrl("/logout")
                //                        .logoutSuccessUrl("/user/login")
                //                        .permitAll()
                //                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}