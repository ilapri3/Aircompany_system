package org.example.aircompany.repositories;

import org.example.aircompany.model.Flight;
import org.example.aircompany.model.FlightLog;
import org.example.aircompany.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlightLogRepository extends JpaRepository<FlightLog, Long> {
    List<FlightLog> findByPilotOrderByLogTimeDesc(User pilot);
    
    List<FlightLog> findAllByOrderByLogTimeDesc();
    
    Optional<FlightLog> findByFlight(Flight flight);
    
    boolean existsByFlight(Flight flight);
}
