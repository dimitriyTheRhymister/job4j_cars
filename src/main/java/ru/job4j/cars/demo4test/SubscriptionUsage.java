package ru.job4j.cars.demo4test;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.*;
import ru.job4j.cars.repository.CrudRepository;
import ru.job4j.cars.repository.ParticipatesRepository;
import ru.job4j.cars.repository.UserRepository;

import java.util.List;
import java.util.Optional;

    public class SubscriptionUsage {

        public static void main(String[] args) {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure().build();

            try (SessionFactory sf = new MetadataSources(registry)
                    .buildMetadata().buildSessionFactory()) {

                executeDemo(sf);

            } finally {
                StandardServiceRegistryBuilder.destroy(registry);
            }
        }

        private static void executeDemo(SessionFactory sf) {
            var crudRepository = new CrudRepository(sf);
            var userRepository = new UserRepository(crudRepository);
            var participatesRepository = new ParticipatesRepository(crudRepository);

            List<User> users = createTestUsers(userRepository);
            Post post = createTestPost(crudRepository, users.get(0));

            setupSubscriptions(participatesRepository, users, post);
            demonstrateRepositoryMethods(participatesRepository, users, post);
            demonstrateUnsubscribe(participatesRepository, users, post);
        }

        private static List<User> createTestUsers(UserRepository userRepository) {
            var user1 = new User();
            user1.setLogin("user1");
            user1.setPassword("pass1");
            userRepository.create(user1);

            var user2 = new User();
            user2.setLogin("user2");
            user2.setPassword("pass2");
            userRepository.create(user2);

            var user3 = new User();
            user3.setLogin("user3");
            user3.setPassword("pass3");
            userRepository.create(user3);

            return List.of(user1, user2, user3);
        }

        private static Post createTestPost(CrudRepository crudRepository,
                                           User author) {
            var post = new Post();
            post.setDescription("Продам Toyota Camry");
            post.setUser(author);
            post.setCurrentPrice(1500000L);

            crudRepository.run(session -> session.persist(post));
            return post;
        }

        private static void setupSubscriptions(ParticipatesRepository participatesRepository,
                                               List<User> users,
                                               Post post) {
            participatesRepository.subscribe(users.get(1), post);
            participatesRepository.subscribe(users.get(2), post);
        }

        private static void demonstrateRepositoryMethods(ParticipatesRepository participatesRepository,
                                                         List<User> users,
                                                         Post post) {
            displayAllSubscriptions(participatesRepository);
            displayPostSubscribers(participatesRepository, post);
            displayUserSubscriptions(participatesRepository, users.get(1));
            checkSubscriptionStatus(participatesRepository, users.get(1), post);
            findSpecificSubscription(participatesRepository, users.get(1), post);
            displayStatistics(participatesRepository, users, post);
        }

        private static void displayAllSubscriptions(ParticipatesRepository participatesRepository) {
            System.out.println("=== ВСЕ ПОДПИСКИ ===");
            List<Participates> allSubscriptions = participatesRepository.findAllOrderById();
            allSubscriptions.forEach(SubscriptionUsage::printSubscriptionInfo);
        }

        private static void printSubscriptionInfo(Participates subscription) {
            System.out.printf("Подписка ID: %d | Пользователь: %s | Объявление: %s%n",
                    subscription.getId(),
                    subscription.getUser().getLogin(),
                    subscription.getPost().getDescription());
        }

        private static void displayPostSubscribers(ParticipatesRepository participatesRepository,
                                                   Post post) {
            System.out.println("\n=== ПОДПИСЧИКИ ОБЪЯВЛЕНИЯ ===");
            List<User> subscribers = participatesRepository.findSubscribersByPost(post);
            subscribers.forEach(SubscriptionUsage::printSubscriberInfo);
        }

        private static void printSubscriberInfo(User subscriber) {
            System.out.println("Подписчик: " + subscriber.getLogin());
        }

        private static void displayUserSubscriptions(ParticipatesRepository participatesRepository,
                                                     User user) {
            System.out.println("\n=== ПОДПИСКИ ПОЛЬЗОВАТЕЛЯ " + user.getLogin() + " ===");
            List<Post> userSubscriptions = participatesRepository.findSubscriptionsByUser(user);
            userSubscriptions.forEach(SubscriptionUsage::printPostSubscriptionInfo);
        }

        private static void printPostSubscriptionInfo(Post post) {
            System.out.println("Подписан на: " + post.getDescription());
        }

        private static void checkSubscriptionStatus(ParticipatesRepository participatesRepository,
                                                    User user,
                                                    Post post) {
            boolean isSubscribed = participatesRepository.isSubscribed(user, post);
            System.out.println("\n" + user.getLogin() + " подписан на объявление: " + isSubscribed);
        }

        private static void findSpecificSubscription(ParticipatesRepository participatesRepository,
                                                     User user,
                                                     Post post) {
            Optional<Participates> foundSubscription = participatesRepository.findByUserAndPost(user, post);
            foundSubscription.ifPresent(sub ->
                    System.out.println("Найдена подписка ID: " + sub.getId())
            );
        }

        private static void displayStatistics(ParticipatesRepository participatesRepository,
                                              List<User> users,
                                              Post post) {
            long subscribersCount = participatesRepository.countSubscribersByPost(post);
            long userSubscriptionsCount = participatesRepository.countSubscriptionsByUser(users.get(1));

            System.out.println("\n=== СТАТИСТИКА ===");
            System.out.println("Подписчиков на объявление: " + subscribersCount);
            System.out.println("Подписок у " + users.get(1).getLogin() + ": " + userSubscriptionsCount);
        }

        private static void demonstrateUnsubscribe(ParticipatesRepository participatesRepository,
                                                   List<User> users,
                                                   Post post) {
            participatesRepository.unsubscribe(users.get(1), post);
            boolean isSubscribed = participatesRepository.isSubscribed(users.get(1), post);

            System.out.println("\n" + users.get(1).getLogin()
                    + " подписан на объявление после отписки: " + isSubscribed);

            long subscribersCount = participatesRepository.countSubscribersByPost(post);
            System.out.println("Подписчиков на объявление после отписки: " + subscribersCount);
        }
    }