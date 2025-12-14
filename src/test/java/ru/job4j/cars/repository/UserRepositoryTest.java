package ru.job4j.cars.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends RepositoryTestBase {

    private UserRepository userRepository;

    @Override
    @BeforeEach
    public void init() {
        super.init();
        userRepository = new UserRepository(crudRepository);
        clearDatabase();
    }

    @Test
    void whenCreateUserThenFindById() {
        User user = new User();
        user.setLogin("testuser");
        user.setPassword("password123");

        userRepository.create(user);

        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("testuser");
        assertThat(found.get().getPassword()).isEqualTo("password123");
    }

    @Test
    void whenUpdateUserThenChangesSaved() {
        User user = new User();
        user.setLogin("oldlogin");
        user.setPassword("oldpass");
        userRepository.create(user);

        user.setLogin("newlogin");
        user.setPassword("newpass");
        userRepository.update(user);

        Optional<User> updated = userRepository.findById(user.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getLogin()).isEqualTo("newlogin");
        assertThat(updated.get().getPassword()).isEqualTo("newpass");
    }

    @Test
    void whenDeleteUserThenNotFound() {
        User user = new User();
        user.setLogin("todelete");
        user.setPassword("pass");
        userRepository.create(user);
        int id = user.getId();

        userRepository.delete(id);

        Optional<User> deleted = userRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    void whenFindAllUsersThenGetList() {
        User user1 = new User();
        user1.setLogin("user1");
        user1.setPassword("pass1");
        userRepository.create(user1);

        User user2 = new User();
        user2.setLogin("user2");
        user2.setPassword("pass2");
        userRepository.create(user2);

        List<User> users = userRepository.findAllOrderById();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getLogin)
                .containsExactly("user1", "user2");
    }

    @Test
    void whenFindByLoginThenSuccess() {
        User user = new User();
        user.setLogin("uniqueuser");
        user.setPassword("pass");
        userRepository.create(user);

        Optional<User> found = userRepository.findByLogin("uniqueuser");
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("uniqueuser");
    }

    @Test
    void whenFindByLikeLoginThenFindMatching() {
        User user1 = new User();
        user1.setLogin("ivanov123");
        user1.setPassword("pass");
        userRepository.create(user1);

        User user2 = new User();
        user2.setLogin("ivanova");
        user2.setPassword("pass");
        userRepository.create(user2);

        User user3 = new User();
        user3.setLogin("petrov");
        user3.setPassword("pass");
        userRepository.create(user3);

        List<User> users = userRepository.findByLikeLogin("ivan");
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getLogin)
                .containsExactlyInAnyOrder("ivanov123", "ivanova");
    }

    @Test
    void whenFindByNonExistentLoginThenEmpty() {
        Optional<User> found = userRepository.findByLogin("nonexistent");
        assertThat(found).isEmpty();
    }
}