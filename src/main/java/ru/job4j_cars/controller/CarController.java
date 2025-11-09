package ru.job4j_cars.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CarController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Job4j Cars");
        model.addAttribute("message", "Добро пожаловать в автосалон!");
        return "index";
    }

    @GetMapping("/cars")
    public String cars(Model model) {
        model.addAttribute("pageTitle", "Список автомобилей");
        return "cars";
    }
}