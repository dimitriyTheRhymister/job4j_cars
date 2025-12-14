package ru.job4j.cars.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.model.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipatesRepositoryTest extends RepositoryTestBase {

    private ParticipatesRepository participatesRepository;
    private PostRepository postRepository;
    private UserRepository userRepository;
    private CarRepository carRepository;
    private EngineRepository engineRepository;

    @Override
    @BeforeEach
    public void init() {
        super.init();
        userRepository = new UserRepository(crudRepository);
        engineRepository = new EngineRepository(crudRepository);
        carRepository = new CarRepository(crudRepository);
        postRepository = new PostRepository(crudRepository);
        participatesRepository = new ParticipatesRepository(crudRepository);
        clearDatabase();
    }

    private User createTestUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword("password");
        return userRepository.create(user);
    }

    private Post createTestPost(String description, User user) {
        Engine engine = new Engine();
        engine.setName("Engine");
        engine.setVolume(2.0);
        engine.setPower(150);
        engineRepository.create(engine);

        Car car = new Car();
        car.setName("Car");
        car.setModel("Model");
        car.setManufactureYear(2020);
        car.setEngine(engine);
        carRepository.create(car);

        Post post = new Post();
        post.setDescription(description);
        post.setUser(user);
        post.setCar(car);
        return postRepository.create(post);
    }

    @Test
    void whenSubscribeUserToPostThenSuccess() {
        User user = createTestUser("subscriber");
        User author = createTestUser("author");
        Post post = createTestPost("Test post", author);

        Participates subscription = participatesRepository.subscribe(user, post);

        assertThat(subscription.getId()).isGreaterThan(0);
        assertThat(subscription.getUser().getId()).isEqualTo(user.getId());
        assertThat(subscription.getPost().getId()).isEqualTo(post.getId());
        assertThat(subscription.getCreated()).isNotNull();
    }

    @Test
    void whenUnsubscribeUserFromPostThenSuccess() {
        User user = createTestUser("subscriber");
        User author = createTestUser("author");
        Post post = createTestPost("Test post", author);

        participatesRepository.subscribe(user, post);
        boolean wasSubscribed = participatesRepository.isSubscribed(user, post);
        assertThat(wasSubscribed).isTrue();

        participatesRepository.unsubscribe(user, post);
        boolean isSubscribed = participatesRepository.isSubscribed(user, post);
        assertThat(isSubscribed).isFalse();
    }

    @Test
    void whenFindSubscribersByPostThenSuccess() {
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");
        User author = createTestUser("author");
        Post post = createTestPost("Popular post", author);

        participatesRepository.subscribe(user1, post);
        participatesRepository.subscribe(user2, post);

        List<User> subscribers = participatesRepository.findSubscribersByPost(post);
        assertThat(subscribers).hasSize(2);
        assertThat(subscribers).extracting(User::getLogin)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void whenFindSubscriptionsByUserThenSuccess() {
        User user = createTestUser("user");
        User author1 = createTestUser("author1");
        User author2 = createTestUser("author2");

        Post post1 = createTestPost("Post 1", author1);
        Post post2 = createTestPost("Post 2", author2);

        participatesRepository.subscribe(user, post1);
        participatesRepository.subscribe(user, post2);

        List<Post> subscriptions = participatesRepository.findSubscriptionsByUser(user);
        assertThat(subscriptions).hasSize(2);
        assertThat(subscriptions).extracting(Post::getDescription)
                .containsExactlyInAnyOrder("Post 1", "Post 2");
    }

    @Test
    void whenCountSubscribersByPostThenCorrect() {
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");
        User user3 = createTestUser("user3");
        User author = createTestUser("author");

        Post post = createTestPost("Post with subscribers", author);

        participatesRepository.subscribe(user1, post);
        participatesRepository.subscribe(user2, post);
        participatesRepository.subscribe(user3, post);

        long count = participatesRepository.countSubscribersByPost(post);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void whenCountSubscriptionsByUserThenCorrect() {
        User user = createTestUser("activeuser");
        User author1 = createTestUser("author1");
        User author2 = createTestUser("author2");
        User author3 = createTestUser("author3");

        Post post1 = createTestPost("Post 1", author1);
        Post post2 = createTestPost("Post 2", author2);
        Post post3 = createTestPost("Post 3", author3);

        participatesRepository.subscribe(user, post1);
        participatesRepository.subscribe(user, post2);
        participatesRepository.subscribe(user, post3);

        long count = participatesRepository.countSubscriptionsByUser(user);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void whenCreateParticipatesThenFindById() {
        User user = createTestUser("user");
        User author = createTestUser("author");
        Post post = createTestPost("Test post", author);

        Participates participates = new Participates();
        participates.setUser(user);
        participates.setPost(post);
        participatesRepository.create(participates);

        var found = participatesRepository.findById(participates.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(found.get().getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    void whenUpdateParticipatesThenChangesSaved() {
        User user = createTestUser("user");
        User author1 = createTestUser("author1");
        User author2 = createTestUser("author2");

        Post post1 = createTestPost("Post 1", author1);
        Post post2 = createTestPost("Post 2", author2);

        Participates participates = new Participates();
        participates.setUser(user);
        participates.setPost(post1);
        participatesRepository.create(participates);

        participates.setPost(post2);
        participatesRepository.update(participates);

        var updated = participatesRepository.findById(participates.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getPost().getId()).isEqualTo(post2.getId());
    }

    @Test
    void whenDeleteParticipatesThenNotFound() {
        User user = createTestUser("user");
        User author = createTestUser("author");
        Post post = createTestPost("Test post", author);

        Participates participates = new Participates();
        participates.setUser(user);
        participates.setPost(post);
        participatesRepository.create(participates);
        int id = participates.getId();

        participatesRepository.delete(id);

        var deleted = participatesRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    void whenFindAllParticipatesThenSortedById() {
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");
        User author = createTestUser("author");
        Post post = createTestPost("Test post", author);

        Participates p1 = new Participates();
        p1.setUser(user1);
        p1.setPost(post);
        participatesRepository.create(p1);

        Participates p2 = new Participates();
        p2.setUser(user2);
        p2.setPost(post);
        participatesRepository.create(p2);

        List<Participates> all = participatesRepository.findAllOrderById();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isLessThan(all.get(1).getId());
    }
}