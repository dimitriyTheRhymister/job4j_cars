package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import ru.job4j.cars.model.Participates;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class ParticipatesRepository {
    private final CrudRepository crudRepository;

    /**
     * Сохранить подписку в базе.
     * @param participates подписка.
     * @return подписка с id.
     */
    public Participates create(Participates participates) {
        crudRepository.run(session -> session.persist(participates));
        return participates;
    }

    /**
     * Обновить подписку в базе.
     * @param participates подписка.
     */
    public void update(Participates participates) {
        crudRepository.run(session -> session.merge(participates));
    }

    /**
     * Удалить подписку по id.
     * @param participatesId ID
     */
    public void delete(int participatesId) {
        crudRepository.run(
                "DELETE FROM Participates WHERE id = :fId",
                Map.of("fId", participatesId)
        );
    }

    /**
     * Подписать пользователя на объявление
     */
    public Participates subscribe(User user, Post post) {
        Participates participates = new Participates();
        participates.setUser(user);
        participates.setPost(post);
        return create(participates);
    }

    /**
     * Отписать пользователя от объявления
     */
    public void unsubscribe(User user, Post post) {
        crudRepository.run(
                "DELETE FROM Participates WHERE user = :fUser AND post = :fPost",
                Map.of("fUser", user, "fPost", post)
        );
    }

    /**
     * Список всех подписок отсортированных по id.
     * @return список подписок.
     */
    public List<Participates> findAllOrderById() {
        return crudRepository.query("FROM Participates ORDER BY id ASC", Participates.class);
    }

    /**
     * Найти подписку по ID
     * @return подписка.
     */
    public Optional<Participates> findById(int participatesId) {
        return crudRepository.optional(
                "FROM Participates WHERE id = :fId", Participates.class,
                Map.of("fId", participatesId)
        );
    }

    /**
     * Найти подписку по пользователю и объявлению
     */
    public Optional<Participates> findByUserAndPost(User user, Post post) {
        return crudRepository.optional(
                "FROM Participates WHERE user = :fUser AND post = :fPost", Participates.class,
                Map.of("fUser", user, "fPost", post)
        );
    }

    /**
     * Проверить, подписан ли пользователь на объявление
     */
    public boolean isSubscribed(User user, Post post) {
        return findByUserAndPost(user, post).isPresent();
    }

    /**
     * Найти всех подписчиков объявления
     */
    public List<User> findSubscribersByPost(Post post) {
        return crudRepository.query(
                "SELECT p.user FROM Participates p WHERE p.post = :fPost", User.class,
                Map.of("fPost", post)
        );
    }

    /**
     * Найти все подписки пользователя
     */
    public List<Post> findSubscriptionsByUser(User user) {
        return crudRepository.query(
                "SELECT p.post FROM Participates p WHERE p.user = :fUser", Post.class,
                Map.of("fUser", user)
        );
    }

    /**
     * Найти все подписки по объявлению
     */
    public List<Participates> findByPost(Post post) {
        return crudRepository.query(
                "FROM Participates WHERE post = :fPost ORDER BY created DESC", Participates.class,
                Map.of("fPost", post)
        );
    }

    /**
     * Найти все подписки по пользователю
     */
    public List<Participates> findByUser(User user) {
        return crudRepository.query(
                "FROM Participates WHERE user = :fUser ORDER BY created DESC", Participates.class,
                Map.of("fUser", user)
        );
    }

    /**
     * Количество подписчиков объявления
     */
    public long countSubscribersByPost(Post post) {
        return crudRepository.query(
                "SELECT COUNT(p) FROM Participates p WHERE p.post = :fPost", Long.class,
                Map.of("fPost", post)
        ).get(0);
    }

    /**
     * Количество подписок пользователя
     */
    public long countSubscriptionsByUser(User user) {
        return crudRepository.query(
                "SELECT COUNT(p) FROM Participates p WHERE p.user = :fUser", Long.class,
                Map.of("fUser", user)
        ).get(0);
    }
}