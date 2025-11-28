package ru.job4j.cars.demo;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.*;
import ru.job4j.cars.repository.CrudRepository;

public class H2DatabaseDemo {

    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();

        try (SessionFactory sf = new MetadataSources(registry)
                .buildMetadata().buildSessionFactory()) {

            var crudRepository = new CrudRepository(sf);
            executeDemo(crudRepository);

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
            createCars(session);
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
    }

    private static void createCars(org.hibernate.Session session) {
        var engine = new Engine();
        engine.setName("V6");
        engine.setVolume(3.5);
        engine.setPower(249);
        session.persist(engine);

        var car1 = new Car();
        car1.setName("Toyota");
        car1.setModel("Camry");
        car1.setManufactureYear(2015);
        car1.setEngine(engine);
        session.persist(car1);

        var car2 = new Car();
        car2.setName("Honda");
        car2.setModel("Civic");
        car2.setManufactureYear(2018);
        car2.setEngine(engine);
        session.persist(car2);
    }

    private static void createPostsWithPriceHistory(org.hibernate.Session session) {
        createFirstPost(session);
        createSecondPost(session);
    }

    private static void createFirstPost(org.hibernate.Session session) {
        var user1 = session.createQuery("FROM User WHERE login = 'ivanov'", User.class)
                .uniqueResult();

        var car1 = session.createQuery("FROM Car WHERE name = 'Toyota'", Car.class)
                .uniqueResult();

        var post1 = new Post();
        post1.setDescription("Продам Toyota Camry 2015 года");
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
        post2.setDescription("Продам Honda Civic 2018");
        post2.setUser(user2);
        post2.setCar(car2);

        addPriceHistoryToPost(post2, 1200000L, 1100000L);
        addPriceHistoryToPost(post2, 1100000L, 1000000L);

        session.persist(post2);
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
            displayCars(session);
            displayPostsWithPriceHistory(session);
            return null;
        });
    }

    private static void displayUsers(org.hibernate.Session session) {
        System.out.println("\n=== ПОЛЬЗОВАТЕЛИ ===");
        session.createQuery("FROM User ORDER BY id", User.class)
                .list()
                .forEach(H2DatabaseDemo::printUserInfo);
    }

    private static void printUserInfo(User user) {
        System.out.printf("ID: %d | Login: %s%n", user.getId(), user.getLogin());
    }

    private static void displayCars(org.hibernate.Session session) {
        System.out.println("\n=== МАШИНЫ ===");
        session.createQuery("FROM Car c JOIN FETCH c.engine ORDER BY c.id", Car.class)
                .list()
                .forEach(H2DatabaseDemo::printCarInfo);
    }

    private static void printCarInfo(Car car) {
        System.out.printf("ID: %d | %s %s (%d) | Двигатель: %s %.1fL %d л.с.%n",
                car.getId(),
                car.getName(),
                car.getModel(),
                car.getManufactureYear(),
                car.getEngine().getName(),
                car.getEngine().getVolume(),
                car.getEngine().getPower());
    }

    private static void displayPostsWithPriceHistory(org.hibernate.Session session) {
        System.out.println("\n=== ПОСТЫ С ИСТОРИЕЙ ЦЕН ===");
        session.createQuery("FROM Post p JOIN FETCH p.user JOIN FETCH p.car ORDER BY p.id", Post.class)
                .list()
                .forEach(H2DatabaseDemo::printPostInfo);
    }

    private static void printPostInfo(Post post) {
        System.out.printf("ID: %d | %s | Автор: %s | Машина: %s %s (%d)%n",
                post.getId(),
                post.getDescription(),
                post.getUser().getLogin(),
                post.getCar().getName(),
                post.getCar().getModel(),
                post.getCar().getManufactureYear());

        displayPriceHistory(post);
        System.out.println();
    }

    private static void displayPriceHistory(Post post) {
        if (!post.getPriceHistories().isEmpty()) {
            System.out.println("  История изменений цены:");
            post.getPriceHistories().forEach(H2DatabaseDemo::printPriceChange);
        }
    }

    private static void printPriceChange(PriceHistory priceHistory) {
        System.out.printf("    - %,d руб. → %,d руб.%n",
                priceHistory.getBefore(), priceHistory.getAfter());
    }
}