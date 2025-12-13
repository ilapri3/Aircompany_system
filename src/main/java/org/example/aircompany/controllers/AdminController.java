package org.example.aircompany.controllers;

import org.example.aircompany.model.User;
import org.example.aircompany.model.UserRole;
import org.example.aircompany.services.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AircraftService aircraftService;
    private final FlightService flightService;
    private final BookingService bookingService;
    private final FlightLogService flightLogService;

    public AdminController(UserService userService,
                           AircraftService aircraftService,
                           FlightService flightService,
                           BookingService bookingService,
                           FlightLogService flightLogService) {

        this.userService = userService;
        this.aircraftService = aircraftService;
        this.flightService = flightService;
        this.bookingService = bookingService;
        this.flightLogService = flightLogService;
    }

    // ------------------ УПРАВЛЕНИЕ РОЛЯМИ ------------------

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("roles", UserRole.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/update-role")
    public String updateRole(@PathVariable Long id,
                             @RequestParam("role") UserRole newRole,
                             @AuthenticationPrincipal UserDetails adminDetails) {

        // Загружаем администратора, который меняет роль
        User admin = userService.findByUsername(adminDetails.getUsername());

        userService.updateUserRole(id, newRole, admin);

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails adminDetails) {

        // Загружаем администратора
        User admin = userService.findByUsername(adminDetails.getUsername());

        // Проверяем, что админ не пытается удалить самого себя
        if (admin.getUserId().equals(id)) {
            throw new IllegalArgumentException("Администратор не может удалить самого себя");
        }

        userService.deleteUserById(id);

        return "redirect:/admin/users";
    }



    // ------------------ СТАТИСТИКА ------------------

    @GetMapping("/stats")
    public String viewStats(Model model) {

        // Пользователи
        model.addAttribute("totalUsers", userService.findAllUsers().size());
        model.addAttribute("totalPassengers", userService.countByRole(UserRole.passenger));
        model.addAttribute("totalPilots", userService.countByRole(UserRole.pilot));
        model.addAttribute("totalStaff", userService.countByRole(UserRole.booking_staff));
        model.addAttribute("totalAdmins", userService.countByRole(UserRole.admin));

        // Самолёты
        model.addAttribute("aircraftActive", aircraftService.countByStatus("active"));
        model.addAttribute("aircraftMaintenance", aircraftService.countByStatus("in_maintenance"));
        model.addAttribute("aircraftInFlight", aircraftService.countByStatus("in_flight"));

        // Рейсы и бронирования
        model.addAttribute("totalFlights", flightService.findAllFlights().size());
        model.addAttribute("totalBookings", bookingService.findAllBookings().size());

        // Пилотские логи
        model.addAttribute("totalFlightLogs", flightLogService.findAll().size());

        return "admin/stats";
    }
}
