package ru.job4j.cars.repository;

import ru.job4j.cars.model.PriceHistory;

import java.util.List;

public class PriceHistoryRepository {
    private final CrudRepository crudRepository;

    public PriceHistoryRepository(CrudRepository crudRepository) {
        this.crudRepository = crudRepository;
    }

    public PriceHistory save(PriceHistory priceHistory) {
        crudRepository.run(session -> session.persist(priceHistory));
        return priceHistory;
    }

    public List<PriceHistory> findAllOrderById() {
        return crudRepository.query(
                "FROM PriceHistory ph ORDER BY ph.id", PriceHistory.class);
    }
}