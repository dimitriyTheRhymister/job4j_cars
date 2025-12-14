package ru.job4j.cars.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.model.Car;
import ru.job4j.cars.model.Engine;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PostRepositoryTest extends RepositoryTestBase {

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
        clearDatabase();
    }

    private User createTestUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword("password");
        return userRepository.create(user);
    }

    private Car createTestCar(String name, String model) {
        Engine engine = new Engine();
        engine.setName("Engine " + name);
        engine.setVolume(2.0);
        engine.setPower(150);
        engineRepository.create(engine);

        Car car = new Car();
        car.setName(name);
        car.setModel(model);
        car.setManufactureYear(2020);
        car.setEngine(engine);
        return carRepository.create(car);
    }

    @Test
    void whenCreatePostThenFindById() {
        User user = createTestUser("postauthor");
        Car car = createTestCar("Toyota", "Camry");

        Post post = new Post();
        post.setDescription("Продам Toyota Camry в отличном состоянии");
        post.setUser(user);
        post.setCar(car);
        post.setCurrentPrice(1500000L);

        postRepository.create(post);

        Optional<Post> found = postRepository.findById(post.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Продам Toyota Camry в отличном состоянии");
        assertThat(found.get().getCurrentPrice()).isEqualTo(1500000L);
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(found.get().getCar().getId()).isEqualTo(car.getId());
        assertThat(found.get().getCreated()).isCloseTo(LocalDateTime.now(), within(1, java.time.temporal.ChronoUnit.MINUTES));
    }

    @Test
    void whenCreatePostWithPhotosThenSuccess() {
        User user = createTestUser("user");
        Car car = createTestCar("BMW", "X5");

        Post post = new Post();
        post.setDescription("Продам BMW X5 с фото");
        post.setUser(user);
        post.setCar(car);
        post.setCurrentPrice(3000000L);
        post.addPhoto("https://example.com/bmw1.jpg");
        post.addPhoto("https://example.com/bmw2.jpg");

        postRepository.create(post);

        Optional<Post> found = postRepository.findByIdWithPhotos(post.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getPhotoCount()).isEqualTo(2);
        assertThat(found.get().hasPhotos()).isTrue();
    }

    @Test
    void whenFindAllPostsOrderByCreatedDescThenNewestFirst() {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        Post post1 = new Post();
        post1.setDescription("Post 1");
        post1.setUser(user);
        post1.setCar(car);
        post1.setCreated(LocalDateTime.now().minusDays(1));
        postRepository.create(post1);

        Post post2 = new Post();
        post2.setDescription("Post 2");
        post2.setUser(user);
        post2.setCar(car);
        post2.setCreated(LocalDateTime.now());
        postRepository.create(post2);

        List<Post> posts = postRepository.findAllOrderByCreatedDesc();
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getDescription()).isEqualTo("Post 2");
        assertThat(posts.get(1).getDescription()).isEqualTo("Post 1");
    }

    @Test
    void whenFindPostsFromLastDayThenSuccess() throws InterruptedException {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        // Пост от вчера
        Post oldPost = new Post();
        oldPost.setDescription("Old post");
        oldPost.setUser(user);
        oldPost.setCar(car);
        oldPost.setCreated(LocalDateTime.now().minusDays(2));
        postRepository.create(oldPost);

        // Пост от сегодня
        Post newPost = new Post();
        newPost.setDescription("New post");
        newPost.setUser(user);
        newPost.setCar(car);
        newPost.setCreated(LocalDateTime.now().minusHours(12));
        postRepository.create(newPost);

        List<Post> posts = postRepository.findPostsFromLastDay();
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getDescription()).isEqualTo("New post");
    }

    @Test
    void whenFindPostsWithPhotosThenOnlyWithPhotos() {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        Post post1 = new Post();
        post1.setDescription("Post with photos");
        post1.setUser(user);
        post1.setCar(car);
        post1.addPhoto("photo1.jpg");
        post1.addPhoto("photo2.jpg");
        postRepository.create(post1);

        Post post2 = new Post();
        post2.setDescription("Post without photos");
        post2.setUser(user);
        post2.setCar(car);
        postRepository.create(post2);

        List<Post> postsWithPhotos = postRepository.findPostsWithPhotos();
        assertThat(postsWithPhotos).hasSize(1);
        assertThat(postsWithPhotos.get(0).getDescription()).isEqualTo("Post with photos");

        List<Post> postsWithoutPhotos = postRepository.findPostsWithoutPhotos();
        assertThat(postsWithoutPhotos).hasSize(1);
        assertThat(postsWithoutPhotos.get(0).getDescription()).isEqualTo("Post without photos");
    }

    @Test
    void whenFindPostsByCarBrandThenSuccess() {
        User user = createTestUser("user");

        Car car1 = createTestCar("Toyota", "Camry");
        Car car2 = createTestCar("Honda", "Civic");

        Post post1 = new Post();
        post1.setDescription("Toyota post");
        post1.setUser(user);
        post1.setCar(car1);
        postRepository.create(post1);

        Post post2 = new Post();
        post2.setDescription("Honda post");
        post2.setUser(user);
        post2.setCar(car2);
        postRepository.create(post2);

        List<Post> toyotaPosts = postRepository.findPostsByCarBrand("Toyota");
        assertThat(toyotaPosts).hasSize(1);
        assertThat(toyotaPosts.get(0).getDescription()).isEqualTo("Toyota post");
    }

    @Test
    void whenFindPostsByUserIdThenSuccess() {
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");
        Car car = createTestCar("Car", "Model");

        Post post1 = new Post();
        post1.setDescription("User1 post");
        post1.setUser(user1);
        post1.setCar(car);
        postRepository.create(post1);

        Post post2 = new Post();
        post2.setDescription("User2 post");
        post2.setUser(user2);
        post2.setCar(car);
        postRepository.create(post2);

        List<Post> user1Posts = postRepository.findPostsByUserId(user1.getId());
        assertThat(user1Posts).hasSize(1);
        assertThat(user1Posts.get(0).getDescription()).isEqualTo("User1 post");
    }

    @Test
    void whenFindPostsByPriceRangeThenSuccess() {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        Post post1 = new Post();
        post1.setDescription("Cheap post");
        post1.setUser(user);
        post1.setCar(car);
        post1.setCurrentPrice(1000000L);
        postRepository.create(post1);

        Post post2 = new Post();
        post2.setDescription("Expensive post");
        post2.setUser(user);
        post2.setCar(car);
        post2.setCurrentPrice(3000000L);
        postRepository.create(post2);

        List<Post> posts = postRepository.findPostsByPriceRange(1500000L, 4000000L);
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getDescription()).isEqualTo("Expensive post");
    }

    @Test
    void whenSearchByKeywordThenSuccess() {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        Post post1 = new Post();
        post1.setDescription("Продам отличный автомобиль в идеальном состоянии");
        post1.setUser(user);
        post1.setCar(car);
        postRepository.create(post1);

        Post post2 = new Post();
        post2.setDescription("Требуется срочный ремонт автомобиля");
        post2.setUser(user);
        post2.setCar(car);
        postRepository.create(post2);

        List<Post> found = postRepository.searchByKeyword("идеальном");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDescription()).contains("идеальном");
    }

    @Test
    void whenAddPhotoToPostThenSuccess() {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        Post post = new Post();
        post.setDescription("Post with added photo");
        post.setUser(user);
        post.setCar(car);
        postRepository.create(post);

        postRepository.addPhotoToPost(post.getId(), "newphoto.jpg");

        Optional<Post> updated = postRepository.findByIdWithPhotos(post.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getPhotoCount()).isEqualTo(1);
        assertThat(updated.get().getPhotoUrls()).contains("newphoto.jpg");
    }

    @Test
    void whenUpdatePostThenChangesSaved() {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        Post post = new Post();
        post.setDescription("Old description");
        post.setUser(user);
        post.setCar(car);
        post.setCurrentPrice(1000000L);
        postRepository.create(post);

        post.setDescription("New description");
        post.setCurrentPrice(1200000L);
        postRepository.update(post);

        Optional<Post> updated = postRepository.findById(post.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getDescription()).isEqualTo("New description");
        assertThat(updated.get().getCurrentPrice()).isEqualTo(1200000L);
    }

    @Test
    void whenDeletePostThenNotFound() {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        Post post = new Post();
        post.setDescription("To delete");
        post.setUser(user);
        post.setCar(car);
        postRepository.create(post);
        int id = post.getId();

        postRepository.delete(id);

        Optional<Post> deleted = postRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    void whenCountPostsThenCorrect() {
        User user = createTestUser("user");
        Car car = createTestCar("Car", "Model");

        Post post1 = new Post();
        post1.setDescription("Post 1");
        post1.setUser(user);
        post1.setCar(car);
        postRepository.create(post1);

        Post post2 = new Post();
        post2.setDescription("Post 2");
        post2.setUser(user);
        post2.setCar(car);
        post2.addPhoto("photo.jpg");
        postRepository.create(post2);

        long totalCount = postRepository.countAllPosts();
        long withPhotosCount = postRepository.countPostsWithPhotos();
        long withoutPhotosCount = postRepository.countPostsWithoutPhotos();

        assertThat(totalCount).isEqualTo(2);
        assertThat(withPhotosCount).isEqualTo(1);
        assertThat(withoutPhotosCount).isEqualTo(1);
    }
}