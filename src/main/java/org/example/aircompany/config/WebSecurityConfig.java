package org.example.aircompany.config;

import org.example.aircompany.services.CustomUserDetailsService;
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

    private final CustomUserDetailsService userDetailsService;

    public WebSecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // 1. Определяем, как будут защищены URL-адреса
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        // Публичный доступ
                        // ИСПРАВЛЕНО: Вместо AntPathRequestMatcher используем прямые строки
                        .requestMatchers("/",
                                "/css/**",
                                "/js/**",
                                "/register",
                                "/login").permitAll()

                        // Доступ Администратора
                        // ИСПРАВЛЕНО: Теперь строка является прямым паттерном
                        .requestMatchers("/admin/**").hasAuthority("admin")

                        // Доступ для Сотрудников бронирования
                        .requestMatchers("/booking-staff/**").hasAnyAuthority("admin", "booking_staff")

                        // Доступ для Пилотов
                        .requestMatchers("/pilot/**").hasAnyAuthority("admin", "pilot")

                        .requestMatchers("/passenger/**").hasAnyAuthority("admin", "passenger")
                        
                        // Доступ к поиску рейсов для пассажиров и администраторов
                        .requestMatchers("/search/flights").hasAnyAuthority("admin", "passenger")





                        // Все остальные запросы требуют аутентификации
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

    // 2. Бин для хеширования паролей (BCrypt — стандарт в Spring)
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 
        return new BCryptPasswordEncoder();
    }
}