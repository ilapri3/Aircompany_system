package org.example.aircompany.controllers;

import org.example.aircompany.model.Aircraft;
import org.example.aircompany.services.AircraftService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/booking-staff/aircrafts") // Доступно для admin и booking_staff
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    /** 1. Список всех самолетов (Read All) */
    @GetMapping
    public String listAircrafts(Model model) {
        List<Aircraft> aircrafts = aircraftService.findAllAircrafts();
        model.addAttribute("aircrafts", aircrafts);
        // Шаблон: /resources/templates/aircrafts/list.html
        return "aircrafts/list";
    }

    /** 2. Отображение формы создания/редактирования */
    @GetMapping("/new")
    public String showAircraftForm(Model model) {
        model.addAttribute("aircraft", new Aircraft());
        model.addAttribute("pageTitle", "Добавить новый самолет");
        // Шаблон: /resources/templates/aircrafts/form.html
        return "aircrafts/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditAircraftForm(@PathVariable("id") Long id, Model model) {
        Aircraft aircraft = aircraftService.findAircraftById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID самолета:" + id));
        model.addAttribute("aircraft", aircraft);
        model.addAttribute("pageTitle", "Редактировать самолет ID: " + id);
        return "aircrafts/form";
    }

    /** 3. Обработка сохранения самолета (Create/Update Submit) */
//    @PostMapping("/save")
//    public String saveAircraft(@ModelAttribute("aircraft") Aircraft aircraft) {
//        aircraftService.saveAircraft(aircraft);
//        return "redirect:/booking-staff/aircrafts";
//    }
    @PostMapping("/save")
    public String saveAircraft(@ModelAttribute("aircraft") Aircraft aircraft) {
        try {
            aircraftService.saveAircraft(aircraft);
            return "redirect:/booking-staff/aircrafts";
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения данных самолета. Смотрите логи.", e);
        }
    }

    /** 4. Удаление самолета (Delete) */
    @GetMapping("/delete/{id}")
    public String deleteAircraft(@PathVariable("id") Long id) {
        aircraftService.deleteAircraft(id);
        return "redirect:/booking-staff/aircrafts";
    }
}