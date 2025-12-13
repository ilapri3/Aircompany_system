package org.example.aircompany.controllers;

import org.example.aircompany.model.Booking;
import org.example.aircompany.services.BookingService;
import org.example.aircompany.services.FlightService;
import org.example.aircompany.services.UserService; // Нужен для выпадающего списка пользователей
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/booking-staff/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final FlightService flightService;
    private final UserService userService; // Инжектируем UserService для получения списка пользователей

    public BookingController(BookingService bookingService, FlightService flightService, UserService userService) {
        this.bookingService = bookingService;
        this.flightService = flightService;
        this.userService = userService;
    }

    /** 1. Список всех бронирований (Read All) */
    @GetMapping
    public String listBookings(Model model) {
        // Убедитесь, что ваш сервис/репозиторий использует JOIN FETCH или EAGER для Flight и User, чтобы избежать LazyInitializationException
        List<Booking> bookings = bookingService.findAllBookings();
        model.addAttribute("bookings", bookings);
        return "bookings/list";
    }


    /** 2.1. Отображение формы СОЗДАНИЯ нового бронирования (Create Form) */
    @GetMapping("/new")
    public String showNewBookingForm(Model model) {
        // Создаем новый пустой объект Booking для привязки формы
        Booking booking = new Booking();
        booking.setStatus(Booking.BookingStatus.confirmed);

        // Передаем в модель списки для заполнения <select> элементов в bookings/form.html
        model.addAttribute("flights", flightService.findAllFlights());
        model.addAttribute("users", userService.findAllUsers()); // Предполагая, что у вас есть такой метод в UserService

        model.addAttribute("booking", booking);
        model.addAttribute("pageTitle", "Добавить новое бронирование");

        return "bookings/form";
    }

    /** 2.2. Отображение формы РЕДАКТИРОВАНИЯ (Update Form) */
    @GetMapping("/edit/{id}")
    public String showEditBookingForm(@PathVariable("id") Long id, Model model) {
        Booking booking = bookingService.findBookingById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID бронирования:" + id));

        model.addAttribute("booking", booking);

        // Передаем списки для потенциального редактирования FK (обязательно!)
        model.addAttribute("flights", flightService.findAllFlights());
        model.addAttribute("users", userService.findAllUsers());
        
        // Получаем занятые места для выбранного рейса
        if (booking.getFlight() != null) {
            List<String> occupiedSeats = bookingService.getOccupiedSeats(booking.getFlight());
            model.addAttribute("occupiedSeats", occupiedSeats);
            Integer seatCapacity = booking.getFlight().getAircraft().getSeatCapacity() != null 
                    ? booking.getFlight().getAircraft().getSeatCapacity() 
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
        }

        model.addAttribute("pageTitle", "Редактировать бронирование ID: " + id);
        return "bookings/form";
    }

    /** 3. Обработка сохранения бронирования (Create/Update Submit) */
    @PostMapping("/save")
    public String saveBooking(@ModelAttribute("booking") Booking booking, Model model) {
        try {
            // Устанавливаем статус confirmed для всех бронирований
            booking.setStatus(Booking.BookingStatus.confirmed);
            // Здесь Spring MVC автоматически привяжет выбранные flight и user по ID
            // Убедитесь, что метод saveBooking также обрабатывает установку bookingDate, если это нужно
            bookingService.saveBooking(booking);
            return "redirect:/booking-staff/bookings";
        } catch (IllegalStateException e) {
            // В случае ошибки возвращаемся к форме редактирования
            model.addAttribute("error", e.getMessage());
            model.addAttribute("booking", booking);
            model.addAttribute("flights", flightService.findAllFlights());
            model.addAttribute("users", userService.findAllUsers());
            if (booking.getFlight() != null) {
                List<String> occupiedSeats = bookingService.getOccupiedSeats(booking.getFlight());
                model.addAttribute("occupiedSeats", occupiedSeats);
                Integer seatCapacity = booking.getFlight().getAircraft().getSeatCapacity() != null 
                        ? booking.getFlight().getAircraft().getSeatCapacity() 
                        : 30;
                List<String> availableSeats = new java.util.ArrayList<>();
                String[] letters = {"A", "B", "C", "D", "E", "F"};
                for (int row = 1; row <= seatCapacity; row++) {
                    for (String letter : letters) {
                        availableSeats.add(row + letter);
                    }
                }
                model.addAttribute("availableSeats", availableSeats);
            }
            model.addAttribute("pageTitle", booking.getBookingId() != null 
                    ? "Редактировать бронирование ID: " + booking.getBookingId() 
                    : "Добавить новое бронирование");
            return "bookings/form";
        }
    }

    /** 4. Удаление бронирования (Delete) */
    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable("id") Long id) {
        bookingService.deleteBooking(id);
        return "redirect:/booking-staff/bookings";
    }

    /** 5. Получение списка мест для рейса (AJAX) */
    @GetMapping("/seats/{flightId}")
    @ResponseBody
    public java.util.Map<String, Object> getSeatsForFlight(@PathVariable("flightId") Long flightId) {
        var flight = flightService.findFlightById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Рейс не найден"));
        
        List<String> occupiedSeats = bookingService.getOccupiedSeats(flight);
        Integer seatCapacity = flight.getAircraft().getSeatCapacity() != null 
                ? flight.getAircraft().getSeatCapacity() 
                : 30;
        
        // Генерируем список всех доступных мест
        List<String> availableSeats = new java.util.ArrayList<>();
        String[] letters = {"A", "B", "C", "D", "E", "F"};
        for (int row = 1; row <= seatCapacity; row++) {
            for (String letter : letters) {
                availableSeats.add(row + letter);
            }
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("availableSeats", availableSeats);
        result.put("occupiedSeats", occupiedSeats);
        return result;
    }
}