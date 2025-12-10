package ru.job4j.cars.demo;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.*;
import ru.job4j.cars.repository.CrudRepository;

import java.util.Set;

public class TestDataGenerator {

    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();

        try (SessionFactory sf = new MetadataSources(registry)
                .buildMetadata().buildSessionFactory()) {

            var crudRepository = new CrudRepository(sf);
            executeDemo(crudRepository);

            System.out.println("\n✓ Теперь можно запускать RepositoryTestSuite!");
            System.out.println("✓ Или сразу запускать H2WebConsoleStarter!");

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static void executeDemo(CrudRepository crudRepository) {
        createTestData(crudRepository);
        displayAllData(crudRepository);
    }

    private static void createTestData(CrudRepository crudRepository) {
        crudRepository.tx(session -> {
            if (hasExistingData(session)) {
                System.out.println("Данные уже существуют, пропускаем создание");
                return null;
            }

            createUsers(session);
            createEngines(session);
            createCars(session);
            createOwners(session);
            createPostsWithPriceHistory(session);
            System.out.println("Тестовые данные созданы!");

            return null;
        });
    }

    private static boolean hasExistingData(org.hibernate.Session session) {
        Long count = session.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .uniqueResult();
        return count > 0;
    }

    private static void createUsers(org.hibernate.Session session) {
        var user1 = new User();
        user1.setLogin("ivanov");
        user1.setPassword("123");
        session.persist(user1);

        var user2 = new User();
        user2.setLogin("petrov");
        user2.setPassword("456");
        session.persist(user2);

        var user3 = new User();
        user3.setLogin("sidorov");
        user3.setPassword("789");
        session.persist(user3);
    }

    private static void createEngines(org.hibernate.Session session) {
        var engine1 = new Engine();
        engine1.setName("V6 3.5L");
        engine1.setVolume(3.5);
        engine1.setPower(249);
        session.persist(engine1);

        var engine2 = new Engine();
        engine2.setName("I4 2.0L");
        engine2.setVolume(2.0);
        engine2.setPower(150);
        session.persist(engine2);

        var engine3 = new Engine();
        engine3.setName("V8 5.7L");
        engine3.setVolume(5.7);
        engine3.setPower(345);
        session.persist(engine3);
    }

    private static void createCars(org.hibernate.Session session) {
        var engine1 = session.createQuery("FROM Engine WHERE name = 'V6 3.5L'", Engine.class)
                .uniqueResult();
        var engine2 = session.createQuery("FROM Engine WHERE name = 'I4 2.0L'", Engine.class)
                .uniqueResult();
        var engine3 = session.createQuery("FROM Engine WHERE name = 'V8 5.7L'", Engine.class)
                .uniqueResult();

        var car1 = new Car();
        car1.setName("Toyota");
        car1.setModel("Camry");
        car1.setManufactureYear(2015);
        car1.setEngine(engine1);
        session.persist(car1);

        var car2 = new Car();
        car2.setName("Honda");
        car2.setModel("Civic");
        car2.setManufactureYear(2018);
        car2.setEngine(engine2);
        session.persist(car2);

        var car3 = new Car();
        car3.setName("Ford");
        car3.setModel("Mustang");
        car3.setManufactureYear(2020);
        car3.setEngine(engine3);
        session.persist(car3);
    }

    private static void createOwners(org.hibernate.Session session) {
        var user1 = session.createQuery("FROM User WHERE login = 'ivanov'", User.class)
                .uniqueResult();
        var user2 = session.createQuery("FROM User WHERE login = 'petrov'", User.class)
                .uniqueResult();
        var user3 = session.createQuery("FROM User WHERE login = 'sidorov'", User.class)
                .uniqueResult();

        var car1 = session.createQuery("FROM Car WHERE name = 'Toyota'", Car.class)
                .uniqueResult();
        var car2 = session.createQuery("FROM Car WHERE name = 'Honda'", Car.class)
                .uniqueResult();
        var car3 = session.createQuery("FROM Car WHERE name = 'Ford'", Car.class)
                .uniqueResult();

        // Создаем владельцев
        var owner1 = new Owner();
        owner1.setName("Иванов Иван Иванович");
        owner1.setUser(user1);
        session.persist(owner1);

        var owner2 = new Owner();
        owner2.setName("Петров Петр Петрович");
        owner2.setUser(user2);
        session.persist(owner2);

        var owner3 = new Owner();
        owner3.setName("Сидоров Алексей");
        owner3.setUser(user3);
        session.persist(owner3);

        // Устанавливаем связи ManyToMany между Car и Owner
        car1.setOwners(Set.of(owner1, owner2)); // У Toyota два владельца
        car2.setOwners(Set.of(owner2));         // У Honda один владелец
        car3.setOwners(Set.of(owner3, owner1)); // У Ford два владельца

        session.update(car1);
        session.update(car2);
        session.update(car3);
    }

    private static void createPostsWithPriceHistory(org.hibernate.Session session) {
        createFirstPost(session);
        createSecondPost(session);
        createThirdPost(session);
    }

    private static void createFirstPost(org.hibernate.Session session) {
        var user1 = session.createQuery("FROM User WHERE login = 'ivanov'", User.class)
                .uniqueResult();
        var car1 = session.createQuery("FROM Car WHERE name = 'Toyota'", Car.class)
                .uniqueResult();

        var post1 = new Post();
        post1.setDescription("Продам Toyota Camry 2015 года в отличном состоянии");
        post1.setUser(user1);
        post1.setCar(car1);

        var price1 = new PriceHistory();
        price1.setBefore(1500000L);
        price1.setAfter(1400000L);
        post1.getPriceHistories().add(price1);

        session.persist(post1);
    }

    private static void createSecondPost(org.hibernate.Session session) {
        var user2 = session.createQuery("FROM User WHERE login = 'petrov'", User.class)
                .uniqueResult();
        var car2 = session.createQuery("FROM Car WHERE name = 'Honda'", Car.class)
                .uniqueResult();

        var post2 = new Post();
        post2.setDescription("Продам Honda Civic 2018, один владелец");
        post2.setUser(user2);
        post2.setCar(car2);

        addPriceHistoryToPost(post2, 1200000L, 1100000L);
        addPriceHistoryToPost(post2, 1100000L, 1000000L);

        session.persist(post2);
    }

    private static void createThirdPost(org.hibernate.Session session) {
        var user3 = session.createQuery("FROM User WHERE login = 'sidorov'", User.class)
                .uniqueResult();
        var car3 = session.createQuery("FROM Car WHERE name = 'Ford'", Car.class)
                .uniqueResult();

        var post3 = new Post();
        post3.setDescription("Ford Mustang 2020, спорткар, два владельца");
        post3.setUser(user3);
        post3.setCar(car3);

        addPriceHistoryToPost(post3, 3500000L, 3200000L);
        addPriceHistoryToPost(post3, 3200000L, 3000000L);

        session.persist(post3);
    }

    private static void addPriceHistoryToPost(Post post, Long before, Long after) {
        var priceHistory = new PriceHistory();
        priceHistory.setBefore(before);
        priceHistory.setAfter(after);
        post.getPriceHistories().add(priceHistory);
    }

    private static void displayAllData(CrudRepository crudRepository) {
        crudRepository.tx(session -> {
            displayUsers(session);
            displayEngines(session);
            displayCarsWithOwners(session);
            displayOwners(session);
            displayPostsWithPriceHistory(session);
            return null;
        });
    }

    private static void displayUsers(org.hibernate.Session session) {
        System.out.println("\n=== ПОЛЬЗОВАТЕЛИ ===");
        session.createQuery("FROM User ORDER BY id", User.class)
                .list()
                .forEach(TestDataGenerator::printUserInfo);
    }

    private static void printUserInfo(User user) {
        System.out.printf("ID: %d | Login: %s%n", user.getId(), user.getLogin());
    }

    private static void displayEngines(org.hibernate.Session session) {
        System.out.println("\n=== ДВИГАТЕЛИ ===");
        session.createQuery("FROM Engine ORDER BY id", Engine.class)
                .list()
                .forEach(TestDataGenerator::printEngineInfo);
    }

    private static void printEngineInfo(Engine engine) {
        System.out.printf("ID: %d | %s | %.1fL | %d л.с.%n",
                engine.getId(),
                engine.getName(),
                engine.getVolume(),
                engine.getPower());
    }

    private static void displayCarsWithOwners(org.hibernate.Session session) {
        System.out.println("\n=== МАШИНЫ С ВЛАДЕЛЬЦАМИ ===");
        session.createQuery("SELECT DISTINCT c FROM Car c JOIN FETCH c.engine LEFT JOIN FETCH c.owners ORDER BY c.id", Car.class)
                .list()
                .forEach(TestDataGenerator::printCarInfo);
    }

    private static void printCarInfo(Car car) {
        System.out.printf("ID: %d | %s %s (%d) | Двигатель: %s%n",
                car.getId(),
                car.getName(),
                car.getModel(),
                car.getManufactureYear(),
                car.getEngine().getName());

        if (!car.getOwners().isEmpty()) {
            System.out.println("  Владельцы:");
            car.getOwners().forEach(owner ->
                    System.out.printf("    - %s (User: %s)%n",
                            owner.getName(),
                            owner.getUser().getLogin()));
        } else {
            System.out.println("  Владельцев нет");
        }
        System.out.println();
    }

    private static void displayOwners(org.hibernate.Session session) {
        System.out.println("\n=== ВЛАДЕЛЬЦЫ ===");
        session.createQuery("SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.cars ORDER BY o.id", Owner.class)
                .list()
                .forEach(TestDataGenerator::printOwnerInfo);
    }

    private static void printOwnerInfo(Owner owner) {
        System.out.printf("ID: %d | %s | User: %s%n",
                owner.getId(),
                owner.getName(),
                owner.getUser().getLogin());

        if (!owner.getCars().isEmpty()) {
            System.out.println("  Автомобили:");
            owner.getCars().forEach(car ->
                    System.out.printf("    - %s %s (%d)%n",
                            car.getName(),
                            car.getModel(),
                            car.getManufactureYear()));
        }
        System.out.println();
    }

    private static void displayPostsWithPriceHistory(org.hibernate.Session session) {
        System.out.println("\n=== ОБЪЯВЛЕНИЯ С ИСТОРИЕЙ ЦЕН ===");
        session.createQuery("SELECT DISTINCT p FROM Post p JOIN FETCH p.user JOIN FETCH p.car ORDER BY p.id", Post.class)
                .list()
                .forEach(TestDataGenerator::printPostInfo);
    }

    private static void printPostInfo(Post post) {
        System.out.printf("ID: %d | %s | Автор: %s | Машина: %s %s%n",
                post.getId(),
                post.getDescription(),
                post.getUser().getLogin(),
                post.getCar().getName(),
                post.getCar().getModel());

        // Показываем количество владельцев машины
        int ownerCount = post.getCar().getOwners().size();
        System.out.printf("  Количество владельцев машины: %d%n", ownerCount);

        displayPriceHistory(post);
        System.out.println();
    }

    private static void displayPriceHistory(Post post) {
        if (!post.getPriceHistories().isEmpty()) {
            System.out.println("  История изменений цены:");
            post.getPriceHistories().forEach(TestDataGenerator::printPriceChange);
        }
    }

    private static void printPriceChange(PriceHistory priceHistory) {
        System.out.printf("    - %,d руб. → %,d руб.%n",
                priceHistory.getBefore(), priceHistory.getAfter());
    }
}