package org.example.aircompany.controllers;

import org.example.aircompany.model.Booking;
import org.example.aircompany.model.Flight;
import org.example.aircompany.model.User;
import org.example.aircompany.services.BookingService;
import org.example.aircompany.services.FlightService;
import org.example.aircompany.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PassengerFlightController {

    private final FlightService flightService;
    private final UserService userService;
    private final BookingService bookingService;

    public PassengerFlightController(FlightService flightService,
                                     UserService userService,
                                     BookingService bookingService) {
        this.flightService = flightService;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @GetMapping("/search/flights")
    public String searchFlights(
            @RequestParam(required = false) String departureCity,
            @RequestParam(required = false) String arrivalCity,
            @RequestParam(required = false) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        model.addAttribute("departureCity", departureCity);
        model.addAttribute("arrivalCity", arrivalCity);
        model.addAttribute("date", date);

        model.addAttribute("searchPerformed", false);
        model.addAttribute("flights", List.of());

        if (date != null) {
            List<Flight> flights =
                    flightService.searchFlights(departureCity, arrivalCity, date);

            model.addAttribute("flights", flights);
            model.addAttribute("searchPerformed", true);

            // защита от повторного бронирования
            User user = userService.findByUsername(userDetails.getUsername());
            List<Booking> bookings = bookingService.findBookingsByUser(user);

            Set<Long> bookedFlightIds = bookings.stream()
                    .map(b -> b.getFlight().getFlightId())
                    .collect(Collectors.toSet());

            model.addAttribute("bookedFlightIds", bookedFlightIds);
        }

        return "search/flights";
    }
}
