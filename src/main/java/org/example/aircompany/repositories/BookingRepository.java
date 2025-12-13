package org.example.aircompany.repositories;

import org.example.aircompany.model.Booking;
import org.example.aircompany.model.Flight;
import org.example.aircompany.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    boolean existsByUserAndFlight(User user, Flight flight);

    List<Booking> findByFlight(Flight flight);

    boolean existsByFlightAndSeatNumber(Flight flight, String seatNumber);
}

