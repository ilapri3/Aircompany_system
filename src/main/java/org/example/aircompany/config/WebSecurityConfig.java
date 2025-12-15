package org.example.aircompany.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/",
                                "/css/**",
                                "/js/**",
                                "/about",
                                "/register",
                                "/login").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("admin")
                        .requestMatchers("/booking-staff/**").hasAnyAuthority("admin", "booking_staff")
                        .requestMatchers("/pilot/**").hasAnyAuthority("admin", "pilot")
                        .requestMatchers("/passenger/**").hasAnyAuthority("admin", "passenger")
                        .requestMatchers("/search/flights").hasAnyAuthority("admin", "passenger")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        // Используем .logoutUrl() или, для более явного соответствия старому коду:
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable()); // Временно отключаем CSRF для упрощения тестирования API

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}