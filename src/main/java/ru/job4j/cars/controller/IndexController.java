package ru.job4j.cars.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.service.PostService;

import java.util.List;

@Controller
@AllArgsConstructor
public class IndexController {
    private final PostService postService;

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Валидация параметров
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 50) {
            size = 10;
        }
        // максимум 50 на странице

        List<Post> posts = postService.findActivePage(page, size);
        long total = postService.countActive();
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPosts", total);

        return "index";
    }
}