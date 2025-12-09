package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import ru.job4j.cars.model.Engine;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class EngineRepository {
    private final CrudRepository crudRepository;

    /**
     * Сохранить в базе.
     * @param engine двигатель.
     * @return двигатель с id.
     */
    public Engine create(Engine engine) {
        crudRepository.run(session -> session.persist(engine));
        return engine;
    }

    /**
     * Обновить в базе двигатель.
     * @param engine двигатель.
     */
    public void update(Engine engine) {
        crudRepository.run(session -> session.merge(engine));
    }

    /**
     * Удалить двигатель по id.
     * @param engineId ID
     */
    public void delete(int engineId) {
        crudRepository.run(
                "DELETE FROM Engine WHERE id = :fId",
                Map.of("fId", engineId)
        );
    }

    /**
     * Найти двигатель по ID
     * @param engineId ID
     * @return двигатель.
     */
    public Optional<Engine> findById(int engineId) {
        return crudRepository.optional(
                "FROM Engine WHERE id = :fId",
                Engine.class,
                Map.of("fId", engineId)
        );
    }

    /**
     * Список всех двигателей отсортированных по id.
     * @return список двигателей.
     */
    public List<Engine> findAllOrderById() {
        return crudRepository.query(
                "FROM Engine ORDER BY id ASC",
                Engine.class
        );
    }

    /**
     * Найти двигатели по названию.
     * @param name название двигателя.
     * @return список двигателей.
     */
    public List<Engine> findByName(String name) {
        return crudRepository.query(
                "FROM Engine WHERE name = :fName",
                Engine.class,
                Map.of("fName", name)
        );
    }

    /**
     * Найти двигатели с объемом больше указанного.
     * @param minVolume минимальный объем.
     * @return список двигателей.
     */
    public List<Engine> findByVolumeGreaterThan(double minVolume) {
        return crudRepository.query(
                "FROM Engine WHERE volume > :fVolume",
                Engine.class,
                Map.of("fVolume", minVolume)
        );
    }

    /**
     * Найти двигатели в диапазоне мощности.
     * @param minPower минимальная мощность.
     * @param maxPower максимальная мощность.
     * @return список двигателей.
     */
    public List<Engine> findByPowerBetween(int minPower, int maxPower) {
        return crudRepository.query(
                "FROM Engine WHERE power BETWEEN :fMinPower AND :fMaxPower",
                Engine.class,
                Map.of("fMinPower", minPower, "fMaxPower", maxPower)
        );
    }
}