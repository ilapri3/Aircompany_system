package org.example.aircompany.controllers;

import org.example.aircompany.model.Aircraft;
import org.example.aircompany.model.Flight;
import org.example.aircompany.model.User;
import org.example.aircompany.services.AircraftService; // Убедитесь, что этот сервис существует
import org.example.aircompany.services.FlightService;
import org.example.aircompany.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Controller
@RequestMapping("/booking-staff/flights") // Все методы доступны по пути /booking-staff/flights
public class FlightController {

    private final FlightService flightService;
    private final AircraftService aircraftService;
    private final UserService userService;

    // Инжекция зависимостей
    public FlightController(FlightService flightService, AircraftService aircraftService, UserService userService) {
        this.flightService = flightService;
        this.aircraftService = aircraftService;
        this.userService = userService;
    }

    /** 1. Список всех рейсов (Read All) */
    @GetMapping
    public String listFlights(Model model) {
        List<Flight> flights = flightService.findAllFlights();
        model.addAttribute("flights", flights);
        // Отображение шаблона: /resources/templates/flights/list.html
        return "flights/list";
    }

    /** 2. Отображение формы создания/редактирования (Create/Update Form) */
    @GetMapping("/new")
    public String showFlightForm(Model model) {
        // Для создания нового рейса
        model.addAttribute("flight", new Flight());
        // Добавляем список доступных самолетов (только active) для выпадающего списка
        List<Aircraft> availableAircrafts = aircraftService.findAllAircrafts().stream()
                .filter(a -> a.getStatus() == Aircraft.AircraftStatus.active)
                .toList();
        model.addAttribute("aircrafts", availableAircrafts);
        // Добавляем список доступных пилотов (только те, кто не назначен на незавершенные рейсы)
        List<User> availablePilots = flightService.getAvailablePilots(userService.findPilots(), null);
        model.addAttribute("pilots", availablePilots);
        model.addAttribute("pageTitle", "Добавить новый рейс");
        return "flights/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditFlightForm(@PathVariable("id") Long id, Model model) {
        Flight flight = flightService.findFlightById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID рейса:" + id));
        model.addAttribute("flight", flight);
        // Показываем все самолеты, но текущий самолет рейса всегда должен быть доступен
        List<Aircraft> availableAircrafts = aircraftService.findAllAircrafts().stream()
                .filter(a -> a.getStatus() == Aircraft.AircraftStatus.active 
                          || a.getAircraftId().equals(flight.getAircraft().getAircraftId()))
                .toList();
        model.addAttribute("aircrafts", availableAircrafts);
        // Показываем доступных пилотов + текущего пилота рейса (если он есть)
        List<User> allPilots = userService.findPilots();
        List<User> availablePilots = flightService.getAvailablePilots(allPilots, id);
        // Если у рейса есть пилот, добавляем его в список, если его там еще нет
        if (flight.getPilot() != null) {
            boolean pilotAlreadyInList = availablePilots.stream()
                    .anyMatch(p -> p.getUserId().equals(flight.getPilot().getUserId()));
            if (!pilotAlreadyInList) {
                availablePilots.add(flight.getPilot());
            }
        }
        model.addAttribute("pilots", availablePilots);
        model.addAttribute("pageTitle", "Редактировать рейс ID: " + id);
        return "flights/form";
    }

