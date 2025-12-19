package ru.job4j.cars.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.cars.dto.PostDto;
import ru.job4j.cars.model.Engine;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.User;
import ru.job4j.cars.service.EngineService;
import ru.job4j.cars.service.PostService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final EngineService engineService;

    @GetMapping("/create")
    public String getCreationForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("postDto", new PostDto());
        model.addAttribute("brands", postService.getAllBrands());
        model.addAttribute("bodyTypes", postService.getAllBodyTypes());
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("transmissions", postService.getAllTransmissions());
        model.addAttribute("engineTypes", postService.getAllEngineTypes());
        model.addAttribute("colors", postService.getAllColors());
        model.addAttribute("engines", engineService.findAll());
        return "post/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute PostDto postDto,
                         BindingResult bindingResult,
                         @RequestParam("photos") List<MultipartFile> photos,
                         HttpSession session,
                         Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            addAttributesForForm(model);
            return "post/create";
        }

        postDto.setPhotos(photos);
        postService.create(postDto, user);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable int id, Model model) {  // УБРАЛ HttpSession session
        // 1. Находим пост
        Optional<Post> postOptional = postService.findById(id);

        if (postOptional.isEmpty()) {
            model.addAttribute("message", "Объявление не найдено");
            return "error/404";
        }

        Post post = postOptional.get();

        // 2. Добавляем в модель ТОЛЬКО post
        model.addAttribute("post", post);
        // УДАЛИЛ: model.addAttribute("user", sessionUser);

        return "post/details";
    }

    @PostMapping("/{id}/sell")
    public String markAsSold(@PathVariable int id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        postService.updateStatus(id, Post.PostStatus.SOLD, user.getId());
        return "redirect:/posts/my";
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable int id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        postService.updateStatus(id, Post.PostStatus.ARCHIVED, user.getId());
        return "redirect:/posts/my";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable int id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        postService.updateStatus(id, Post.PostStatus.ACTIVE, user.getId());
        return "redirect:/posts/my";
    }

    private void addAttributesForForm(Model model) {
        model.addAttribute("brands", postService.getAllBrands());
        model.addAttribute("bodyTypes", postService.getAllBodyTypes());
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("transmissions", postService.getAllTransmissions());
        model.addAttribute("engineTypes", postService.getAllEngineTypes());
        model.addAttribute("colors", postService.getAllColors());
        model.addAttribute("engines", engineService.findAll());
    }

    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable int id,
                              Model model,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        Optional<Post> postOptional = postService.findById(id);
        if (postOptional.isEmpty()) {
            return "redirect:/";
        }

        Post post = postOptional.get();

        // Проверяем, что это объявление пользователя
        if (!post.getUser().getId().equals(user.getId())) {
            return "redirect:/posts/" + id;
        }

        // Заполняем модель
        fillEditModel(post, model);

        return "post/edit";
    }

    private void fillEditModel(Post post, Model model) {
        // Создаем DTO
        PostDto postDto = new PostDto();
        postDto.setDescription(post.getDescription());
        postDto.setPrice(post.getPrice());
        postDto.setBrand(post.getCar().getBrand());
        postDto.setModel(post.getCar().getModel());
        postDto.setManufactureYear(post.getCar().getManufactureYear());
        postDto.setBodyType(post.getBodyType());
        postDto.setEngineType(post.getEngineType());
        postDto.setTransmission(post.getTransmission());
        postDto.setMileage(post.getMileage());
        postDto.setColor(post.getColor());
        postDto.setEngineId(post.getCar().getEngine().getId());

        model.addAttribute("currentStatus", post.getStatus()); // ← НОВАЯ СТРОКА
        model.addAttribute("postDto", postDto);
        model.addAttribute("postId", post.getId());
        model.addAttribute("currentPhotos", post.getPhotoUrls()); // Текущие фото
        addAttributesForForm(model);
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id,
                         @Valid @ModelAttribute PostDto postDto,
                         BindingResult bindingResult,
                         @RequestParam(value = "newPhotos", required = false) List<MultipartFile> newPhotos,
                         HttpSession session,
                         Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (!hasEditPermission(id, user)) {
            return "redirect:/posts/" + id;
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            addAttributesForForm(model);
            return "post/edit";
        }

        // Обновляем пост с новыми фото
        postDto.setPhotos(newPhotos);
        postService.update(id, postDto, user);

        return "redirect:/posts/" + id;
    }

    // Удаление фото
    @PostMapping("/{id}/photos/delete")
    @ResponseBody
    public String deletePhoto(@PathVariable int id,
                              @RequestParam String photoUrl,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "ERROR: Not authorized";
        }

        boolean deleted = postService.deletePhoto(id, photoUrl, user.getId());
        return deleted ? "OK" : "ERROR";
    }

    /**
     * Проверяет права пользователя на редактирование поста
     */
    private boolean hasEditPermission(int postId, User user) {
        Optional<Post> postOptional = postService.findById(postId);
        return postOptional.isPresent()
                && postOptional.get().getUser().getId().equals(user.getId());
    }

    @GetMapping("/my")
    public String getMyPosts(@RequestParam(value = "status", required = false) String statusParam,
                             HttpSession session,
                             Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Post> posts;
        String filterName = "Все мои объявления";

        if (statusParam == null) {
            // Все мои объявления
            posts = postService.findByUserId(user.getId());
        } else {
            // Фильтр по статусу
            Post.PostStatus status = Post.PostStatus.valueOf(statusParam.toUpperCase());
            posts = postService.findByUserIdAndStatus(user.getId(), status);

            // Название фильтра
            switch (status) {
                case ACTIVE:
                    filterName = "Мои активные объявления";
                    break;
                case SOLD:
                    filterName = "Мои проданные автомобили";
                    break;
                case ARCHIVED:
                    filterName = "Мои объявления в архиве";
                    break;
                default:
                    filterName = "Мои объявления";
                    break;
            }
        }

        model.addAttribute("posts", posts);
        model.addAttribute("filterName", filterName);
        model.addAttribute("currentFilter", statusParam);
        return "post/my";
    }
}