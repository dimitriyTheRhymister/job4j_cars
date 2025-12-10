package ru.job4j.cars.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "auto_post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    private String description;
    private LocalDateTime created = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "auto_user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    private List<PriceHistory> priceHistories = new ArrayList<>();

    private Long currentPrice;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participates> subscribers = new ArrayList<>();

    // ДОБАВЛЕНО: Поле для хранения фото (URL-адреса изображений)
    @ElementCollection(fetch = FetchType.EAGER)  // ИЛИ LAZY с JOIN FETCH в запросах
    @CollectionTable(name = "post_photos", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "photo_url")
    @Fetch(FetchMode.JOIN)  // Важно для предотвращения N+1 проблемы
    private List<String> photoUrls = new ArrayList<>();

    /**
     * Проверяет, есть ли у объявления фото
     * @return true если есть хотя бы одно фото
     */
    public boolean hasPhotos() {
        return photoUrls != null && !photoUrls.isEmpty();
    }

    /**
     * Добавляет фото к объявлению
     * @param photoUrl URL фото
     */
    public void addPhoto(String photoUrl) {
        if (photoUrls == null) {
            photoUrls = new ArrayList<>();
        }
        photoUrls.add(photoUrl);
    }

    /**
     * Удаляет фото из объявления
     * @param photoUrl URL фото для удаления
     */
    public void removePhoto(String photoUrl) {
        if (photoUrls != null) {
            photoUrls.remove(photoUrl);
        }
    }

    /**
     * Получить количество фото
     * @return количество фото
     */
    public int getPhotoCount() {
        return photoUrls != null ? photoUrls.size() : 0;
    }
}