    /** 3. Обработка сохранения рейса (Create/Update Submit) */
    @PostMapping("/save")
    public String saveFlight(@ModelAttribute("flight") Flight flight,
                             @RequestParam("aircraftId") Long aircraftId,
                             @RequestParam(value = "pilotId", required = false) Long pilotId,
                             Model model) {

        // Проверяем статус самолета перед назначением
        Aircraft aircraft = aircraftService.findAircraftById(aircraftId)
                .orElseThrow(() -> new IllegalArgumentException("Самолет не найден"));
        
        // Нельзя назначать самолет со статусом in_maintenance
        if (aircraft.getStatus() == Aircraft.AircraftStatus.in_maintenance) {
            model.addAttribute("error", "Нельзя назначить самолет со статусом 'in_maintenance' на рейс");
            model.addAttribute("flight", flight);
            List<Aircraft> availableAircrafts = aircraftService.findAllAircrafts().stream()
                    .filter(a -> a.getStatus() == Aircraft.AircraftStatus.active 
                              || (flight.getAircraft() != null && a.getAircraftId().equals(flight.getAircraft().getAircraftId())))
                    .toList();
            model.addAttribute("aircrafts", availableAircrafts);
            // Добавляем доступных пилотов
            List<User> allPilots = userService.findPilots();
            List<User> availablePilots = flightService.getAvailablePilots(allPilots, flight.getFlightId());
            // Если у рейса есть пилот, добавляем его в список, если его там еще нет
            if (flight.getPilot() != null) {
                boolean pilotAlreadyInList = availablePilots.stream()
                        .anyMatch(p -> p.getUserId().equals(flight.getPilot().getUserId()));
                if (!pilotAlreadyInList) {
                    availablePilots.add(flight.getPilot());
                }
            }
            model.addAttribute("pilots", availablePilots);
            model.addAttribute("pageTitle", flight.getFlightId() == null ? "Добавить новый рейс" : "Редактировать рейс ID: " + flight.getFlightId());
            return "flights/form";
        }

        flight.setAircraft(aircraft);

        // Проверяем, что время отправления раньше времени прибытия
        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            if (!flight.getDepartureTime().isBefore(flight.getArrivalTime())) {
                model.addAttribute("error", "Время отправления должно быть раньше времени прибытия");
                model.addAttribute("flight", flight);
                List<Aircraft> availableAircrafts = aircraftService.findAllAircrafts().stream()
                        .filter(a -> a.getStatus() == Aircraft.AircraftStatus.active 
                                  || (flight.getAircraft() != null && a.getAircraftId().equals(flight.getAircraft().getAircraftId())))
                        .toList();
                model.addAttribute("aircrafts", availableAircrafts);
                // Добавляем доступных пилотов
                List<User> allPilots = userService.findPilots();
                List<User> availablePilots = flightService.getAvailablePilots(allPilots, flight.getFlightId());
                // Если у рейса есть пилот, добавляем его в список, если его там еще нет
                if (flight.getPilot() != null) {
                    boolean pilotAlreadyInList = availablePilots.stream()
                            .anyMatch(p -> p.getUserId().equals(flight.getPilot().getUserId()));
                    if (!pilotAlreadyInList) {
                        availablePilots.add(flight.getPilot());
                    }
                }
                model.addAttribute("pilots", availablePilots);
                model.addAttribute("pageTitle", flight.getFlightId() == null ? "Добавить новый рейс" : "Редактировать рейс ID: " + flight.getFlightId());
                return "flights/form";
            }
        }

        // пилот - проверяем только если пилот выбран
        if (pilotId != null) {
            User pilot = userService.findById(pilotId)
                    .orElseThrow(() -> new IllegalArgumentException("Пилот не найден"));
            
            // Проверяем, может ли пилот быть назначен на этот рейс
            if (!flightService.canAssignPilotToFlight(pilot, flight.getFlightId())) {
                model.addAttribute("error", "Пилот уже назначен на другой незавершенный рейс и не может быть назначен на этот рейс");
                model.addAttribute("flight", flight);
                List<Aircraft> availableAircrafts = aircraftService.findAllAircrafts().stream()
                        .filter(a -> a.getStatus() == Aircraft.AircraftStatus.active 
                                  || (flight.getAircraft() != null && a.getAircraftId().equals(flight.getAircraft().getAircraftId())))
                        .toList();
                model.addAttribute("aircrafts", availableAircrafts);
                // Добавляем доступных пилотов
                List<User> allPilots = userService.findPilots();
                List<User> availablePilots = flightService.getAvailablePilots(allPilots, flight.getFlightId());
                // Если у рейса есть пилот, добавляем его в список, если его там еще нет
                if (flight.getPilot() != null) {
                    boolean pilotAlreadyInList = availablePilots.stream()
                            .anyMatch(p -> p.getUserId().equals(flight.getPilot().getUserId()));
                    if (!pilotAlreadyInList) {
                        availablePilots.add(flight.getPilot());
                    }
                }
                model.addAttribute("pilots", availablePilots);
                model.addAttribute("pageTitle", flight.getFlightId() == null ? "Добавить новый рейс" : "Редактировать рейс ID: " + flight.getFlightId());
                return "flights/form";
            }
            
            flight.setPilot(pilot);
        } else {
            // Если пилот не выбран, сбрасываем его
            flight.setPilot(null);
        }

        // статус по умолчанию
        if (flight.getFlightId() == null && flight.getStatus() == null) {
            flight.setStatus(Flight.FlightStatus.scheduled);
        }

        flightService.saveFlight(flight);

        return "redirect:/booking-staff/flights";
    }

    /** 4. Удаление рейса (Delete) */
    @GetMapping("/delete/{id}")
    public String deleteFlight(@PathVariable("id") Long id) {
        flightService.deleteFlight(id);
        return "redirect:/booking-staff/flights";
    }
}