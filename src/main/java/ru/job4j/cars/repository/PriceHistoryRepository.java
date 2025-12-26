package ru.job4j.cars.repository;

import org.hibernate.SessionFactory;
import ru.job4j.cars.model.PriceHistory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceHistoryRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceHistoryRepository.class);
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