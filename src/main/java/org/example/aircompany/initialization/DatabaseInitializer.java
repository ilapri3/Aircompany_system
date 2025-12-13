package org.example.aircompany.initialization;

import org.example.aircompany.model.User;
import org.example.aircompany.model.UserRole;
import org.example.aircompany.repositories.UserRepository;
import org.example.aircompany.services.FlightService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final FlightService flightService;

    public DatabaseInitializer(UserRepository userRepository, 
                               PasswordEncoder passwordEncoder,
                               JdbcTemplate jdbcTemplate,
                               FlightService flightService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.flightService = flightService;
    }

    // Метод, который выполняется сразу после запуска Spring Boot
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        // Сначала обновляем структуру таблиц (ENUM значения)
        updateTableStructures();
        
        // Затем исправляем устаревшие статусы рейсов
        fixFlightStatuses();
        
        // Исправляем устаревшие статусы самолетов
        fixAircraftStatuses();
        
        // Исправляем устаревшие статусы бронирований
        fixBookingStatuses();
        
        // Финально обновляем структуру таблиц (убираем старые значения из ENUM)
        finalizeTableStructures();
        
        // Исправляем статусы самолетов для завершенных рейсов
        fixAircraftStatusesForCompletedFlights();

        // Проверяем, существует ли уже пользователь 'admin'
        if (userRepository.findByUsername("admin").isEmpty()) {

            User admin = new User();
            admin.setUsername("admin");
            // Хешируем пароль "admin123" перед сохранением
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.admin);

            userRepository.save(admin);

            System.out.println("Стартовый пользователь 'admin' создан с паролем 'admin123'.");
        }

        // // Создадим стартового пассажира
        // if (userRepository.findByUsername("passenger1").isEmpty()) {
        //     User passenger = new User();
        //     passenger.setUsername("passenger1");
        //     passenger.setPasswordHash(passwordEncoder.encode("pass123"));
        //     passenger.setRole(UserRole.passenger);
        //     userRepository.save(passenger);
        //     System.out.println("Стартовый пользователь 'passenger1' создан с паролем 'pass123'.");
        // }
    }
    
    @Transactional
    private void updateTableStructures() {
        try {
            // Сначала временно расширяем ENUM, включая старые значения
            jdbcTemplate.execute(
                "ALTER TABLE flights MODIFY COLUMN status ENUM('scheduled', 'delayed', 'in_flight', 'completed', 'cancelled', 'arrived') NOT NULL"
            );
            System.out.println("Временная структура таблицы flights обновлена (включая старые значения)");
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении структуры flights: " + e.getMessage());
        }
        
        try {
            // Временно расширяем ENUM для aircrafts
            jdbcTemplate.execute(
                "ALTER TABLE aircrafts MODIFY COLUMN status ENUM('active', 'in_flight', 'in_maintenance', 'available') NOT NULL"
            );
            System.out.println("Временная структура таблицы aircrafts обновлена (включая старые значения)");
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении структуры aircrafts: " + e.getMessage());
        }
        
        try {
            // Временно расширяем ENUM для bookings
            jdbcTemplate.execute(
                "ALTER TABLE bookings MODIFY COLUMN status ENUM('confirmed', 'pending', 'paid', 'cancelled') NOT NULL"
            );
            System.out.println("Временная структура таблицы bookings обновлена (включая старые значения)");
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении структуры bookings: " + e.getMessage());
        }
    }
    
    @Transactional
    private void finalizeTableStructures() {
        try {
            // Финальная структура для flights (без старых значений)
            jdbcTemplate.execute(
                "ALTER TABLE flights MODIFY COLUMN status ENUM('scheduled', 'delayed', 'in_flight', 'completed', 'cancelled') NOT NULL"
            );
            System.out.println("Финальная структура таблицы flights установлена");
        } catch (Exception e) {
            System.err.println("Ошибка при финализации структуры flights: " + e.getMessage());
        }
        
        try {
            // Финальная структура для aircrafts
            jdbcTemplate.execute(
                "ALTER TABLE aircrafts MODIFY COLUMN status ENUM('active', 'in_flight', 'in_maintenance') NOT NULL"
            );
            System.out.println("Финальная структура таблицы aircrafts установлена");
        } catch (Exception e) {
            System.err.println("Ошибка при финализации структуры aircrafts: " + e.getMessage());
        }
        
        try {
            // Финальная структура для bookings
            jdbcTemplate.execute(
                "ALTER TABLE bookings MODIFY COLUMN status ENUM('confirmed') NOT NULL"
            );
            System.out.println("Финальная структура таблицы bookings установлена");
        } catch (Exception e) {
            System.err.println("Ошибка при финализации структуры bookings: " + e.getMessage());
        }
    }
    
    @Transactional
    private void fixFlightStatuses() {
        try {
            // Заменяем устаревшие статусы рейсов на актуальные
            int updated = jdbcTemplate.update(
                "UPDATE flights SET status = ? WHERE status = ?",
                "completed", "arrived"
            );
            if (updated > 0) {
                System.out.println("Обновлено " + updated + " рейсов: статус 'arrived' заменен на 'completed'");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении статусов рейсов: " + e.getMessage());
            // Продолжаем выполнение, даже если есть ошибка
        }
    }
    
    @Transactional
    private void fixAircraftStatuses() {
        try {
            // Заменяем устаревший статус 'available' на 'active'
            int updated = jdbcTemplate.update(
                "UPDATE aircrafts SET status = ? WHERE status = ?",
                "active", "available"
            );
            if (updated > 0) {
                System.out.println("Обновлено " + updated + " самолетов: статус 'available' заменен на 'active'");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении статусов самолетов: " + e.getMessage());
            // Продолжаем выполнение, даже если есть ошибка
        }
    }
    
    @Transactional
    private void fixBookingStatuses() {
        try {
            // Заменяем все устаревшие статусы бронирований на 'confirmed'
            int updated = jdbcTemplate.update(
                "UPDATE bookings SET status = ? WHERE status IN (?, ?, ?)",
                "confirmed", "pending", "paid", "cancelled"
            );
            if (updated > 0) {
                System.out.println("Обновлено " + updated + " бронирований: статусы заменены на 'confirmed'");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении статусов бронирований: " + e.getMessage());
            // Продолжаем выполнение, даже если есть ошибка
        }
    }
    
    @Transactional
    private void fixAircraftStatusesForCompletedFlights() {
        try {
            flightService.fixAircraftStatusesForCompletedFlights();
            System.out.println("Статусы самолетов для завершенных рейсов исправлены");
        } catch (Exception e) {
            System.err.println("Ошибка при исправлении статусов самолетов: " + e.getMessage());
        }
    }
}