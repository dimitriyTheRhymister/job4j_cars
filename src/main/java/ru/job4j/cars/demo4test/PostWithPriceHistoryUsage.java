package ru.job4j.cars.demo4test;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.PriceHistory;
import ru.job4j.cars.model.User;

import java.time.LocalDateTime;

public class PostWithPriceHistoryUsage {

    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure().build();
        try (SessionFactory sf = new MetadataSources(registry)
                .buildMetadata().buildSessionFactory()) {

            Integer postId = createPostWithPriceHistory(sf);
            verifyPostAndPriceHistory(sf, postId);

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static Integer createPostWithPriceHistory(SessionFactory sf) {
        var session = sf.openSession();
        session.beginTransaction();

        var user = new User();
        user.setLogin("seller");
        user.setPassword("password");
        session.save(user);

        var post = new Post();
        post.setDescription("Продам машину");
        post.setUser(user);
        post.setCreated(LocalDateTime.now());

        var priceHistory = new PriceHistory();
        priceHistory.setBefore(1000000L);
        priceHistory.setAfter(900000L);
        priceHistory.setCreated(LocalDateTime.now());

        post.getPriceHistories().add(priceHistory);

        session.save(post);
        session.getTransaction().commit();
        session.close();

        return post.getId();
    }

    private static void verifyPostAndPriceHistory(SessionFactory sf, Integer postId) {
        var session = sf.openSession();
        var savedPost = session.find(Post.class, postId);

        System.out.println("=== ПРОВЕРКА СВЯЗИ POST -> PRICE_HISTORY ===");
        System.out.println("Пост: " + savedPost.getDescription());
        System.out.println("Пользователь: " + savedPost.getUser().getLogin());
        System.out.println("Количество историй цен: " + savedPost.getPriceHistories().size());

        savedPost.getPriceHistories().forEach(ph ->
                System.out.println("Цена изменилась с " + ph.getBefore() + " на " + ph.getAfter())
        );

        session.close();
    }
}