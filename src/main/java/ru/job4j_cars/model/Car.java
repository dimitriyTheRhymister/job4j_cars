package ru.job4j_cars.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "production_year")
    private Integer productionYear;

    private BigDecimal price;  // ИЗМЕНИЛИ ТИП

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    // Конструкторы
    public Car() {}

    public Car(String brand, String model, Integer productionYear, BigDecimal price) {
        this.brand = brand;
        this.model = model;
        this.productionYear = productionYear;
        this.price = price;
        this.createdDate = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getProductionYear() { return productionYear; }
    public void setProductionYear(Integer productionYear) { this.productionYear = productionYear; }

    public BigDecimal getPrice() { return price; }  // ИЗМЕНИЛИ
    public void setPrice(BigDecimal price) { this.price = price; }  // ИЗМЕНИЛИ

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}