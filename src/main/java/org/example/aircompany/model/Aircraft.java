package org.example.aircompany.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString; // <-- ДОБАВИТЬ ЭТОТ ИМПОРТ
import java.util.List;

@Entity
@Table(name = "aircrafts")
@Data
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aircraftId;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "registration", length = 20)
    private String registration;

    @Column(name = "seat_capacity")
    private Integer seatCapacity = 30; // По умолчанию 30 мест

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AircraftStatus status;

    public enum AircraftStatus {
        active, in_flight, in_maintenance
    }

    // Связь OneToMany с Flight
    @OneToMany(mappedBy = "aircraft")
    @ToString.Exclude // <-- ДОБАВИТЬ ЭТУ АННОТАЦИЮ
    private List<Flight> flights;
}