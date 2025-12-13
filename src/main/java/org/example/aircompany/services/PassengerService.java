package org.example.aircompany.services;

import org.example.aircompany.dto.PassengerBookingForm;
import org.example.aircompany.model.Passenger;
import org.example.aircompany.model.User;
import org.example.aircompany.repositories.PassengerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public Passenger createOrUpdatePassenger(User user, PassengerBookingForm form) {

        Optional<Passenger> existing = passengerRepository.findById(user.getUserId());

        Passenger passenger = existing.orElseGet(Passenger::new);

        passenger.setUser(user);
        passenger.setFirstName(form.getFirstName());
        passenger.setLastName(form.getLastName());
        passenger.setPassportNumber(form.getPassportNumber());
        passenger.setContactEmail(form.getContactEmail());
        passenger.setContactPhone(form.getContactPhone());

        if (passenger.getRegistrationDate() == null) {
            passenger.setRegistrationDate(LocalDate.now());
        }

        return passengerRepository.save(passenger);
    }
}
