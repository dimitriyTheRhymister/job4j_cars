package ru.job4j.cars.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.cars.model.Post.PostStatus; // Импортируем enum

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PostDto {
    private Integer id;

    @NotBlank(message = "Описание обязательно")
    private String description;

    @NotNull(message = "Цена обязательна")
    @Min(value = 1, message = "Цена должна быть больше 0")
    private Long price;

    @NotBlank(message = "Марка обязательна")
    private String brand;

    @NotBlank(message = "Модель обязательна")
    private String model;

    @NotNull(message = "Год выпуска обязателен")
    @Min(value = 1900, message = "Некорректный год")
    private Integer manufactureYear;

    @NotBlank(message = "Тип кузова обязателен")
    private String bodyType;

    @NotBlank(message = "Тип двигателя обязателен")
    private String engineType;

    @NotBlank(message = "Коробка передач обязательна")
    private String transmission;

    @NotNull(message = "Пробег обязателен")
    @Min(value = 0, message = "Пробег не может быть отрицательным")
    private Integer mileage;

    private String color;

    private String category;

    private List<MultipartFile> photos;

    private Integer engineId;

    // Для отображения
    private String userLogin;
    private List<String> photoUrls;

    // ИЗМЕНЕНИЕ: используем Post.PostStatus вместо String
    private PostStatus status; // было: private String status;
}