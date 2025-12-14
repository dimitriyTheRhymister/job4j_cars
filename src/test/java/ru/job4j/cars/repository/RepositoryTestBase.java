package ru.job4j.cars.repository;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

public abstract class RepositoryTestBase {
    protected SessionFactory sf;
    protected CrudRepository crudRepository;

    @BeforeEach
    public void init() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml") // будет использовать тестовый из test/resources
                .build();
        try {
            sf = new MetadataSources(registry).buildMetadata().buildSessionFactory();
            crudRepository = new CrudRepository(sf);
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    @AfterEach
    public void destroy() {
        if (sf != null) {
            sf.close();
        }
    }

    protected void clearDatabase() {
        // Удаление в правильном порядке из-за foreign key constraints
        // Используем Map.of() для пустого Map
        crudRepository.run("DELETE FROM Participates", Map.of());
        crudRepository.run("DELETE FROM PriceHistory", Map.of());
        crudRepository.run("DELETE FROM Post", Map.of());
        crudRepository.run("DELETE FROM Owner", Map.of());
        crudRepository.run("DELETE FROM Car", Map.of());
        crudRepository.run("DELETE FROM Engine", Map.of());
        crudRepository.run("DELETE FROM User", Map.of());
    }
}