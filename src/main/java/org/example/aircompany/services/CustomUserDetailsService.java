package org.example.aircompany.services;

import org.example.aircompany.model.User;
import org.example.aircompany.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Этот метод вызывается Spring Security при попытке входа
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Находим пользователя в нашей БД
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        // 2. Преобразуем нашего User в формат UserDetails, понятный Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                // ИСПРАВЛЕННАЯ СТРОКА: обернули роль в SimpleGrantedAuthority
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        ); // Роль передается как GrantedAuthority
    }
}