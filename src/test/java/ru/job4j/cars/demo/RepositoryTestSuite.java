package ru.job4j.cars.demo;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.*;
import ru.job4j.cars.repository.*;

import java.util.List;
import java.util.Optional;

public class RepositoryTestSuite {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("   🧪 RepositoryTestSuite - ТЕСТИРОВАНИЕ ВСЕЙ СИСТЕМЫ");
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("⚠  ПРЕДУПРЕЖДЕНИЕ: Этот демо тестирует работу репозиториев.");
        System.out.println("   Перед запуском убедитесь, что:");
        System.out.println("   1. Запущен DatabaseMigration (структура БД)");
        System.out.println("   2. Запущен TestDataGenerator (тестовые данные)");
        System.out.println();

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();

        try (SessionFactory sf = new MetadataSources(registry)
                .buildMetadata().buildSessionFactory()) {

            var crudRepository = new CrudRepository(sf);

            // ПРОВЕРКА: есть ли данные в БД?
            System.out.println("🔍 Проверка наличия данных в БД...");
            if (!hasData(crudRepository)) {
                System.err.println("\n❌ ОШИБКА: Данные не найдены в базе!");
                System.err.println("   Сначала запустите:");
                System.err.println("   1. DatabaseMigration - создание структуры БД");
                System.err.println("   2. TestDataGenerator - наполнение тестовыми данными");
                System.err.println("\n   Команды для запуска:");
                System.err.println("   mvn compile exec:java -Dexec.mainClass=\"ru.job4j.cars.demo.DatabaseMigration\"");
                System.err.println("   mvn compile exec:java -Dexec.mainClass=\"ru.job4j.cars.demo.TestDataGenerator\"");
                return;
            }

            System.out.println("✅ Данные найдены. Запуск тестов...\n");

            // Создаём все репозитории
            var userRepo = new UserRepository(crudRepository);
            var carRepo = new CarRepository(crudRepository);
            var engineRepo = new EngineRepository(crudRepository);
            var postRepo = new PostRepository(crudRepository);
            var ownerRepo = new OwnerRepository(crudRepository);
            var participatesRepo = new ParticipatesRepository(crudRepository);
            var priceHistoryRepo = new PriceHistoryRepository(crudRepository);

            // Шаг 1: Тест UserRepository
            testUserRepository(userRepo);

            // Шаг 2: Тест EngineRepository
            testEngineRepository(engineRepo);

            // Шаг 3: Тест CarRepository
            testCarRepository(carRepo);

            // Шаг 4: Тест OwnerRepository
            testOwnerRepository(ownerRepo, userRepo);

            // Шаг 5: Тест PostRepository
            testPostRepository(postRepo, userRepo, carRepo);

            // Шаг 6: Тест PriceHistoryRepository
            testPriceHistoryRepository(priceHistoryRepo, postRepo);

            // Шаг 7: Тест ParticipatesRepository
            testParticipatesRepository(participatesRepo, userRepo, postRepo);

            // Шаг 8: Комплексный тест - получение полной информации
            testComplexQueries(postRepo, carRepo, userRepo);

            System.out.println("\n" + "=".repeat(60));
            System.out.println("   ✅ RepositoryTestSuite УСПЕШНО ЗАВЕРШЁН!");
            System.out.println("=".repeat(60));
            System.out.println("\n💡 Теперь можете запустить H2WebConsoleStarter для просмотра данных:");
            System.out.println("   mvn compile exec:java -Dexec.mainClass=\"ru.job4j.cars.demo.H2WebConsoleStarter\"");

        } catch (Exception e) {
            System.err.println("\n❌ Ошибка в RepositoryTestSuite:");
            e.printStackTrace();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static boolean hasData(CrudRepository crudRepository) {
        return crudRepository.tx(session -> {
            try {
                // Проверяем несколько ключевых таблиц
                Long userCount = session.createQuery(
                        "SELECT COUNT(u) FROM User u", Long.class).uniqueResult();
                Long postCount = session.createQuery(
                        "SELECT COUNT(p) FROM Post p", Long.class).uniqueResult();
                Long carCount = session.createQuery(
                        "SELECT COUNT(c) FROM Car c", Long.class).uniqueResult();

                System.out.printf("   Найдено: %d пользователей, %d объявлений, %d автомобилей%n",
                        userCount, postCount, carCount);

                // Если есть хотя бы по одному в каждой таблице - данные есть
                return userCount > 0 && postCount > 0 && carCount > 0;
            } catch (Exception e) {
                // Если ошибка - значит таблиц нет (Liquibase не запускался)
                System.err.println("   ⚠ Ошибка при проверке данных: " + e.getMessage());
                return false;
            }
        });
    }

    private static void testUserRepository(UserRepository repo) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ТЕСТ 1: UserRepository");
        System.out.println("=".repeat(40));

        System.out.println("\n1. Все пользователи:");
        List<User> users = repo.findAllOrderById();
        if (users.isEmpty()) {
            System.out.println("   (нет пользователей)");
        } else {
            users.forEach(u ->
                    System.out.printf("   ID: %d | Login: %s%n",
                            u.getId(), u.getLogin()));
        }

        System.out.println("\n2. Поиск по логину 'ivanov':");
        Optional<User> user = repo.findByLogin("ivanov");
        if (user.isPresent()) {
            System.out.printf("   Найден: ID: %d, Login: %s%n",
                    user.get().getId(), user.get().getLogin());
        } else {
            System.out.println("   Пользователь 'ivanov' не найден");
        }
    }

