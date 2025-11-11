package ru.cars.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.cars.model.Car;
import ru.cars.repository.CarRepository;

import java.util.List;

@Controller
public class CarController {

    @Autowired
    private CarRepository carRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Job4j Cars");
        model.addAttribute("message", "Добро пожаловать в автосалон!");
        return "index";
    }

    @GetMapping("/cars")
    public String cars(Model model) {
        List<Car> cars = carRepository.findAll();
        model.addAttribute("pageTitle", "Список автомобилей");
        model.addAttribute("cars", cars);
        return "cars";
    }
}