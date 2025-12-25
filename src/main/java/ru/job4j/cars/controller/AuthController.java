package ru.job4j.cars.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.job4j.cars.model.User;
import ru.job4j.cars.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;

@Controller
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/register")
    public String getRegistrationForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            userService.save(user);
            return "redirect:/auth/login";
        } catch (Exception e) {
            // Ловим любое исключение, связанное с уникальностью
            if (e.getCause() instanceof ConstraintViolationException
                    || e.getMessage().contains("unique")
                    || e.getMessage().contains("duplicate")) {
                model.addAttribute("error", "Пользователь с таким логином уже существует");
                return "auth/register";
            }
            // Или обобщённо:
            model.addAttribute("error", "Ошибка регистрации. Попробуйте другое имя.");
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String getLoginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute User user, Model model, HttpServletRequest request) {
        var userOptional = userService.findByLoginAndPassword(user.getLogin(), user.getPassword());
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Неверный логин или пароль");
            return "auth/login";
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", userOptional.get());
        return "redirect:/posts/my";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}