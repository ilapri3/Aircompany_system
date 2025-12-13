package org.example.aircompany.services;

import org.example.aircompany.model.Booking;
import org.example.aircompany.model.RolesHistory;
import org.example.aircompany.model.User;
import org.example.aircompany.model.UserRole;
import org.example.aircompany.repositories.RolesHistoryRepository;
import org.example.aircompany.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolesHistoryRepository rolesHistoryRepository;
    private final BookingService bookingService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RolesHistoryRepository rolesHistoryRepository,
                       BookingService bookingService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolesHistoryRepository = rolesHistoryRepository;
        this.bookingService = bookingService;
    }

    // Метод для регистрации нового пользователя с ролью "passenger"
    public boolean registerNewPassenger(User user) {
        // Проверяем уникальность имени
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false; // Пользователь уже существует
        }

        // Устанавливаем роль и хешируем пароль
        user.setRole(UserRole.passenger);
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash())); // Принимаем пароль из формы

        userRepository.save(user);
        return true;
    }

    public boolean updateUserRole(Long targetUserId, UserRole newRole, User adminUser) {
        // Находим пользователя, которому меняем роль
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + targetUserId + " не найден"));

        UserRole oldRole = targetUser.getRole();

        // Если роль не меняется — нечего обновлять
        if (oldRole == newRole) {
            return false;
        }

        // Меняем роль
        targetUser.setRole(newRole);
        userRepository.save(targetUser);

        // Создаем запись в истории
        RolesHistory history = new RolesHistory();
        history.setTargetUser(targetUser);
        history.setAdminUser(adminUser);
        history.setOldRole(oldRole);
        history.setNewRole(newRole);

        rolesHistoryRepository.save(history);

        return true;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));
    }
    public long countByRole(UserRole role) {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == role)
                .count();
    }

    public List<User> findPilots() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == UserRole.pilot)
                .toList();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Transactional
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + userId + " не найден"));

        // Удаляем все бронирования пользователя
        List<Booking> bookings = bookingService.findBookingsByUser(user);
        for (Booking booking : bookings) {
            bookingService.deleteBooking(booking.getBookingId());
        }

        // Удаляем все записи из истории ролей, где пользователь является target_user или admin_user
        // Это необходимо, так как в базе данных есть ограничение внешнего ключа
        rolesHistoryRepository.deleteByTargetUser(user);
        rolesHistoryRepository.deleteByAdminUser(user);

        // Удаляем пользователя
        // Passenger и FlightLog удалятся автоматически благодаря cascade = CascadeType.ALL
        userRepository.delete(user);
    }




}