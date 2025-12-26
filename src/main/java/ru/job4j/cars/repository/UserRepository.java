package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import ru.job4j.cars.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);
    private final CrudRepository crudRepository;

    public User save(User user) {
        crudRepository.run(session -> session.save(user));
        return user;
    }

    public Optional<User> findById(int id) {
        return crudRepository.optional(
                "FROM User WHERE id = :id",
                User.class,
                Map.of("id", id)
        );
    }

    public Optional<User> findByLogin(String login) {
        return crudRepository.optional(
                "FROM User WHERE login = :login",
                User.class,
                Map.of("login", login)
        );
    }

    public Optional<User> findByLoginAndPassword(String login, String password) {
        return crudRepository.optional(
                "FROM User WHERE login = :login AND password = :password",
                User.class,
                Map.of("login", login, "password", password)
        );
    }

    public List<User> findAll() {
        return crudRepository.query("FROM User", User.class);
    }
}