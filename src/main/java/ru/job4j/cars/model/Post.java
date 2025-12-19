package ru.job4j.cars.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_user_id")  // ИЗМЕНИТЕ С "user_id" на "auto_user_id"
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    private List<PriceHistory> priceHistories = new ArrayList<>();

    @Column(name = "currentprice")  // ДОБАВЬТЕ ЭТУ АННОТАЦИЮ
    private Long price;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participates> subscribers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_photos", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "photo_url")
    private List<String> photoUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.ACTIVE;

    // Добавляем поля для фильтрации
    @Column(name = "body_type")
    private String bodyType; // тип кузова

    @Column(name = "engine_type")
    private String engineType; // тип двигателя

    @Column(name = "transmission")
    private String transmission; // коробка передач

    @Column(name = "mileage")
    private Integer mileage; // пробег

    @Column(name = "color")
    private String color; // цвет

    public enum PostStatus {
        ACTIVE, SOLD, ARCHIVED
    }

    public boolean isSold() {
        return status == PostStatus.SOLD;
    }

    public boolean isActive() {
        return status == PostStatus.ACTIVE;
    }
}