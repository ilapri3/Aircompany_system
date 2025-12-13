package org.example.aircompany.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Обратные связи (детали пассажира, бронирования, логи)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Passenger passengerDetails;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<Booking> bookings;

    @OneToMany(mappedBy = "pilot", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<FlightLog> flightLogs = new ArrayList<>();
}