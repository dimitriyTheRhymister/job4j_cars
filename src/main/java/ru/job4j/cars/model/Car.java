package ru.job4j.cars.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    private String name;
    private String model;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @ManyToOne
    @JoinColumn(name = "engine_id")
    private Engine engine;

    @OneToOne(mappedBy = "car")
    private Post post;
}