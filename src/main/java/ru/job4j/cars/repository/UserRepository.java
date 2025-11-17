package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
            Query query = session.createQuery(
                    "UPDATE User SET login = :login, password = :password WHERE id = :id"
            );
            query.setParameter("login", user.getLogin());
            query.setParameter("password", user.getPassword());
            query.setParameter("id", user.getId());
            return query.executeUpdate();  // HQL DML подход
        });
    }

    /**
     * Удалить пользователя по id.
     * @param userId ID
     */
    public void delete(Integer userId) {
        execute(session -> {
            Query query = session.createQuery("DELETE User WHERE id = :id");
            query.setParameter("id", userId);
            return query.executeUpdate();  // HQL DML подход
        });
    }

    /**
     * Список пользователь отсортированных по id.
     * @return список пользователей.
     */
    public List<User> findAllOrderById() {
        return execute(session -> {
            Query<User> query = session.createQuery(
                    "FROM User ORDER BY id", User.class
            );
            return query.list();  // HQL подход
        });
    }

    /**
     * Найти пользователя по ID
     * @return пользователь.
     */
    public Optional<User> findById(Integer userId) {
        return execute(session -> {
            Query<User> query = session.createQuery(
                    "FROM User WHERE id = :id", User.class
            );
            query.setParameter("id", userId);
            return query.uniqueResultOptional();  // HQL подход
        });
    }

    /**
     * Список пользователей по login LIKE %key%
     * @param key key
     * @return список пользователей.
     */
    public List<User> findByLikeLogin(String key) {
        return execute(session -> {
            Query<User> query = session.createQuery(
                    "FROM User WHERE login LIKE :login", User.class
            );
            query.setParameter("login", "%" + key + "%");
            return query.list();  // HQL подход
        });
    }

    /**
     * Найти пользователя по login.
     * @param login login.
     * @return Optional or user.
     */
    public Optional<User> findByLogin(String login) {
        return execute(session -> {
            Query<User> query = session.createQuery(
                    "FROM User WHERE login = :login", User.class
            );
            query.setParameter("login", login);
            return query.uniqueResultOptional();  // HQL подход
        });
    }
}