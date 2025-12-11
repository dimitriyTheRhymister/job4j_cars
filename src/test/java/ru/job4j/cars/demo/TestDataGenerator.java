package ru.job4j.cars.demo;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.*;
import ru.job4j.cars.repository.CrudRepository;

import java.util.Scanner;
import java.util.Set;

public class TestDataGenerator {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("   🛢  TEST DATA GENERATOR - СОЗДАНИЕ ТЕСТОВЫХ ДАННЫХ");
        System.out.println("=".repeat(60));
        System.out.println();

        // Параметр командной строки для принудительной очистки
        boolean forceClear = args.length > 0 && args[0].equals("--clear");

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();

        try (SessionFactory sf = new MetadataSources(registry)
                .buildMetadata().buildSessionFactory()) {

            var crudRepository = new CrudRepository(sf);
            executeDemo(crudRepository, forceClear);

            System.out.println("\n" + "=".repeat(60));
            System.out.println("   ✅ ТЕСТОВЫЕ ДАННЫЕ СОЗДАНЫ!");
            System.out.println("=".repeat(60));

            // Выводим точную статистику
            displayDatabaseStats(crudRepository);

            System.out.println("\n📋 Дальнейшие действия:");
            System.out.println("   1. 🧪 Запустить RepositoryTestSuite для тестирования");
            System.out.println("   2. 🌐 Запустить H2WebConsoleStarter для просмотра данных");
            System.out.println("\n💡 Совет: Для принудительной очистки базы запустите с параметром --clear");

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static void executeDemo(CrudRepository crudRepository, boolean forceClear) {
        boolean dataCreated = createTestData(crudRepository, forceClear);
        if (dataCreated) {
            displayAllData(crudRepository);
        }
    }

    private static boolean createTestData(CrudRepository crudRepository, boolean forceClear) {
        return crudRepository.tx(session -> {
            if (hasExistingData(session)) {
                System.out.println("⚠  Данные уже существуют в базе!");

                if (forceClear) {
                    System.out.println("   🗑  Принудительная очистка базы (параметр --clear)...");
                    clearDatabase(session);
                    System.out.println("   ✅ База очищена!");
                } else {
                    System.out.println("   Хотите очистить и создать заново? (y/n)");
                    System.out.print("   Ваш выбор: ");

                    Scanner scanner = new Scanner(System.in);
                    String choice = scanner.nextLine().trim().toLowerCase();

                    if (choice.equals("y") || choice.equals("yes") || choice.equals("да")) {
                        System.out.println("   🗑  Очистка базы данных...");
                        clearDatabase(session);
                        System.out.println("   ✅ База очищена!");
                    } else {
                        System.out.println("   ❌ Создание данных пропущено!");
                        return false;
                    }
                }
            }

            System.out.println("\n🔨 Создание тестовых данных...");
            System.out.println("   1. Пользователи...");
            createUsers(session);

            System.out.println("   2. Двигатели...");
            createEngines(session);

            System.out.println("   3. Автомобили...");
            createCars(session);

            System.out.println("   4. Владельцы...");
            createOwners(session);

            System.out.println("   5. Объявления с историей цен...");
            createPostsWithPriceHistory(session);

            System.out.println("   6. Добавление фото к объявлениям...");
            addPhotosToPosts(session);

            System.out.println("✅ Все данные созданы!");
            return true;
        });
    }

    private static void clearDatabase(org.hibernate.Session session) {
        // Важно удалять в правильном порядке из-за foreign keys!
        // Сначала удаляем данные из связующих таблиц

        // Если таблица PARTICIPATES существует (ManyToMany Car-Owner)
        try {
            session.createNativeQuery("DELETE FROM participates").executeUpdate();
            System.out.println("   🗑  Очищена таблица: participates");
        } catch (Exception e) {
            // Таблица может не существовать
        }

        try {
            session.createNativeQuery("DELETE FROM history_owner").executeUpdate();
            System.out.println("   🗑  Очищена таблица: history_owner");
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            session.createNativeQuery("DELETE FROM price_history").executeUpdate();
            System.out.println("   🗑  Очищена таблица: price_history");
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            session.createNativeQuery("DELETE FROM post_photos").executeUpdate();
            System.out.println("   🗑  Очищена таблица: post_photos");
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            session.createNativeQuery("DELETE FROM auto_post").executeUpdate();
            System.out.println("   🗑  Очищена таблица: auto_post");
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            session.createNativeQuery("DELETE FROM owners").executeUpdate();
            System.out.println("   🗑  Очищена таблица: owners");
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            session.createNativeQuery("DELETE FROM cars").executeUpdate();
            System.out.println("   🗑  Очищена таблица: cars");
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            session.createNativeQuery("DELETE FROM engines").executeUpdate();
            System.out.println("   🗑  Очищена таблица: engines");
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        try {
            session.createNativeQuery("DELETE FROM auto_user").executeUpdate();
            System.out.println("   🗑  Очищена таблица: auto_user");
        } catch (Exception e) {
            // Игнорируем ошибки
        }

        // Сбрасываем sequence если используется
        try {
            session.createNativeQuery("ALTER TABLE auto_user ALTER COLUMN id RESTART WITH 1").executeUpdate();
            session.createNativeQuery("ALTER TABLE engines ALTER COLUMN id RESTART WITH 1").executeUpdate();
            session.createNativeQuery("ALTER TABLE cars ALTER COLUMN id RESTART WITH 1").executeUpdate();
            session.createNativeQuery("ALTER TABLE owners ALTER COLUMN id RESTART WITH 1").executeUpdate();
            session.createNativeQuery("ALTER TABLE auto_post ALTER COLUMN id RESTART WITH 1").executeUpdate();
            System.out.println("   🔄 Сброшены sequence ID");
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    private static boolean hasExistingData(org.hibernate.Session session) {
        try {
            Long count = session.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .uniqueResult();
            return count > 0;
        } catch (Exception e) {
            // Если таблицы не существуют
            return false;
        }
    }

    private static void createUsers(org.hibernate.Session session) {
        System.out.print("   ");
        var user1 = new User();
        user1.setLogin("ivanov");
        user1.setPassword("123");
        session.persist(user1);
        System.out.print("ivanov ");

        var user2 = new User();
        user2.setLogin("petrov");
        user2.setPassword("456");
        session.persist(user2);
        System.out.print("petrov ");

        var user3 = new User();
        user3.setLogin("sidorov");
        user3.setPassword("789");
        session.persist(user3);
        System.out.print("sidorov ");

        // Дополнительные пользователи для тестов
        var user4 = new User();
        user4.setLogin("smirnov");
        user4.setPassword("111");
        session.persist(user4);
        System.out.print("smirnov ");

        var user5 = new User();
        user5.setLogin("kuznetsov");
        user5.setPassword("222");
        session.persist(user5);
        System.out.print("kuznetsov ");

        System.out.println();
    }

    private static void createEngines(org.hibernate.Session session) {
        System.out.print("   ");
        var engine1 = new Engine();
        engine1.setName("V6 3.5L");
        engine1.setVolume(3.5);
        engine1.setPower(249);
        session.persist(engine1);
        System.out.print("V6 3.5L ");

        var engine2 = new Engine();
        engine2.setName("I4 2.0L");
        engine2.setVolume(2.0);
        engine2.setPower(150);
        session.persist(engine2);
        System.out.print("I4 2.0L ");

        var engine3 = new Engine();
        engine3.setName("V8 5.7L");
        engine3.setVolume(5.7);
        engine3.setPower(345);
        session.persist(engine3);
        System.out.print("V8 5.7L ");

        // Дополнительные двигатели
        var engine4 = new Engine();
        engine4.setName("I3 1.0L");
        engine4.setVolume(1.0);
        engine4.setPower(75);
        session.persist(engine4);
        System.out.print("I3 1.0L ");

        var engine5 = new Engine();
        engine5.setName("V12 6.0L");
        engine5.setVolume(6.0);
        engine5.setPower(500);
        session.persist(engine5);
        System.out.print("V12 6.0L ");

        System.out.println();
    }

    private static void createCars(org.hibernate.Session session) {
        var engine1 = session.createQuery("FROM Engine WHERE name = 'V6 3.5L'", Engine.class)
                .uniqueResult();
        var engine2 = session.createQuery("FROM Engine WHERE name = 'I4 2.0L'", Engine.class)
                .uniqueResult();
        var engine3 = session.createQuery("FROM Engine WHERE name = 'V8 5.7L'", Engine.class)
                .uniqueResult();
        var engine4 = session.createQuery("FROM Engine WHERE name = 'I3 1.0L'", Engine.class)
                .uniqueResult();
        var engine5 = session.createQuery("FROM Engine WHERE name = 'V12 6.0L'", Engine.class)
                .uniqueResult();

        System.out.print("   ");
        // Основные автомобили
        var car1 = new Car();
        car1.setName("Toyota");
        car1.setModel("Camry");
        car1.setManufactureYear(2015);
        car1.setEngine(engine1);
        session.persist(car1);
        System.out.print("Toyota Camry ");

        var car2 = new Car();
        car2.setName("Honda");
        car2.setModel("Civic");
        car2.setManufactureYear(2018);
        car2.setEngine(engine2);
        session.persist(car2);
        System.out.print("Honda Civic ");

        var car3 = new Car();
        car3.setName("Ford");
        car3.setModel("Mustang");
        car3.setManufactureYear(2020);
        car3.setEngine(engine3);
        session.persist(car3);
        System.out.print("Ford Mustang ");

        // Дополнительные автомобили
        var car4 = new Car();
        car4.setName("BMW");
        car4.setModel("X5");
        car4.setManufactureYear(2022);
        car4.setEngine(engine5);
        session.persist(car4);
        System.out.print("BMW X5 ");

        var car5 = new Car();
        car5.setName("Lada");
        car5.setModel("Vesta");
        car5.setManufactureYear(2020);
        car5.setEngine(engine4);
        session.persist(car5);
        System.out.print("Lada Vesta ");

        var car6 = new Car();
        car6.setName("Mercedes");
        car6.setModel("E-Class");
        car6.setManufactureYear(2021);
        car6.setEngine(engine3);
        session.persist(car6);
        System.out.print("Mercedes E-Class ");

        System.out.println();
    }

    private static void createOwners(org.hibernate.Session session) {
        var user1 = session.createQuery("FROM User WHERE login = 'ivanov'", User.class)
                .uniqueResult();
        var user2 = session.createQuery("FROM User WHERE login = 'petrov'", User.class)
                .uniqueResult();
        var user3 = session.createQuery("FROM User WHERE login = 'sidorov'", User.class)
                .uniqueResult();
        var user4 = session.createQuery("FROM User WHERE login = 'smirnov'", User.class)
                .uniqueResult();
        var user5 = session.createQuery("FROM User WHERE login = 'kuznetsov'", User.class)
                .uniqueResult();

        var car1 = session.createQuery("FROM Car WHERE name = 'Toyota' AND model = 'Camry'", Car.class)
                .uniqueResult();
        var car2 = session.createQuery("FROM Car WHERE name = 'Honda' AND model = 'Civic'", Car.class)
                .uniqueResult();
        var car3 = session.createQuery("FROM Car WHERE name = 'Ford' AND model = 'Mustang'", Car.class)
                .uniqueResult();
        var car4 = session.createQuery("FROM Car WHERE name = 'BMW' AND model = 'X5'", Car.class)
                .uniqueResult();
        var car5 = session.createQuery("FROM Car WHERE name = 'Lada' AND model = 'Vesta'", Car.class)
                .uniqueResult();
        var car6 = session.createQuery("FROM Car WHERE name = 'Mercedes' AND model = 'E-Class'", Car.class)
                .uniqueResult();

        System.out.print("   ");
        // Создаем владельцев
        var owner1 = new Owner();
        owner1.setName("Иванов Иван Иванович");
        owner1.setUser(user1);
        session.persist(owner1);
        System.out.print("Иванов ");

        var owner2 = new Owner();
        owner2.setName("Петров Петр Петрович");
        owner2.setUser(user2);
        session.persist(owner2);
        System.out.print("Петров ");

        var owner3 = new Owner();
        owner3.setName("Сидоров Алексей");
        owner3.setUser(user3);
        session.persist(owner3);
        System.out.print("Сидоров ");

        var owner4 = new Owner();
        owner4.setName("Смирнов Дмитрий");
        owner4.setUser(user4);
        session.persist(owner4);
        System.out.print("Смирнов ");

        var owner5 = new Owner();
        owner5.setName("Кузнецов Андрей");
        owner5.setUser(user5);
        session.persist(owner5);
        System.out.print("Кузнецов ");

        System.out.println();

        System.out.print("   Установка связей автомобилей с владельцами... ");

        // Устанавливаем связи ManyToMany между Car и Owner
        car1.setOwners(Set.of(owner1, owner2));     // Toyota - 2 владельца
        car2.setOwners(Set.of(owner2, owner3));     // Honda - 2 владельца
        car3.setOwners(Set.of(owner3, owner1));     // Ford - 2 владельца
        car4.setOwners(Set.of(owner4));             // BMW - 1 владелец
        car5.setOwners(Set.of(owner5, owner1));     // Lada - 2 владельца
        car6.setOwners(Set.of(owner2, owner4));     // Mercedes - 2 владельца

        session.update(car1);
        session.update(car2);
        session.update(car3);
        session.update(car4);
        session.update(car5);
        session.update(car6);

        System.out.println("✅");
    }

    private static void createPostsWithPriceHistory(org.hibernate.Session session) {
        System.out.print("   ");
        createFirstPost(session);
        System.out.print("Toyota ");

        createSecondPost(session);
        System.out.print("Honda ");

        createThirdPost(session);
        System.out.print("Ford ");

        createFourthPost(session);
        System.out.print("BMW ");

        createFifthPost(session);
        System.out.print("Lada ");

        System.out.println();
    }

    private static void createFirstPost(org.hibernate.Session session) {
        var user1 = session.createQuery("FROM User WHERE login = 'ivanov'", User.class)
                .uniqueResult();
        var car1 = session.createQuery("FROM Car WHERE name = 'Toyota' AND model = 'Camry'", Car.class)
                .uniqueResult();

        var post1 = new Post();
        post1.setDescription("Продам Toyota Camry 2015 года в отличном состоянии. Полная сервисная история, один владелец, пробег 85 000 км.");
        post1.setUser(user1);
        post1.setCar(car1);
        post1.setCurrentPrice(1400000L);

        addPriceHistoryToPost(post1, 1500000L, 1450000L);
        addPriceHistoryToPost(post1, 1450000L, 1400000L);

        session.persist(post1);
    }

    private static void createSecondPost(org.hibernate.Session session) {
        var user2 = session.createQuery("FROM User WHERE login = 'petrov'", User.class)
                .uniqueResult();
        var car2 = session.createQuery("FROM Car WHERE name = 'Honda' AND model = 'Civic'", Car.class)
                .uniqueResult();

        var post2 = new Post();
        post2.setDescription("Продам Honda Civic 2018, полный комплект, сервисная история, экономичный расход 6.5л/100км.");
        post2.setUser(user2);
        post2.setCar(car2);
        post2.setCurrentPrice(1000000L);

        addPriceHistoryToPost(post2, 1200000L, 1100000L);
        addPriceHistoryToPost(post2, 1100000L, 1000000L);

        session.persist(post2);
    }

    private static void createThirdPost(org.hibernate.Session session) {
        var user3 = session.createQuery("FROM User WHERE login = 'sidorov'", User.class)
                .uniqueResult();
        var car3 = session.createQuery("FROM Car WHERE name = 'Ford' AND model = 'Mustang'", Car.class)
                .uniqueResult();

        var post3 = new Post();
        post3.setDescription("Ford Mustang 2020, спорткар, два владельца, премиум комплектация, оригинальный V8 двигатель.");
        post3.setUser(user3);
        post3.setCar(car3);
        post3.setCurrentPrice(3000000L);

        addPriceHistoryToPost(post3, 3500000L, 3200000L);
        addPriceHistoryToPost(post3, 3200000L, 3000000L);

        session.persist(post3);
    }

    private static void createFourthPost(org.hibernate.Session session) {
        var user4 = session.createQuery("FROM User WHERE login = 'smirnov'", User.class)
                .uniqueResult();
        var car4 = session.createQuery("FROM Car WHERE name = 'BMW' AND model = 'X5'", Car.class)
                .uniqueResult();

        var post4 = new Post();
        post4.setDescription("BMW X5 2022 года, премиум комплектация M Sport, полный привод, гарантия дилера до 2025 года.");
        post4.setUser(user4);
        post4.setCar(car4);
        post4.setCurrentPrice(5500000L);

        addPriceHistoryToPost(post4, 6000000L, 5500000L);

        session.persist(post4);
    }

    private static void createFifthPost(org.hibernate.Session session) {
        var user5 = session.createQuery("FROM User WHERE login = 'kuznetsov'", User.class)
                .uniqueResult();
        var car5 = session.createQuery("FROM Car WHERE name = 'Lada' AND model = 'Vesta'", Car.class)
                .uniqueResult();

        var post5 = new Post();
        post5.setDescription("Lada Vesta 2020 года, экономичный расход, идеально для города, новый аккумулятор, не битая.");
        post5.setUser(user5);
        post5.setCar(car5);
        post5.setCurrentPrice(800000L);

        addPriceHistoryToPost(post5, 900000L, 850000L);
        addPriceHistoryToPost(post5, 850000L, 800000L);

        session.persist(post5);
    }

    private static void addPriceHistoryToPost(Post post, Long before, Long after) {
        var priceHistory = new PriceHistory();
        priceHistory.setBefore(before);
        priceHistory.setAfter(after);
        post.getPriceHistories().add(priceHistory);
    }

    private static void addPhotosToPosts(org.hibernate.Session session) {
        System.out.print("   Добавление фото: ");
        // Добавляем фото к объявлениям (лайзи коллекция)
        var post1 = session.createQuery("FROM Post WHERE id = 1", Post.class).uniqueResult();
        if (post1 != null) {
            post1.addPhoto("/photos/toyota_camry_front.jpg");
            post1.addPhoto("/photos/toyota_camry_side.jpg");
            post1.addPhoto("/photos/toyota_camry_interior.jpg");
            System.out.print("Toyota(3) ");
        }

        var post2 = session.createQuery("FROM Post WHERE id = 2", Post.class).uniqueResult();
        if (post2 != null) {
            post2.addPhoto("/photos/honda_civic_front.jpg");
            post2.addPhoto("/photos/honda_civic_rear.jpg");
            System.out.print("Honda(2) ");
        }

        var post3 = session.createQuery("FROM Post WHERE id = 3", Post.class).uniqueResult();
        if (post3 != null) {
            post3.addPhoto("/photos/ford_mustang.jpg");
            System.out.print("Ford(1) ");
        }

        var post4 = session.createQuery("FROM Post WHERE id = 4", Post.class).uniqueResult();
        if (post4 != null) {
            post4.addPhoto("/photos/bmw_x5_front.jpg");
            post4.addPhoto("/photos/bmw_x5_interior.jpg");
            post4.addPhoto("/photos/bmw_x5_engine.jpg");
            post4.addPhoto("/photos/bmw_x5_rear.jpg");
            System.out.print("BMW(4) ");
        }

        // post5 без фото специально - для тестирования объявлений без фото
        System.out.print("Lada(0) ");
        System.out.println();
    }

    private static void displayAllData(CrudRepository crudRepository) {
        System.out.println("\n📊 ОБЗОР СОЗДАННЫХ ДАННЫХ:");
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
        System.out.println("\n👥 ПОЛЬЗОВАТЕЛИ (всего " +
                session.createQuery("SELECT COUNT(u) FROM User u", Long.class).uniqueResult() + "):");
        session.createQuery("FROM User ORDER BY id", User.class)
                .list()
                .forEach(TestDataGenerator::printUserInfo);
    }

    private static void printUserInfo(User user) {
        System.out.printf("   ID: %d | Login: %s%n", user.getId(), user.getLogin());
    }

    private static void displayEngines(org.hibernate.Session session) {
        System.out.println("\n⚙️  ДВИГАТЕЛИ (всего " +
                session.createQuery("SELECT COUNT(e) FROM Engine e", Long.class).uniqueResult() + "):");
        session.createQuery("FROM Engine ORDER BY id", Engine.class)
                .list()
                .forEach(TestDataGenerator::printEngineInfo);
    }

    private static void printEngineInfo(Engine engine) {
        System.out.printf("   ID: %d | %s | %.1fL | %d л.с.%n",
                engine.getId(),
                engine.getName(),
                engine.getVolume(),
                engine.getPower());
    }

    private static void displayCarsWithOwners(org.hibernate.Session session) {
        System.out.println("\n🚗 АВТОМОБИЛИ С ВЛАДЕЛЬЦАМИ (всего " +
                session.createQuery("SELECT COUNT(c) FROM Car c", Long.class).uniqueResult() + "):");
        session.createQuery("SELECT DISTINCT c FROM Car c JOIN FETCH c.engine LEFT JOIN FETCH c.owners ORDER BY c.id", Car.class)
                .list()
                .forEach(TestDataGenerator::printCarInfo);
    }

    private static void printCarInfo(Car car) {
        System.out.printf("   ID: %d | %s %s (%d) | Двигатель: %s%n",
                car.getId(),
                car.getName(),
                car.getModel(),
                car.getManufactureYear(),
                car.getEngine().getName());

        int ownerCount = car.getOwners() != null ? car.getOwners().size() : 0;
        System.out.printf("   👤 Владельцев: %d%n", ownerCount);
        System.out.println();
    }

    private static void displayOwners(org.hibernate.Session session) {
        System.out.println("\n👤 ВЛАДЕЛЬЦЫ (всего " +
                session.createQuery("SELECT COUNT(o) FROM Owner o", Long.class).uniqueResult() + "):");
        session.createQuery("SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.cars ORDER BY o.id", Owner.class)
                .list()
                .forEach(TestDataGenerator::printOwnerInfo);
    }

    private static void printOwnerInfo(Owner owner) {
        System.out.printf("   ID: %d | %s | User: %s%n",
                owner.getId(),
                owner.getName(),
                owner.getUser().getLogin());

        int carCount = owner.getCars() != null ? owner.getCars().size() : 0;
        System.out.printf("   🚗 Автомобилей: %d%n", carCount);
        System.out.println();
    }

    private static void displayPostsWithPriceHistory(org.hibernate.Session session) {
        System.out.println("\n📝 ОБЪЯВЛЕНИЯ (всего " +
                session.createQuery("SELECT COUNT(p) FROM Post p", Long.class).uniqueResult() + "):");
        // Используем JOIN FETCH для фото (только здесь, где они нужны)
        session.createQuery(
                        "SELECT DISTINCT p FROM Post p " +
                                "JOIN FETCH p.user " +
                                "JOIN FETCH p.car c " +
                                "JOIN FETCH c.engine " +
                                "LEFT JOIN FETCH p.photoUrls " +  // ✅ Загружаем фото явно для отображения
                                "ORDER BY p.id", Post.class)
                .list()
                .forEach(TestDataGenerator::printPostInfo);
    }

    private static void printPostInfo(Post post) {
        System.out.printf("   ID: %d | %s...%n",
                post.getId(),
                post.getDescription().substring(0, Math.min(50, post.getDescription().length())));
        System.out.printf("   👤 Автор: %s | 🚗 %s %s (%d)%n",
                post.getUser().getLogin(),
                post.getCar().getName(),
                post.getCar().getModel(),
                post.getCar().getManufactureYear());
        System.out.printf("   💰 Текущая цена: %,d руб. | 📸 Фото: %d%n",
                post.getCurrentPrice() != null ? post.getCurrentPrice() : 0,
                post.getPhotoUrls() != null ? post.getPhotoUrls().size() : 0);

        int priceHistoryCount = post.getPriceHistories() != null ? post.getPriceHistories().size() : 0;
        System.out.printf("   📈 История цен: %d изменений%n", priceHistoryCount);
        System.out.println();
    }

    private static void displayDatabaseStats(CrudRepository crudRepository) {
        crudRepository.tx(session -> {
            System.out.println("\n📊 СТАТИСТИКА БАЗЫ ДАННЫХ (точные значения):");

            String[][] queries = {
                    {"AUTO_USER", "SELECT COUNT(*) as cnt FROM auto_user"},
                    {"ENGINES", "SELECT COUNT(*) as cnt FROM engines"},
                    {"CARS", "SELECT COUNT(*) as cnt FROM cars"},
                    {"OWNERS", "SELECT COUNT(*) as cnt FROM owners"},
                    {"HISTORY_OWNER", "SELECT COUNT(*) as cnt FROM history_owner"},
                    {"AUTO_POST", "SELECT COUNT(*) as cnt FROM auto_post"},
                    {"PRICE_HISTORY", "SELECT COUNT(*) as cnt FROM price_history"},
                    {"POST_PHOTOS", "SELECT COUNT(*) as cnt FROM post_photos"}
            };

            int total = 0;
            for (String[] query : queries) {
                String tableName = query[0];
                String sql = query[1];
                try {
                    Long count = ((Number) session.createNativeQuery(sql).getSingleResult()).longValue();
                    System.out.printf("   %-20s: %4d строк%n", tableName, count);
                    total += count != null ? count : 0;
                } catch (Exception e) {
                    System.out.printf("   %-20s: таблица не найдена%n", tableName);
                }
            }

            System.out.println("   " + "-".repeat(40));
            System.out.printf("   %-20s: %4d строк всего%n", "ВСЕГО", total);

            // Проверка таблицы PARTICIPATES если она есть
            try {
                Long participatesCount = (Long) session.createNativeQuery("SELECT COUNT(*) FROM participates")
                        .uniqueResult();
                if (participatesCount != null && participatesCount > 0) {
                    System.out.printf("   %-20s: %4d связей%n", "PARTICIPATES", participatesCount);
                }
            } catch (Exception e) {
                // Таблица может не существовать
            }

            return null;
        });
    }
}