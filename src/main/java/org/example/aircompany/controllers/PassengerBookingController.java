//package org.example.aircompany.controllers;
//
//import org.example.aircompany.dto.PassengerBookingForm;
//import org.example.aircompany.model.Flight;
//import org.example.aircompany.model.Passenger;
//import org.example.aircompany.model.User;
//import org.example.aircompany.services.BookingService;
//import org.example.aircompany.services.FlightService;
//import org.example.aircompany.services.PassengerService;
//import org.example.aircompany.services.UserService;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//
//@Controller
//@RequestMapping("/passenger/bookings")
//public class PassengerBookingController {
//
//    private final BookingService bookingService;
//    private final FlightService flightService;
//    private final UserService userService;
//    private final PassengerService passengerService;
//
//    public PassengerBookingController(
//            BookingService bookingService,
//            FlightService flightService,
//            UserService userService,
//            PassengerService passengerService
//    ) {
//        this.bookingService = bookingService;
//        this.flightService = flightService;
//        this.userService = userService;
//        this.passengerService = passengerService;
//    }
//
//    // 🟢 Форма бронирования
//    @GetMapping("/form/{flightId}")
//    public String bookingForm(@PathVariable Long flightId, Model model) {
//
//        PassengerBookingForm form = new PassengerBookingForm();
//        form.setFlightId(flightId);
//
//        model.addAttribute("form", form);
//        return "passenger/booking_form";
//    }
//
//    // 🟢 Сохранение бронирования
//    @PostMapping("/create")
//    public String createBooking(
//            @ModelAttribute("form") PassengerBookingForm form,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        User user = userService.findByUsername(userDetails.getUsername());
//        Flight flight = flightService.findFlightById(form.getFlightId())
//                .orElseThrow(() -> new RuntimeException("Рейс не найден"));
//
//        // создаём или обновляем Passenger
//        Passenger passenger = passengerService.createOrUpdatePassenger(user, form);
//
//        // создаём бронирование
//        bookingService.createBookingForPassenger(user, flight);
//
//        return "redirect:/passenger/bookings";
//    }
//}

package org.example.aircompany.controllers;

import org.example.aircompany.dto.PassengerBookingForm;
import org.example.aircompany.model.Flight;
import org.example.aircompany.model.User;
import org.example.aircompany.services.BookingService;
import org.example.aircompany.services.FlightService;
import org.example.aircompany.services.PassengerService;
import org.example.aircompany.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/passenger/bookings")
public class PassengerBookingController {

    private final BookingService bookingService;
    private final FlightService flightService;
    private final UserService userService;
    private final PassengerService passengerService;

    public PassengerBookingController(BookingService bookingService,
                                      FlightService flightService,
                                      UserService userService,
                                      PassengerService passengerService) {
        this.bookingService = bookingService;
        this.flightService = flightService;
        this.userService = userService;
        this.passengerService = passengerService;
    }

    // ✅ Список МОИХ бронирований
    @GetMapping
    public String myBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User passenger = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("bookings", bookingService.findBookingsByUser(passenger));
        return "passenger/bookings";
    }

    // ✅ Открыть форму ввода данных пассажира
    @GetMapping("/form/{flightId}")
    public String bookingForm(@PathVariable Long flightId, Model model) {
        Flight flight = flightService.findFlightById(flightId)
                .orElseThrow(() -> new RuntimeException("Рейс не найден"));
        
        PassengerBookingForm form = new PassengerBookingForm();
        form.setFlightId(flightId);
        model.addAttribute("form", form);
        
        // Получаем занятые места
        List<String> occupiedSeats = bookingService.getOccupiedSeats(flight);
        model.addAttribute("occupiedSeats", occupiedSeats);
        
        // Получаем количество мест в самолете
        Integer seatCapacity = flight.getAircraft().getSeatCapacity() != null 
                ? flight.getAircraft().getSeatCapacity() 
                : 30; // По умолчанию 30 мест
        
        // Генерируем список всех доступных мест
        List<String> availableSeats = new java.util.ArrayList<>();
        String[] letters = {"A", "B", "C", "D", "E", "F"};
        for (int row = 1; row <= seatCapacity; row++) {
            for (String letter : letters) {
                availableSeats.add(row + letter);
            }
        }
        model.addAttribute("availableSeats", availableSeats);
        
        return "passenger/booking_form";
    }

    // ✅ Подтвердить бронирование (POST!)
    @PostMapping("/create")
    public String createBooking(@ModelAttribute("form") PassengerBookingForm form,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {

        User user = userService.findByUsername(userDetails.getUsername());
        Flight flight = flightService.findFlightById(form.getFlightId())
                .orElseThrow(() -> new RuntimeException("Рейс не найден"));

        passengerService.createOrUpdatePassenger(user, form);

        try {
            bookingService.createBookingForPassenger(user, flight, form.getSeatNumber());
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            // Восстанавливаем данные для формы
            Flight flightForForm = flightService.findFlightById(form.getFlightId())
                    .orElseThrow(() -> new RuntimeException("Рейс не найден"));
            List<String> occupiedSeats = bookingService.getOccupiedSeats(flightForForm);
            model.addAttribute("occupiedSeats", occupiedSeats);
            Integer seatCapacity = flightForForm.getAircraft().getSeatCapacity() != null 
                    ? flightForForm.getAircraft().getSeatCapacity() 
                    : 30;
            // Генерируем список всех доступных мест
            List<String> availableSeats = new java.util.ArrayList<>();
            String[] letters = {"A", "B", "C", "D", "E", "F"};
            for (int row = 1; row <= seatCapacity; row++) {
                for (String letter : letters) {
                    availableSeats.add(row + letter);
                }
            }
            model.addAttribute("availableSeats", availableSeats);
            return "passenger/booking_form";
        }

        return "redirect:/passenger/bookings";
    }

}