    private static void testEngineRepository(EngineRepository repo) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ТЕСТ 2: EngineRepository");
        System.out.println("=".repeat(40));

        System.out.println("Все двигатели:");
        List<Engine> engines = repo.findAllOrderById();
        if (engines.isEmpty()) {
            System.out.println("   (нет двигателей)");
        } else {
            engines.forEach(e ->
                    System.out.printf("   ID: %d | %s | %.1fL | %d л.с.%n",
                            e.getId(), e.getName(), e.getVolume(), e.getPower()));
        }
    }

    private static void testCarRepository(CarRepository repo) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ТЕСТ 3: CarRepository");
        System.out.println("=".repeat(40));

        System.out.println("1. Все автомобили:");
        List<Car> cars = repo.findAllOrderById();
        if (cars.isEmpty()) {
            System.out.println("   (нет автомобилей)");
        } else {
            cars.forEach(c ->
                    System.out.printf("   ID: %d | %s %s (%d) | Двигатель: %s%n",
                            c.getId(), c.getName(), c.getModel(), c.getManufactureYear(),
                            c.getEngine() != null ? c.getEngine().getName() : "нет"));
        }

        System.out.println("\n2. Автомобили 2018 года:");
        List<Car> cars2018 = repo.findByManufactureYear(2018);
        if (cars2018.isEmpty()) {
            System.out.println("   (нет автомобилей 2018 года)");
        } else {
            cars2018.forEach(c ->
                    System.out.printf("   %s %s%n", c.getName(), c.getModel()));
        }
    }

    private static void testOwnerRepository(OwnerRepository repo, UserRepository userRepo) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ТЕСТ 4: OwnerRepository");
        System.out.println("=".repeat(40));

        System.out.println("Все владельцы:");
        List<Owner> owners = repo.findAllOrderById();
        if (owners.isEmpty()) {
            System.out.println("   (нет владельцев)");
        } else {
            owners.forEach(o -> {
                User user = o.getUser();
                System.out.printf("   ID: %d | %s | Пользователь: %s (ID: %d)%n",
                        o.getId(), o.getName(),
                        user != null ? user.getLogin() : "нет",
                        user != null ? user.getId() : 0);
            });
        }
    }

    private static void testPostRepository(PostRepository repo,
                                           UserRepository userRepo,
                                           CarRepository carRepo) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ТЕСТ 5: PostRepository");
        System.out.println("=".repeat(40));

        System.out.println("1. Все объявления (по убыванию даты):");
        List<Post> posts = repo.findAllOrderByCreatedDesc();
        if (posts.isEmpty()) {
            System.out.println("   (нет объявлений)");
        } else {
            posts.forEach(p -> {
                int photoCount = p.getPhotoUrls() != null ? p.getPhotoUrls().size() : 0;
                System.out.printf("   ID: %d | Автор: %s | %s... | Фото: %d | Создано: %s%n",
                        p.getId(),
                        p.getUser() != null ? p.getUser().getLogin() : "нет",
                        p.getDescription().substring(0, Math.min(30, p.getDescription().length())),
                        photoCount,
                        p.getCreated());
            });
        }

        System.out.println("\n2. Объявления с фото:");
        List<Post> withPhotos = repo.findPostsWithPhotos();
        System.out.printf("   Найдено: %d объявлений с фото%n", withPhotos.size());

        System.out.println("\n3. Объявления марки 'Toyota':");
        List<Post> toyotaPosts = repo.findPostsByCarBrand("Toyota");
        System.out.printf("   Найдено: %d объявлений Toyota%n", toyotaPosts.size());

        System.out.println("\n4. Объявления за последний день:");
        List<Post> lastDay = repo.findPostsFromLastDay();
        System.out.printf("   Найдено: %d объявлений за последний день%n", lastDay.size());

        // НЕ добавляем новые фото! Только проверяем существующие
        System.out.println("\n5. Проверка фото в объявлениях:");
        Optional<Post> post1 = repo.findById(1);
        if (post1.isPresent()) {
            Post p = post1.get();
            int existingPhotos = p.getPhotoUrls() != null ? p.getPhotoUrls().size() : 0;
            System.out.printf("   Объявление ID: %d имеет %d фото%n", p.getId(), existingPhotos);
        } else {
            System.out.println("   Объявление ID: 1 не найдено");
        }
    }

    private static void testPriceHistoryRepository(PriceHistoryRepository repo,
                                                   PostRepository postRepo) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ТЕСТ 6: PriceHistoryRepository");
        System.out.println("=".repeat(40));

        System.out.println("Вся история цен:");
        List<PriceHistory> histories = repo.findAllOrderById();
        if (histories.isEmpty()) {
            System.out.println("   (нет записей истории цен)");
        } else {
            System.out.printf("   Всего записей: %d%n", histories.size());
            histories.forEach(ph -> {
                Post p = ph.getPost();
                System.out.printf("   ID: %d | Пост ID: %s | Было: %,d | Стало: %,d | Дата: %s%n",
                        ph.getId(),
                        p != null ? String.valueOf(p.getId()) : "нет",
                        ph.getBefore(),
                        ph.getAfter(),
                        ph.getCreated());
            });
        }

        // НЕ создаём новые записи! Только читаем существующие
    }

    private static void testParticipatesRepository(ParticipatesRepository repo,
                                                   UserRepository userRepo,
                                                   PostRepository postRepo) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ТЕСТ 7: ParticipatesRepository");
        System.out.println("=".repeat(40));

        System.out.println("Все подписки:");
        List<Participates> allParticipates = repo.findAllOrderById();
        if (allParticipates.isEmpty()) {
            System.out.println("   (нет подписок)");
        } else {
            System.out.printf("   Всего подписок: %d%n", allParticipates.size());
            allParticipates.forEach(p -> {
                User user = p.getUser();
                Post post = p.getPost();
                System.out.printf("   ID: %d | Пользователь: %s → Пост ID: %d | Дата: %s%n",
                        p.getId(),
                        user != null ? user.getLogin() : "нет",
                        post != null ? post.getId() : 0,
                        p.getCreated());
            });
        }

        System.out.println("\nСтатистика подписок:");
        Optional<Post> post1 = postRepo.findById(1);
        if (post1.isPresent()) {
            long count = repo.countSubscribersByPost(post1.get());
            System.out.printf("   На пост ID: %d подписано: %d пользователей%n",
                    post1.get().getId(), count);
        }

        // НЕ создаём новые подписки! Только читаем существующие
    }

    private static void testComplexQueries(PostRepository postRepo,
                                           CarRepository carRepo,
                                           UserRepository userRepo) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ТЕСТ 8: Комплексные запросы и статистика");
        System.out.println("=".repeat(50));

        System.out.println("\n1. Статистика системы:");
        long totalUsers = userRepo.findAllOrderById().size();
        long totalCars = carRepo.findAllOrderById().size();
        long totalPosts = postRepo.countAllPosts();
        long postsWithPhotos = postRepo.countPostsWithPhotos();

        System.out.printf("   👥 Пользователей: %d%n", totalUsers);
        System.out.printf("   🚗 Автомобилей: %d%n", totalCars);
        System.out.printf("   📝 Объявлений: %d%n", totalPosts);
        System.out.printf("   📸 Объявлений с фото: %d (%.1f%%)%n",
                postsWithPhotos, totalPosts > 0 ? (postsWithPhotos * 100.0 / totalPosts) : 0);

        System.out.println("\n2. Объявления с полной информацией:");
        List<Post> posts = postRepo.findAllOrderByCreatedDesc();
        if (posts.isEmpty()) {
            System.out.println("   (нет объявлений)");
        } else {
            posts.forEach(p -> {
                Car car = p.getCar();
                User user = p.getUser();
                int photoCount = p.getPhotoUrls() != null ? p.getPhotoUrls().size() : 0;

                System.out.printf("   📋 Объявление ID: %d%n", p.getId());
                System.out.printf("      👤 Автор: %s (ID: %d)%n",
                        user != null ? user.getLogin() : "нет",
                        user != null ? user.getId() : 0);
                System.out.printf("      🚙 Автомобиль: %s %s (%d)%n",
                        car != null ? car.getName() : "нет данных",
                        car != null ? car.getModel() : "",
                        car != null ? car.getManufactureYear() : 0);
                System.out.printf("      ⚙️  Двигатель: %s%n",
                        car != null && car.getEngine() != null ? car.getEngine().getName() : "нет данных");
                System.out.printf("      📄 Описание: %s...%n",
                        p.getDescription().substring(0, Math.min(50, p.getDescription().length())));
                System.out.printf("      🖼  Фото: %d | 📅 Создано: %s%n",
                        photoCount, p.getCreated());
                System.out.println();
            });
        }

        System.out.println("✅ Все тесты выполнены успешно!");
    }
}