package ru.job4j.cars.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.job4j.cars.repository.*;

@Configuration
public class RepositoryConfig {

    @Bean
    public CrudRepository crudRepository(SessionFactory sessionFactory) {
        return new CrudRepository(sessionFactory);
    }

    @Bean
    public PostRepository postRepository(CrudRepository crudRepository) {
        return new PostRepository(crudRepository);
    }

    @Bean
    public UserRepository userRepository(CrudRepository crudRepository) {
        return new UserRepository(crudRepository);
    }

    @Bean
    public CarRepository carRepository(CrudRepository crudRepository) {
        return new CarRepository(crudRepository);
    }

    @Bean
    public EngineRepository engineRepository(CrudRepository crudRepository) {
        return new EngineRepository(crudRepository);
    }

    @Bean
    public OwnerRepository ownerRepository(CrudRepository crudRepository) {
        return new OwnerRepository(crudRepository);
    }

    @Bean
    public ParticipatesRepository participatesRepository(CrudRepository crudRepository) {
        return new ParticipatesRepository(crudRepository);
    }

    @Bean
    public PriceHistoryRepository priceHistoryRepository(CrudRepository crudRepository) {
        return new PriceHistoryRepository(crudRepository);
    }
}