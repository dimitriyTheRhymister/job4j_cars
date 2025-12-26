package ru.job4j.cars.presenter;

import ru.job4j.cars.dto.PostDto;
import ru.job4j.cars.model.Post;

import java.util.List;

public class PostPresenter {

    /**
     * Преобразует сущность Post в PostDto для отображения в форме редактирования.
     * Защищён от NullPointerException.
     */
    public static PostDto toEditDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setDescription(post.getDescription());
        dto.setPrice(post.getPrice());
        dto.setMileage(post.getMileage());
        dto.setBodyType(post.getBodyType());
        dto.setEngineType(post.getEngineType());
        dto.setTransmission(post.getTransmission());
        dto.setColor(post.getColor());

        // Защита от null для Car
        if (post.getCar() != null) {
            dto.setBrand(post.getCar().getBrand());
            dto.setModel(post.getCar().getModel());
            dto.setManufactureYear(post.getCar().getManufactureYear());
            if (post.getCar().getEngine() != null) {
                dto.setEngineId(post.getCar().getEngine().getId());
            }
        }

        // Защита от null для User
        if (post.getUser() != null) {
            dto.setUserLogin(post.getUser().getLogin());
        }

        // Статус с защитой от null
        dto.setStatus(post.getStatus() != null
                ? post.getStatus()
                : Post.PostStatus.ACTIVE);

        // Фото: пустой список вместо null
        dto.setPhotoUrls(post.getPhotoUrls() != null
                ? post.getPhotoUrls()
                : List.of());

        return dto;
    }
}