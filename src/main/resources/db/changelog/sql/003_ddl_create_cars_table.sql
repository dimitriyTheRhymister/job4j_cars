-- Создание таблицы автомобилей (дополнительная)
CREATE TABLE cars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    production_year INT,
    price DECIMAL(10,2),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE cars IS 'Таблица автомобилей';
COMMENT ON COLUMN cars.brand IS 'Марка автомобиля';
COMMENT ON COLUMN cars.model IS 'Модель автомобиля';
COMMENT ON COLUMN cars.production_year IS 'Год выпуска';
COMMENT ON COLUMN cars.price IS 'Цена';