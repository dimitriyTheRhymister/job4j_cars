package ru.job4j.cars.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.model.Car;
import ru.job4j.cars.model.Engine;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CarRepositoryTest extends RepositoryTestBase {

    private CarRepository carRepository;
    private EngineRepository engineRepository;

    @Override
    @BeforeEach
    public void init() {
        super.init();
        engineRepository = new EngineRepository(crudRepository);
        carRepository = new CarRepository(crudRepository);
        clearDatabase();
    }

    private Engine createTestEngine() {
        Engine engine = new Engine();
        engine.setName("Test Engine");
        engine.setVolume(2.0);
        engine.setPower(150);
        return engineRepository.create(engine);
    }

    @Test
    void whenCreateCarThenFindById() {
        Engine engine = createTestEngine();

        Car car = new Car();
        car.setName("Toyota");
        car.setModel("Camry");
        car.setManufactureYear(2020);
        car.setEngine(engine);

        carRepository.create(car);

        Optional<Car> found = carRepository.findById(car.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Toyota");
        assertThat(found.get().getModel()).isEqualTo("Camry");
        assertThat(found.get().getManufactureYear()).isEqualTo(2020);
        assertThat(found.get().getEngine().getId()).isEqualTo(engine.getId());
    }

    @Test
    void whenFindAllCarsThenSortedById() {
        Engine engine = createTestEngine();

        Car car1 = new Car();
        car1.setName("Car1");
        car1.setModel("Model1");
        car1.setManufactureYear(2020);
        car1.setEngine(engine);
        carRepository.create(car1);

        Car car2 = new Car();
        car2.setName("Car2");
        car2.setModel("Model2");
        car2.setManufactureYear(2021);
        car2.setEngine(engine);
        carRepository.create(car2);

        List<Car> cars = carRepository.findAllOrderById();
        assertThat(cars).hasSize(2);
        assertThat(cars.get(0).getId()).isLessThan(cars.get(1).getId());
        assertThat(cars).extracting(Car::getName)
                .containsExactly("Car1", "Car2");
    }

    @Test
    void whenFindByNameThenSuccess() {
        Engine engine = createTestEngine();

        Car car1 = new Car();
        car1.setName("Toyota");
        car1.setModel("Camry");
        car1.setManufactureYear(2020);
        car1.setEngine(engine);
        carRepository.create(car1);

        Car car2 = new Car();
        car2.setName("Toyota");
        car2.setModel("Corolla");
        car2.setManufactureYear(2021);
        car2.setEngine(engine);
        carRepository.create(car2);

        List<Car> found = carRepository.findByName("Toyota");
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Car::getModel)
                .containsExactlyInAnyOrder("Camry", "Corolla");
    }

    @Test
    void whenFindByManufactureYearThenSuccess() {
        Engine engine = createTestEngine();

        Car car1 = new Car();
        car1.setName("Car1");
        car1.setModel("Model1");
        car1.setManufactureYear(2020);
        car1.setEngine(engine);
        carRepository.create(car1);

        Car car2 = new Car();
        car2.setName("Car2");
        car2.setModel("Model2");
        car2.setManufactureYear(2021);
        car2.setEngine(engine);
        carRepository.create(car2);

        List<Car> found = carRepository.findByManufactureYear(2020);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Car1");
    }

    @Test
    void whenFindByEnginePowerGreaterThanThenSuccess() {
        Engine engine1 = new Engine();
        engine1.setName("Weak");
        engine1.setVolume(1.6);
        engine1.setPower(100);
        engineRepository.create(engine1);

        Engine engine2 = new Engine();
        engine2.setName("Strong");
        engine2.setVolume(3.0);
        engine2.setPower(250);
        engineRepository.create(engine2);

        Car car1 = new Car();
        car1.setName("Car1");
        car1.setModel("Model1");
        car1.setManufactureYear(2020);
        car1.setEngine(engine1);
        carRepository.create(car1);

        Car car2 = new Car();
        car2.setName("Car2");
        car2.setModel("Model2");
        car2.setManufactureYear(2021);
        car2.setEngine(engine2);
        carRepository.create(car2);

        List<Car> found = carRepository.findByEnginePowerGreaterThan(150);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Car2");
    }

    @Test
    void whenUpdateCarThenChangesSaved() {
        Engine engine = createTestEngine();

        Car car = new Car();
        car.setName("OldName");
        car.setModel("OldModel");
        car.setManufactureYear(2019);
        car.setEngine(engine);
        carRepository.create(car);

        car.setName("NewName");
        car.setModel("NewModel");
        car.setManufactureYear(2022);
        carRepository.update(car);

        Optional<Car> updated = carRepository.findById(car.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("NewName");
        assertThat(updated.get().getModel()).isEqualTo("NewModel");
        assertThat(updated.get().getManufactureYear()).isEqualTo(2022);
    }

    @Test
    void whenDeleteCarThenNotFound() {
        Engine engine = createTestEngine();

        Car car = new Car();
        car.setName("ToDelete");
        car.setModel("Model");
        car.setManufactureYear(2020);
        car.setEngine(engine);
        carRepository.create(car);
        int id = car.getId();

        carRepository.delete(id);

        Optional<Car> deleted = carRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}