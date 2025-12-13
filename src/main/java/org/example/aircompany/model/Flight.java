package org.example.aircompany.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flights")
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightId;

    @Column(name = "flight_number", nullable = false, length = 10, unique = true)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pilot_id")
    private User pilot;


    @Column(name = "departure_city", nullable = false, length = 50)
    private String departureCity; // IATA-код

    @Column(name = "arrival_city", nullable = false, length = 50)
    private String arrivalCity; // IATA-код

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlightStatus status;

    // Связь Many-to-One: много рейсов (Flight) на один самолет (Aircraft)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    // Ленивая коллекция бронирований
    @OneToMany(mappedBy = "flight")
    @ToString.Exclude // <-- ЭТО НОВОЕ ОБЯЗАТЕЛЬНОЕ ИСПРАВЛЕНИЕ
    private List<Booking> bookings;

    // Enum для статусов рейса
    public enum FlightStatus {
        scheduled, delayed, in_flight, completed, cancelled
    }
}