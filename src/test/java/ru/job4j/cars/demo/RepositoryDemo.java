package ru.job4j.cars.demo;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.repository.*;

public class RepositoryDemo {

    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();

        try (SessionFactory sf = new MetadataSources(registry)
                .buildMetadata().buildSessionFactory()) {

            var crudRepository = new CrudRepository(sf);

            // Создаем все репозитории
            var userRepository = new UserRepository(crudRepository);
            var carRepository = new CarRepository(crudRepository);
            var engineRepository = new EngineRepository(crudRepository);
            var ownerRepository = new OwnerRepository(crudRepository);
            var participatesRepository = new ParticipatesRepository(crudRepository);

            // Тестируем работу репозиториев
            testRepositories(userRepository, carRepository, engineRepository,
                    ownerRepository, participatesRepository);

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static void testRepositories(UserRepository userRepo,
                                         CarRepository carRepo,
                                         EngineRepository engineRepo,
                                         OwnerRepository ownerRepo,
                                         ParticipatesRepository partRepo) {
        System.out.println("=== ТЕСТИРОВАНИЕ РЕПОЗИТОРИЕВ ===");

        // Тест EngineRepository
        System.out.println("\n1. Все двигатели:");
        engineRepo.findAllOrderById().forEach(e ->
                System.out.printf("  Engine: %s (%.1fL, %d л.с.)%n",
                        e.getName(), e.getVolume(), e.getPower()));

        // Тест CarRepository
        System.out.println("\n2. Все автомобили:");
        carRepo.findAllOrderById().forEach(c ->
                System.out.printf("  Car: %s %s (%d)%n",
                        c.getName(), c.getModel(), c.getManufactureYear()));

        // Тест OwnerRepository
        System.out.println("\n3. Все владельцы:");
        ownerRepo.findAllOrderById().forEach(o ->
                System.out.printf("  Owner: %s (User: %s)%n",
                        o.getName(), o.getUser().getLogin()));

        // Тест поиска по критериям
        System.out.println("\n4. Автомобили 2018 года:");
        carRepo.findByManufactureYear(2018).forEach(c ->
                System.out.printf("  %s %s%n", c.getName(), c.getModel()));

        System.out.println("\n=== ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===");
    }
}