package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import ru.job4j.cars.model.Car;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class CarRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarRepository.class);
    private final CrudRepository crudRepository;

    /**
     * Сохранить в базе.
     * @param car автомобиль.
     * @return автомобиль с id.
     */
    public Car create(Car car) {
        crudRepository.run(session -> session.persist(car));
        return car;
    }

    /**
     * Обновить в базе автомобиль.
     * @param car автомобиль.
     */
    public void update(Car car) {
        crudRepository.run(session -> session.merge(car));
    }

    /**
     * Удалить автомобиль по id.
     * @param carId ID
     */
    public void delete(int carId) {
        crudRepository.run(
                "DELETE FROM Car WHERE id = :fId",
                Map.of("fId", carId)
        );
    }

    /**
     * Найти автомобиль по ID
     * @param carId ID
     * @return автомобиль.
     */
    public Optional<Car> findById(int carId) {
        return crudRepository.optional(
                "FROM Car c JOIN FETCH c.engine WHERE c.id = :fId",
                Car.class,
                Map.of("fId", carId)
        );
    }

    /**
     * Список всех автомобилей отсортированных по id.
     * @return список автомобилей.
     */
    public List<Car> findAllOrderById() {
        return crudRepository.query(
                "SELECT DISTINCT c FROM Car c JOIN FETCH c.engine ORDER BY c.id ASC",
                Car.class
        );
    }

    /**
     * Найти автомобили по марке.
     * @param name марка автомобиля.
     * @return список автомобилей.
     */
    public List<Car> findByName(String name) {
        return crudRepository.query(
                "FROM Car WHERE name = :fName",
                Car.class,
                Map.of("fName", name)
        );
    }

    /**
     * Найти автомобили по году выпуска.
     * @param year год выпуска.
     * @return список автомобилей.
     */
    public List<Car> findByManufactureYear(int year) {
        return crudRepository.query(
                "FROM Car WHERE manufactureYear = :fYear",
                Car.class,
                Map.of("fYear", year)
        );
    }

    /**
     * Найти автомобили с двигателями мощнее указанной.
     * @param minPower минимальная мощность.
     * @return список автомобилей.
     */
    public List<Car> findByEnginePowerGreaterThan(int minPower) {
        return crudRepository.query(
                "FROM Car c WHERE c.engine.power > :fPower",
                Car.class,
                Map.of("fPower", minPower)
        );
    }
}