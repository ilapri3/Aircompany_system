package org.example.aircompany.repositories;

import org.example.aircompany.model.Aircraft;
import org.example.aircompany.model.Flight;
import org.example.aircompany.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    // 🔍 Поиск рейсов для пассажиров
    @Query("SELECT f FROM Flight f " +
            "WHERE (:departureCity IS NULL OR f.departureCity LIKE %:departureCity%) " +
            "AND (:arrivalCity IS NULL OR f.arrivalCity LIKE %:arrivalCity%) " +
            "AND (f.departureTime >= :dateStart) " +
            "AND f.status = 'scheduled' " +
            "ORDER BY f.departureTime ASC")
    List<Flight> searchFlights(
            @Param("departureCity") String departureCity,
            @Param("arrivalCity") String arrivalCity,
            @Param("dateStart") LocalDateTime dateStart
    );

    // 🟩 НОВОЕ: рейсы, назначенные конкретному пилоту
    List<Flight> findByPilot(User pilot);
    
    // Рейсы пилота, которые еще не завершены (не completed и не cancelled)
    @Query("SELECT f FROM Flight f WHERE f.pilot = :pilot " +
           "AND f.status != 'completed' AND f.status != 'cancelled'")
    List<Flight> findActiveFlightsByPilot(@Param("pilot") User pilot);
    
    // Рейсы, назначенные конкретному самолету
    List<Flight> findByAircraft(Aircraft aircraft);
}
