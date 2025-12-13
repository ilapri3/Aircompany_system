package org.example.aircompany.repositories;

import org.example.aircompany.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Метод для поиска пользователя по логину (username).
    // Нужен для Spring Security.
    Optional<User> findByUsername(String username);
}
