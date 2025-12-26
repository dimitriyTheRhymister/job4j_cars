package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import ru.job4j.cars.model.Owner;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class OwnerRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerRepository.class);
    private final CrudRepository crudRepository;

    /**
     * Сохранить в базе.
     * @param owner владелец.
     * @return владелец с id.
     */
    public Owner create(Owner owner) {
        crudRepository.run(session -> session.persist(owner));
        return owner;
    }

    /**
     * Обновить в базе владельца.
     * @param owner владелец.
     */
    public void update(Owner owner) {
        crudRepository.run(session -> session.merge(owner));
    }

    /**
     * Удалить владельца по id.
     * @param ownerId ID
     */
    public void delete(int ownerId) {
        crudRepository.run(
                "DELETE FROM Owner WHERE id = :fId",
                Map.of("fId", ownerId)
        );
    }

    /**
     * Найти владельца по ID
     * @param ownerId ID
     * @return владелец.
     */
    public Optional<Owner> findById(int ownerId) {
        return crudRepository.optional(
                "FROM Owner o LEFT JOIN FETCH o.user WHERE o.id = :fId",
                Owner.class,
                Map.of("fId", ownerId)
        );
    }

    /**
     * Список всех владельцев отсортированных по id.
     * @return список владельцев.
     */
    public List<Owner> findAllOrderById() {
        return crudRepository.query(
                "SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.user ORDER BY o.id ASC",
                Owner.class
        );
    }

    /**
     * Найти владельцев по имени.
     * @param name имя владельца.
     * @return список владельцев.
     */
    public List<Owner> findByName(String name) {
        return crudRepository.query(
                "FROM Owner WHERE name = :fName",
                Owner.class,
                Map.of("fName", name)
        );
    }

    /**
     * Найти владельцев по ID пользователя.
     * @param userId ID пользователя.
     * @return список владельцев.
     */
    public List<Owner> findByUserId(int userId) {
        return crudRepository.query(
                "FROM Owner WHERE user.id = :fUserId",
                Owner.class,
                Map.of("fUserId", userId)
        );
    }

    /**
     * Найти владельцев автомобиля по ID автомобиля.
     * @param carId ID автомобиля.
     * @return список владельцев.
     */
    public List<Owner> findByCarId(int carId) {
        return crudRepository.query(
                "SELECT o FROM Car c JOIN c.owners o WHERE c.id = :fCarId",
                Owner.class,
                Map.of("fCarId", carId)
        );
    }
}