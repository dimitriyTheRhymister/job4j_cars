package ru.job4j.cars.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.cars.dto.PostDto;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.User;
import ru.job4j.cars.presenter.PostPresenter;
import ru.job4j.cars.service.EngineService;
import ru.job4j.cars.service.PostService;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
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
        addAttributesForForm(model);
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
    public String getById(@PathVariable int id, Model model) {
        Optional<Post> postOptional = postService.findById(id);

        if (postOptional.isEmpty()) {
            model.addAttribute("message", "Объявление не найдено");
            return "error/404";
        }

        Post post = postOptional.get();
        model.addAttribute("post", post);
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

        if (!post.getUser().getId().equals(user.getId())) {
            return "redirect:/posts/" + id;
        }

        fillEditModel(post, model);
        return "post/edit";
    }

    private void fillEditModel(Post post, Model model) {
        PostDto postDto = PostPresenter.toEditDto(post);
        model.addAttribute("currentStatus", postDto.getStatus());
        model.addAttribute("postDto", postDto);
        model.addAttribute("postId", post.getId());
        model.addAttribute("currentPhotos", post.getPhotoUrls());
        addAttributesForForm(model);
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id,
                         @Valid @ModelAttribute PostDto postDto,
                         BindingResult bindingResult,
                         @RequestParam(value = "newPhotos", required = false) List<MultipartFile> newPhotos,
                         HttpSession session,
                         Model model) {
        // ЕДИНСТВЕННЫЙ БЛОК ОТЛАДКИ
        log.debug("=== EDIT FORM SUBMISSION ===");
        log.debug("Post ID: {}", id);
        log.debug("Status from form: {}", postDto.getStatus());
        log.debug("Description: {}", postDto.getDescription());

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

        postDto.setPhotos(newPhotos);
        postService.update(id, postDto, user);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/photos/delete")
    @ResponseBody
    public ResponseEntity<?> deletePhoto(@PathVariable int id,
                                         @RequestParam String photoUrl,
                                         HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Не авторизован"));
        }

        boolean deleted = postService.deletePhoto(id, photoUrl, user.getId());

        if (deleted) {
            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Фотография удалена"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Ошибка при удалении фотографии"));
        }
    }

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
            posts = postService.findByUserId(user.getId());
        } else {
            Post.PostStatus status = Post.PostStatus.valueOf(statusParam.toUpperCase());
            posts = postService.findByUserIdAndStatus(user.getId(), status);

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