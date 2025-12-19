package ru.job4j.cars.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriceHistoryRepositoryTest extends RepositoryTestBase {

    private PriceHistoryRepository priceHistoryRepository;
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
        priceHistoryRepository = new PriceHistoryRepository(crudRepository);
        clearDatabase();
    }

    private User createTestUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword("password");
        return userRepository.save(user);
    }

    private Post createTestPostWithPrice(String description, User user, long price) {
        Engine engine = new Engine();
        engine.setName("Engine");
        engine.setVolume(2.0);
        engine.setPower(150);
        engineRepository.save(engine);

        Car car = new Car();
        car.setBrand("Car");
        car.setModel("Model");
        car.setManufactureYear(2020);
        car.setEngine(engine);
        carRepository.create(car);

        Post post = new Post();
        post.setDescription(description);
        post.setUser(user);
        post.setCar(car);
        post.setPrice(price);
        return postRepository.save(post);
    }

    @Test
    void whenSavePriceHistoryThenFindAll() {
        User user = createTestUser("user");
        Post post = createTestPostWithPrice("Test post", user, 1500000L);

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setBefore(1600000L);
        priceHistory.setAfter(1500000L);
        priceHistory.setCreated(LocalDateTime.now());
        priceHistory.setPost(post);

        priceHistoryRepository.save(priceHistory);

        List<PriceHistory> all = priceHistoryRepository.findAllOrderById();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getBefore()).isEqualTo(1600000L);
        assertThat(all.get(0).getAfter()).isEqualTo(1500000L);
        assertThat(all.get(0).getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    void whenSaveMultiplePriceHistoriesThenFindAllSorted() {
        User user = createTestUser("user");
        Post post = createTestPostWithPrice("Test post", user, 1500000L);

        PriceHistory ph1 = new PriceHistory();
        ph1.setBefore(1700000L);
        ph1.setAfter(1600000L);
        ph1.setPost(post);
        priceHistoryRepository.save(ph1);

        PriceHistory ph2 = new PriceHistory();
        ph2.setBefore(1600000L);
        ph2.setAfter(1500000L);
        ph2.setPost(post);
        priceHistoryRepository.save(ph2);

        List<PriceHistory> all = priceHistoryRepository.findAllOrderById();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isLessThan(all.get(1).getId());
        assertThat(all.get(0).getBefore()).isEqualTo(1700000L);
        assertThat(all.get(1).getBefore()).isEqualTo(1600000L);
    }

    @Test
    void whenSavePriceHistoryWithoutPostThenSuccess() {
        // В текущей реализации PriceHistory не требует обязательной связи с Post
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setBefore(1000000L);
        priceHistory.setAfter(900000L);
        priceHistory.setCreated(LocalDateTime.now());

        priceHistoryRepository.save(priceHistory);

        List<PriceHistory> all = priceHistoryRepository.findAllOrderById();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getBefore()).isEqualTo(1000000L);
        assertThat(all.get(0).getAfter()).isEqualTo(900000L);
        assertThat(all.get(0).getPost()).isNull();
    }

    @Test
    void whenSavePriceHistoryForDifferentPostsThenSuccess() {
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");

        Post post1 = createTestPostWithPrice("Post 1", user1, 1000000L);
        Post post2 = createTestPostWithPrice("Post 2", user2, 2000000L);

        PriceHistory ph1 = new PriceHistory();
        ph1.setBefore(1100000L);
        ph1.setAfter(1000000L);
        ph1.setPost(post1);
        priceHistoryRepository.save(ph1);

        PriceHistory ph2 = new PriceHistory();
        ph2.setBefore(2100000L);
        ph2.setAfter(2000000L);
        ph2.setPost(post2);
        priceHistoryRepository.save(ph2);

        List<PriceHistory> all = priceHistoryRepository.findAllOrderById();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getPost().getId()).isEqualTo(post1.getId());
        assertThat(all.get(1).getPost().getId()).isEqualTo(post2.getId());
    }
}