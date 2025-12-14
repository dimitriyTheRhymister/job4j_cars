package ru.job4j.cars.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.model.Engine;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EngineRepositoryTest extends RepositoryTestBase {

    private EngineRepository engineRepository;

    @Override
    @BeforeEach
    public void init() {
        super.init();
        engineRepository = new EngineRepository(crudRepository);
        clearDatabase();
    }

    @Test
    void whenCreateEngineThenFindById() {
        Engine engine = new Engine();
        engine.setName("V6 3.0L");
        engine.setVolume(3.0);
        engine.setPower(250);

        engineRepository.create(engine);

        Optional<Engine> found = engineRepository.findById(engine.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("V6 3.0L");
        assertThat(found.get().getVolume()).isEqualTo(3.0);
        assertThat(found.get().getPower()).isEqualTo(250);
    }

    @Test
    void whenFindAllEnginesThenSortedById() {
        Engine engine1 = new Engine();
        engine1.setName("Engine1");
        engine1.setVolume(1.6);
        engine1.setPower(120);
        engineRepository.create(engine1);

        Engine engine2 = new Engine();
        engine2.setName("Engine2");
        engine2.setVolume(2.0);
        engine2.setPower(150);
        engineRepository.create(engine2);

        List<Engine> engines = engineRepository.findAllOrderById();
        assertThat(engines).hasSize(2);
        assertThat(engines.get(0).getId()).isLessThan(engines.get(1).getId());
        assertThat(engines).extracting(Engine::getName)
                .containsExactly("Engine1", "Engine2");
    }

    @Test
    void whenFindByNameThenSuccess() {
        Engine engine = new Engine();
        engine.setName("V8 5.7L");
        engine.setVolume(5.7);
        engine.setPower(345);
        engineRepository.create(engine);

        List<Engine> found = engineRepository.findByName("V8 5.7L");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("V8 5.7L");
    }

    @Test
    void whenFindByVolumeGreaterThanThenSuccess() {
        Engine engine1 = new Engine();
        engine1.setName("Small");
        engine1.setVolume(1.6);
        engine1.setPower(100);
        engineRepository.create(engine1);

        Engine engine2 = new Engine();
        engine2.setName("Medium");
        engine2.setVolume(2.5);
        engine2.setPower(200);
        engineRepository.create(engine2);

        Engine engine3 = new Engine();
        engine3.setName("Large");
        engine3.setVolume(4.0);
        engine3.setPower(300);
        engineRepository.create(engine3);

        List<Engine> found = engineRepository.findByVolumeGreaterThan(2.0);
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Engine::getName)
                .containsExactlyInAnyOrder("Medium", "Large");
    }

    @Test
    void whenFindByPowerBetweenThenSuccess() {
        Engine engine1 = new Engine();
        engine1.setName("Weak");
        engine1.setVolume(1.6);
        engine1.setPower(100);
        engineRepository.create(engine1);

        Engine engine2 = new Engine();
        engine2.setName("Medium");
        engine2.setVolume(2.0);
        engine2.setPower(150);
        engineRepository.create(engine2);

        Engine engine3 = new Engine();
        engine3.setName("Strong");
        engine3.setVolume(3.0);
        engine3.setPower(250);
        engineRepository.create(engine3);

        List<Engine> found = engineRepository.findByPowerBetween(120, 200);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Medium");
    }

    @Test
    void whenUpdateEngineThenChangesSaved() {
        Engine engine = new Engine();
        engine.setName("OldName");
        engine.setVolume(2.0);
        engine.setPower(150);
        engineRepository.create(engine);

        engine.setName("NewName");
        engine.setVolume(2.5);
        engine.setPower(180);
        engineRepository.update(engine);

        Optional<Engine> updated = engineRepository.findById(engine.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("NewName");
        assertThat(updated.get().getVolume()).isEqualTo(2.5);
        assertThat(updated.get().getPower()).isEqualTo(180);
    }

    @Test
    void whenDeleteEngineThenNotFound() {
        Engine engine = new Engine();
        engine.setName("ToDelete");
        engine.setVolume(2.0);
        engine.setPower(150);
        engineRepository.create(engine);
        int id = engine.getId();

        engineRepository.delete(id);

        Optional<Engine> deleted = engineRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}