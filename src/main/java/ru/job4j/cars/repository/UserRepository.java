package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ru.job4j.cars.model.User;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor
public class UserRepository {
    private final SessionFactory sf;

    /**
     * Вспомогательный метод для выполнения операций в транзакции
     */
    private <T> T execute(Function<Session, T> command) {
        Session session = sf.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            T result = command.apply(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * Сохранить в базе.
     * @param user пользователь.
     * @return пользователь с id.
     */
    public User create(User user) {
        execute(session -> {
            session.save(user);
            return null;
        });
        return user;
    }

    /**
     * Обновить в базе пользователя.
     * @param user пользователь.
     */
    public void update(User user) {
        execute(session -> {
            session.update(user);
            return null;
        });
    }

    /**
     * Удалить пользователя по id.
     * @param userId ID
     */
    public void delete(Integer userId) {
        execute(session -> {
            User user = new User();
            user.setId(userId);
            session.delete(user);
            return null;
        });
    }

    /**
     * Список пользователь отсортированных по id.
     * @return список пользователей.
     */
    public List<User> findAllOrderById() {
        return execute(session ->
                session.createQuery("FROM User ORDER BY id", User.class)
                        .list()
        );
    }

    /**
     * Найти пользователя по ID
     * @return пользователь.
     */
    public Optional<User> findById(Integer userId) {
        return execute(session ->
                Optional.ofNullable(session.get(User.class, userId))
        );
    }

    /**
     * Список пользователей по login LIKE %key%
     * @param key key
     * @return список пользователей.
     */
    public List<User> findByLikeLogin(String key) {
        return execute(session ->
                session.createQuery("FROM User WHERE login LIKE :login", User.class)
                        .setParameter("login", "%" + key + "%")
                        .list()
        );
    }

    /**
     * Найти пользователя по login.
     * @param login login.
     * @return Optional or user.
     */
    public Optional<User> findByLogin(String login) {
        return execute(session ->
                session.createQuery("FROM User WHERE login = :login", User.class)
                        .setParameter("login", login)
                        .uniqueResultOptional()
        );
    }
}