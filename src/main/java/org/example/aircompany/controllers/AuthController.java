package org.example.aircompany.controllers;

import org.example.aircompany.model.User;
import org.example.aircompany.services.UserService; // Пока этого сервиса нет, но он нужен для регистрации
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService; // Инжектируем UserService

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Обработка GET-запроса на корневой путь (главная страница)
    @GetMapping("/")
    public String home() {
        return "index"; // Имя Thymeleaf-шаблона: /resources/templates/index.html
    }

    // Обработка GET-запроса на /login (страница входа)
    @GetMapping("/login")
    public String login() {
        return "auth/login"; // Имя Thymeleaf-шаблона: /resources/templates/auth/login.html
    }

    // Обработка GET-запроса на /register (страница регистрации)
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // Добавляем пустой объект User для формы
        return "auth/register"; // Имя Thymeleaf-шаблона: /resources/templates/auth/register.html
    }

    // Обработка POST-запроса с формы регистрации
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {

        // ВАЖНО: Здесь будет вызов метода из UserService для сохранения и хеширования пароля
        if (userService.registerNewPassenger(user)) {
            // Если регистрация успешна
            return "redirect:/login?success";
        } else {
            // Если пользователь с таким username уже существует
            model.addAttribute("registrationError", "Пользователь с таким именем уже существует.");
            return "auth/register";
        }
    }
}