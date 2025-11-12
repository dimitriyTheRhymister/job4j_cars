package ru.cars;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")  // Используем тестовый профиль
class Job4jCarsApplicationTest {

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
/*Тест проверяет, что контекст Spring загружается*/