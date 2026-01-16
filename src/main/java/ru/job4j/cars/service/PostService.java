package ru.job4j.cars.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.cars.dto.PostDto;
import ru.job4j.cars.model.*;
import ru.job4j.cars.repository.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final CarRepository carRepository;
    private final EngineRepository engineRepository;
    private final UserRepository userRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    private static final String UPLOAD_DIR = "./uploads/";

    @Transactional
    public Post create(PostDto postDto, User user) {
        Post post = buildPost(postDto, user);
        Car savedCar = buildAndSaveCar(postDto);
        post.setCar(savedCar);

        Post savedPost = postRepository.save(post);

        createPriceHistory(savedPost, postDto.getPrice());
        savePhotosIfPresent(savedPost, postDto.getPhotos());

        return savedPost;
    }

    private Post buildPost(PostDto postDto, User user) {
        Post post = new Post();
        post.setDescription(postDto.getDescription());
        post.setPrice(postDto.getPrice());
        post.setUser(user);
        post.setBodyType(postDto.getBodyType());
        post.setEngineType(postDto.getEngineType());
        post.setTransmission(postDto.getTransmission());
        post.setMileage(postDto.getMileage());
        post.setColor(postDto.getColor());
        post.setStatus(postDto.getStatus() != null ? postDto.getStatus() : Post.PostStatus.ACTIVE);
        return post;
    }

    private Car buildAndSaveCar(PostDto postDto) {
        Car car = new Car();
        car.setBrand(postDto.getBrand());
        car.setModel(postDto.getModel());
        car.setManufactureYear(postDto.getManufactureYear());

        Engine engine = engineRepository.findById(postDto.getEngineId())
                .orElseThrow(() -> new RuntimeException("Двигатель не найден"));
        car.setEngine(engine);

        return carRepository.create(car);
    }

    private void createPriceHistory(Post post, Long price) {
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setBefore(0L);
        priceHistory.setAfter(price);
        priceHistory.setPost(post);
        priceHistoryRepository.save(priceHistory);
    }

    private void savePhotosIfPresent(Post post, List<MultipartFile> photos) {
        if (photos != null && !photos.isEmpty()) {
            List<String> photoUrls = savePhotos(photos);
            post.setPhotoUrls(photoUrls);
            postRepository.update(post);
        }
    }

    private List<String> savePhotos(List<MultipartFile> photos) {
        List<String> photoUrls = new ArrayList<>();
        for (MultipartFile photo : photos) {
            if (!photo.isEmpty()) {
                try {
                    String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                    Path uploadPath = Paths.get(UPLOAD_DIR);

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(photo.getInputStream(), filePath);
                    photoUrls.add("/uploads/" + fileName);
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка при сохранении фото", e);
                }
            }
        }
        return photoUrls;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public List<Post> findAllActive() {
        return postRepository.findActive();
    }

    // === НОВЫЙ МЕТОД: пагинация активных объявлений ===
    public List<Post> findActivePage(int page, int size) {
        int offset = page * size;
        return postRepository.findActivePage(offset, size);
    }

    // === НОВЫЙ МЕТОД: общее количество активных объявлений ===
    public long countActive() {
        return postRepository.countActive();
    }

    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }

    public List<Post> findByUserId(int userId) {
        return postRepository.findByUserId(userId);
    }

    @Transactional
    public boolean updateStatus(int postId, Post.PostStatus status, int userId) {
        try {
            postRepository.updateStatus(postId, status, userId);
            return true;
        } catch (Exception e) {
            log.error("ERROR updating status: {}", e.getMessage());
            return false;
        }
    }

    public List<String> getAllBrands() {
        List<String> brands = postRepository.findDistinctBrands();
        if (brands.isEmpty()) {
            return List.of("Toyota", "Honda", "Ford", "BMW", "Mercedes", "Audi",
                    "Volkswagen", "Nissan", "Hyundai", "Kia", "Lada",
                    "Chevrolet", "Renault", "Peugeot", "Skoda", "Mazda");
        }
        return brands;
    }

    public List<String> getAllBodyTypes() {
        List<String> bodyTypes = postRepository.findDistinctBodyTypes();
        if (bodyTypes.isEmpty()) {
            return List.of("Седан", "Хэтчбек", "Универсал", "Внедорожник", "Купе",
                    "Кабриолет", "Минивэн", "Пикап", "Лифтбек", "Фургон");
        }
        return bodyTypes;
    }

    public List<String> getAllCategories() {
        return List.of("Седан", "Хэтчбек", "Универсал", "Внедорожник", "Купе",
                "Кабриолет", "Минивэн", "Пикап");
    }

    public List<String> getAllTransmissions() {
        return List.of("Автоматическая", "Механическая", "Роботизированная", "Вариатор");
    }

    public List<String> getAllEngineTypes() {
        return List.of("Бензиновый", "Дизельный", "Гибридный", "Электрический", "Газовый");
    }

    public List<String> getAllColors() {
        return List.of("Черный", "Белый", "Серый", "Серебристый", "Синий",
                "Красный", "Зеленый", "Желтый", "Коричневый", "Оранжевый");
    }

    @Transactional
    public void update(int postId, PostDto postDto, User user) {
        Post post = findById(postId)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        validateOwnership(post, user);
        updatePostFields(post, postDto);

        if (postDto.getPhotos() != null && !postDto.getPhotos().isEmpty()) {
            List<String> existingUrls = post.getPhotoUrls() != null ? post.getPhotoUrls() : new ArrayList<>();
            List<String> newUrls = saveNewPhotos(postDto.getPhotos());
            existingUrls.addAll(newUrls);
            post.setPhotoUrls(existingUrls);
        }

        postRepository.update(post);
    }

    private List<String> saveNewPhotos(List<MultipartFile> photos) {
        List<String> photoUrls = new ArrayList<>();
        for (MultipartFile photo : photos) {
            if (!photo.isEmpty()) {
                try {
                    String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                    Path uploadPath = Paths.get(UPLOAD_DIR);

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(photo.getInputStream(), filePath);
                    photoUrls.add("/uploads/" + fileName);
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка при сохранении фото", e);
                }
            }
        }
        return photoUrls;
    }

    private void validateOwnership(Post post, User user) {
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет прав на редактирование");
        }
    }

    private void updatePostFields(Post post, PostDto postDto) {
        post.setDescription(postDto.getDescription());
        post.setPrice(postDto.getPrice());
        post.setBodyType(postDto.getBodyType());
        post.setEngineType(postDto.getEngineType());
        post.setTransmission(postDto.getTransmission());
        post.setMileage(postDto.getMileage());
        post.setColor(postDto.getColor());
        post.setStatus(postDto.getStatus());
        updateCarFields(post.getCar(), postDto);
    }

    private void updateCarFields(Car car, PostDto postDto) {
        car.setBrand(postDto.getBrand());
        car.setModel(postDto.getModel());
        car.setManufactureYear(postDto.getManufactureYear());

        Engine engine = engineRepository.findById(postDto.getEngineId())
                .orElseThrow(() -> new RuntimeException("Двигатель не найден"));
        car.setEngine(engine);

        carRepository.update(car);
    }

    @Transactional
    public boolean deletePhoto(int postId, String photoUrl, int userId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return false;
        }

        Post post = postOptional.get();
        if (post.getUser().getId() != userId) {
            return false;
        }

        List<String> photoUrls = post.getPhotoUrls();
        if (photoUrls != null && photoUrls.contains(photoUrl)) {
            photoUrls.remove(photoUrl);
            post.setPhotoUrls(photoUrls);
            postRepository.update(post);
            deletePhysicalFile(photoUrl);
            return true;
        }

        return false;
    }

    private void deletePhysicalFile(String photoUrl) {
        try {
            String fileName = photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении файла: {}", e.getMessage());
        }
    }

    public List<Post> findByStatus(Post.PostStatus status) {
        return postRepository.findByStatus(status);
    }

    public List<Post> findByUserIdAndStatus(int userId, Post.PostStatus status) {
        return postRepository.findByUserIdAndStatus(userId, status);
    }
}