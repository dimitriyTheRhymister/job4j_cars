package ru.job4j.cars.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.model.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexIntegrationTest extends RepositoryTestBase {

    private UserRepository userRepository;
    private EngineRepository engineRepository;
    private CarRepository carRepository;
    private OwnerRepository ownerRepository;
    private PostRepository postRepository;
    private ParticipatesRepository participatesRepository;
    private PriceHistoryRepository priceHistoryRepository;

    @Override
    @BeforeEach
    public void init() {
        super.init();
        userRepository = new UserRepository(crudRepository);
        engineRepository = new EngineRepository(crudRepository);
        carRepository = new CarRepository(crudRepository);
        ownerRepository = new OwnerRepository(crudRepository);
        postRepository = new PostRepository(crudRepository);
        participatesRepository = new ParticipatesRepository(crudRepository);
        priceHistoryRepository = new PriceHistoryRepository(crudRepository);
        clearDatabase();
    }

    @Test
    void whenCompleteScenarioThenSuccess() {
        // 1. Создаем пользователей
        User seller = new User();
        seller.setLogin("seller");
        seller.setPassword("pass");
        userRepository.create(seller);

        User buyer1 = new User();
        buyer1.setLogin("buyer1");
        buyer1.setPassword("pass");
        userRepository.create(buyer1);

        User buyer2 = new User();
        buyer2.setLogin("buyer2");
        buyer2.setPassword("pass");
        userRepository.create(buyer2);

        // 2. Создаем двигатель
        Engine engine = new Engine();
        engine.setName("V6 3.5L");
        engine.setVolume(3.5);
        engine.setPower(249);
        engineRepository.create(engine);

        // 3. Создаем автомобиль
        Car car = new Car();
        car.setName("Toyota");
        car.setModel("Camry");
        car.setManufactureYear(2020);
        car.setEngine(engine);
        carRepository.create(car);

        // 4. Создаем владельца
        Owner owner = new Owner();
        owner.setName("Иванов Иван Иванович");
        owner.setUser(seller);
        ownerRepository.create(owner);

        // 5. Создаем объявление
        Post post = new Post();
        post.setDescription("Продам Toyota Camry 2020 года, отличное состояние");
        post.setUser(seller);
        post.setCar(car);
        post.setCurrentPrice(1500000L);
        post.addPhoto("https://example.com/toyota1.jpg");
        post.addPhoto("https://example.com/toyota2.jpg");
        postRepository.create(post);

        // 6. Добавляем историю цен
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setBefore(1600000L);
        priceHistory.setAfter(1500000L);
        priceHistory.setPost(post);
        priceHistoryRepository.save(priceHistory);

        // 7. Пользователи подписываются на объявление
        participatesRepository.subscribe(buyer1, post);
        participatesRepository.subscribe(buyer2, post);

        // Проверяем результаты

        // Проверяем пользователей
        List<User> allUsers = userRepository.findAllOrderById();
        assertThat(allUsers).hasSize(3);

        // Проверяем автомобиль
        Optional<Car> foundCar = carRepository.findById(car.getId());
        assertThat(foundCar).isPresent();
        assertThat(foundCar.get().getName()).isEqualTo("Toyota");

        // Проверяем объявление
        Optional<Post> foundPost = postRepository.findByIdWithPhotos(post.getId());
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getPhotoCount()).isEqualTo(2);
        assertThat(foundPost.get().getCurrentPrice()).isEqualTo(1500000L);

        // Проверяем подписчиков
        List<User> subscribers = participatesRepository.findSubscribersByPost(post);
        assertThat(subscribers).hasSize(2);
        assertThat(subscribers).extracting(User::getLogin)
                .containsExactlyInAnyOrder("buyer1", "buyer2");

        // Проверяем историю цен
        List<PriceHistory> priceHistories = priceHistoryRepository.findAllOrderById();
        assertThat(priceHistories).hasSize(1);
        assertThat(priceHistories.get(0).getBefore()).isEqualTo(1600000L);

        // Проверяем владельца
        List<Owner> owners = ownerRepository.findByUserId(seller.getId());
        assertThat(owners).hasSize(1);
        assertThat(owners.get(0).getName()).isEqualTo("Иванов Иван Иванович");

        // Проверяем статистику
        long postCount = postRepository.countAllPosts();
        long subscribersCount = participatesRepository.countSubscribersByPost(post);
        assertThat(postCount).isEqualTo(1);
        assertThat(subscribersCount).isEqualTo(2);
    }
}