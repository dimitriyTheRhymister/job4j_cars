package ru.job4j.cars.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.service.PostService;

import java.util.List;

@Controller
@AllArgsConstructor
public class IndexController {
    private final PostService postService;

    @GetMapping("/")
    public String index(Model model) {
        // МЕНЯЕМ НАЗАД: findAllActive() вместо findAll()
        List<Post> posts = postService.findAllActive();  // ← ТОЛЬКО активные!
        model.addAttribute("posts", posts);
        return "index";
    }
}