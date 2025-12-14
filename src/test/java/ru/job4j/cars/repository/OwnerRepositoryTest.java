package ru.job4j.cars.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.model.Car;
import ru.job4j.cars.model.Engine;
import ru.job4j.cars.model.Owner;
import ru.job4j.cars.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerRepositoryTest extends RepositoryTestBase {

    private OwnerRepository ownerRepository;
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
        ownerRepository = new OwnerRepository(crudRepository);
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
    void whenCreateOwnerThenFindById() {
        User user = createTestUser("owneruser");

        Owner owner = new Owner();
        owner.setName("Иванов Иван Иванович");
        owner.setUser(user);

        ownerRepository.create(owner);

        Optional<Owner> found = ownerRepository.findById(owner.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Иванов Иван Иванович");
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void whenFindAllOwnersThenSortedById() {
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");

        Owner owner1 = new Owner();
        owner1.setName("Owner1");
        owner1.setUser(user1);
        ownerRepository.create(owner1);

        Owner owner2 = new Owner();
        owner2.setName("Owner2");
        owner2.setUser(user2);
        ownerRepository.create(owner2);

        List<Owner> owners = ownerRepository.findAllOrderById();
        assertThat(owners).hasSize(2);
        assertThat(owners.get(0).getId()).isLessThan(owners.get(1).getId());
        assertThat(owners).extracting(Owner::getName)
                .containsExactly("Owner1", "Owner2");
    }

    @Test
    void whenFindByNameThenSuccess() {
        User user = createTestUser("user");

        Owner owner1 = new Owner();
        owner1.setName("Иванов");
        owner1.setUser(user);
        ownerRepository.create(owner1);

        Owner owner2 = new Owner();
        owner2.setName("Петров");
        owner2.setUser(user);
        ownerRepository.create(owner2);

        List<Owner> found = ownerRepository.findByName("Иванов");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Иванов");
    }

    @Test
    void whenFindByUserIdThenSuccess() {
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");

        Owner owner1 = new Owner();
        owner1.setName("Owner1");
        owner1.setUser(user1);
        ownerRepository.create(owner1);

        Owner owner2 = new Owner();
        owner2.setName("Owner2");
        owner2.setUser(user2);
        ownerRepository.create(owner2);

        List<Owner> found = ownerRepository.findByUserId(user1.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Owner1");
    }

    @Test
    void whenFindByCarIdThenSuccess() {
        // Создаем пользователя
        User user = createTestUser("user");

        // Создаем владельца
        Owner owner = new Owner();
        owner.setName("Test Owner");
        owner.setUser(user);
        ownerRepository.create(owner);

        // Создаем автомобиль
        Car car = createTestCar("Toyota", "Camry");

        // Устанавливаем связь многие-ко-многим
        Set<Owner> owners = new HashSet<>();
        owners.add(owner);
        car.setOwners(owners);
        carRepository.update(car);

        // Ищем владельцев по ID автомобиля
        List<Owner> found = ownerRepository.findByCarId(car.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Test Owner");
    }

    @Test
    void whenUpdateOwnerThenChangesSaved() {
        User user = createTestUser("user");

        Owner owner = new Owner();
        owner.setName("Old Name");
        owner.setUser(user);
        ownerRepository.create(owner);

        owner.setName("New Name");
        User newUser = createTestUser("newuser");
        owner.setUser(newUser);
        ownerRepository.update(owner);

        Optional<Owner> updated = ownerRepository.findById(owner.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("New Name");
        assertThat(updated.get().getUser().getId()).isEqualTo(newUser.getId());
    }

    @Test
    void whenDeleteOwnerThenNotFound() {
        User user = createTestUser("user");

        Owner owner = new Owner();
        owner.setName("To Delete");
        owner.setUser(user);
        ownerRepository.create(owner);
        int id = owner.getId();

        ownerRepository.delete(id);

        Optional<Owner> deleted = ownerRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}