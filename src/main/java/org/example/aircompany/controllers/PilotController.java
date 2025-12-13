package org.example.aircompany.controllers;

import org.example.aircompany.model.Flight;
import org.example.aircompany.model.FlightLog;
import org.example.aircompany.model.User;
import org.example.aircompany.model.UserRole;
import org.example.aircompany.services.FlightLogService;
import org.example.aircompany.services.FlightService;
import org.example.aircompany.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pilot")
public class PilotController {

    private final FlightService flightService;
    private final FlightLogService flightLogService;
    private final UserService userService;

    public PilotController(FlightService flightService,
                           FlightLogService flightLogService,
                           UserService userService) {
        this.flightService = flightService;
        this.flightLogService = flightLogService;
        this.userService = userService;
    }

    // Список рейсов пилота
    @GetMapping("/flights")
    public String pilotFlights(@AuthenticationPrincipal UserDetails pilotDetails,
                               Model model) {

        User pilot = userService.findByUsername(pilotDetails.getUsername());

        // Показываем только рейсы, назначенные этому пилоту, для которых еще нет отчетов
        model.addAttribute("flights", flightService.findFlightsByPilotWithoutLogs(pilot));

        return "pilot/flights";
    }


    // Список летных отчетов

    @GetMapping("/logs")
    public String listLogs(@AuthenticationPrincipal UserDetails pilotDetails,
                           Model model) {

        User currentUser = userService.findByUsername(pilotDetails.getUsername());
        
        // Если пользователь админ, то показываем все логи из БД
        // Если пользователь пилот, то показываем только его логи
        if (currentUser.getRole() == UserRole.admin) {
            model.addAttribute("logs", flightLogService.findAll());
            model.addAttribute("isAdmin", true);
        } else {
            model.addAttribute("logs", flightLogService.findLogsByPilot(currentUser));
            model.addAttribute("isAdmin", false);
        }

        return "pilot/logs";
    }


    // Форма нового летного отчета

    @GetMapping("/logs/new")
    public String newLogForm(@AuthenticationPrincipal UserDetails pilotDetails,
                             @RequestParam Long flightId,
                             Model model) {

        User pilot = userService.findByUsername(pilotDetails.getUsername());
        Flight flight = flightService.findFlightById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Рейс не найден"));

        // Защита: пилот не может писать отчёты за другой рейс
        // Используем сравнение по userId для надежности
        if (flight.getPilot() == null || 
            flight.getPilot().getUserId() == null || 
            !flight.getPilot().getUserId().equals(pilot.getUserId())) {

            // Перенаправляем на страницу рейсов с сообщением об ошибке
            model.addAttribute("error", "Вы не назначены на этот рейс. Вы можете создавать отчёты только для рейсов, назначенных вам.");
            model.addAttribute("flights", flightService.findFlightsByPilotWithoutLogs(pilot));
            return "pilot/flights";
        }

        // Проверка: можно создавать журнал только для рейсов со статусом completed
        if (flight.getStatus() != Flight.FlightStatus.completed) {
            model.addAttribute("error", "Лётный журнал можно создать только для завершенных рейсов (статус: completed).");
            model.addAttribute("flights", flightService.findFlightsByPilotWithoutLogs(pilot));
            return "pilot/flights";
        }

        model.addAttribute("log", new FlightLog());
        model.addAttribute("flightId", flightId);

        return "pilot/log_form";
    }


    // Сохранение летного отчета

    @PostMapping("/logs/save")
    public String saveLog(@AuthenticationPrincipal UserDetails pilotDetails,
                          @RequestParam Long flightId,
                          @RequestParam String technicalCheck,
                          @ModelAttribute("log") FlightLog log,
                          Model model) {

        User pilot = userService.findByUsername(pilotDetails.getUsername());
        Flight flight = flightService.findFlightById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Рейс не найден"));

        // Защита от доступа к чужому рейсу
        // Используем сравнение по userId для надежности
        if (flight.getPilot() == null || 
            flight.getPilot().getUserId() == null || 
            !flight.getPilot().getUserId().equals(pilot.getUserId())) {

            // Перенаправляем на страницу рейсов с сообщением об ошибке
            model.addAttribute("error", "Вы не можете добавить отчёт к рейсу, на который не назначены.");
            model.addAttribute("flights", flightService.findFlightsByPilotWithoutLogs(pilot));
            return "pilot/flights";
        }

        // Проверка: можно создавать журнал только для рейсов со статусом completed
        if (flight.getStatus() != Flight.FlightStatus.completed) {
            model.addAttribute("error", "Лётный журнал можно создать только для завершенных рейсов (статус: completed).");
            model.addAttribute("flights", flightService.findFlightsByPilotWithoutLogs(pilot));
            return "pilot/flights";
        }

        // Связь отчёта с пилотом и рейсом
        log.setPilot(pilot);
        log.setFlight(flight);
        
        // Устанавливаем техническую проверку из параметра запроса
        if (technicalCheck != null && !technicalCheck.trim().isEmpty()) {
            try {
                log.setTechnicalCheck(FlightLog.TechnicalCheck.valueOf(technicalCheck));
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Неверное значение технической проверки");
                model.addAttribute("log", log);
                model.addAttribute("flightId", flightId);
                return "pilot/log_form";
            }
        }

        flightLogService.save(log);

        return "redirect:/pilot/logs";
    }
}
