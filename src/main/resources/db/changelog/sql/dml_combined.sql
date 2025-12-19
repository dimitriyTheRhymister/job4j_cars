-- ============================================
-- DML: Тестовые данные для базы данных cars_db
-- Порядок: соблюдение внешних ключей
-- ============================================

-- 1. Пользователи (добавляем сразу с правильными паролями)
INSERT INTO auto_user (login, password) VALUES
('ivanov', 'root'),
('petrov', 'root'),
('sidorova', 'root'),
('Ivanov', 'root'),
('Petrov', 'root'),
('Sidorov', 'root');

-- 2. Двигатели
INSERT INTO engines (name, volume, power) VALUES
('V6 3.5L', 3.5, 249),
('I4 2.0L', 2.0, 150),
('V8 5.7L', 5.7, 345),
('Бензиновый 1.6', 1.6, 120),
('Бензиновый 2.0', 2.0, 150),
('Дизельный 2.0', 2.0, 140);

-- 3. Автомобили
INSERT INTO cars (name, model, manufacture_year, engine_id) VALUES
('Toyota', 'Camry', 2015, 1),
('Honda', 'Civic', 2018, 2),
('Ford', 'Mustang', 2020, 3),
('Toyota', 'Corolla', 2021, 4),
('Honda', 'Accord', 2019, 5),
('Ford', 'Focus', 2017, 6);

-- 4. Объявления (ДОБАВЛЕНЫ НОВЫЕ ПОЛЯ)
INSERT INTO auto_post (description, auto_user_id, car_id, currentprice,
                       body_type, engine_type, transmission, mileage, color, status) VALUES
('Продам Toyota Camry 2022 года, в отличном состоянии, пробег 30 000 км.',
 1, 1, 1500000, 'Седан', 'Бензиновый', 'Автоматическая', 30000, 'Черный', 'ACTIVE'),

('Продам Honda Civic 2021 года, полный комплект, сервисная история.',
 2, 2, 1200000, 'Седан', 'Бензиновый', 'Механическая', 25000, 'Белый', 'ACTIVE'),

('Продам BMW X5 2023 года, премиум комплектация, гарантия дилера.',
 1, 3, 3500000, 'Внедорожник', 'Бензиновый', 'Автоматическая', 15000, 'Синий', 'ACTIVE'),

('Продам Lada Vesta 2020 года, экономичный расход, идеально для города.',
 3, 4, 800000, 'Седан', 'Бензиновый', 'Механическая', 45000, 'Серебристый', 'ACTIVE'),

('Продам Toyota Corolla 2021 года, идеальное состояние.',
 4, 5, 1300000, 'Седан', 'Бензиновый', 'Автоматическая', 20000, 'Красный', 'ACTIVE'),

('Продам Ford Focus 2017 года, без ДТП.',
 5, 6, 700000, 'Хэтчбек', 'Бензиновый', 'Механическая', 60000, 'Серый', 'ACTIVE');

-- 5. Владельцы
INSERT INTO owners (name, user_id) VALUES
('Иванов Иван Иванович', 1),
('Петров Петр Петрович', 2),
('Сидоров Алексей', 3);

-- 6. История владения (many-to-many)
INSERT INTO history_owner (car_id, owner_id) VALUES
(1, 1), (1, 2),  -- Toyota имеет 2 владельца
(2, 2),          -- Honda имеет 1 владельца
(3, 1), (3, 3);  -- Ford имеет 2 владельца

-- 7. История цен (пример)
INSERT INTO price_history (before, after, post_id) VALUES
(1500000, 1450000, 1),
(2000000, 1950000, 2),
(3500000, 3400000, 3);

-- 8. Фотографии (пример)
INSERT INTO post_photos (post_id, photo_url) VALUES
(1, '/uploads/1.jpg'),
(2, '/uploads/2.jpg'),
(3, '/uploads/3.jpg'),
(4, '/uploads/4.jpg'),
(5, '/uploads/5.jpg'),
(6, '/uploads/6.jpg');

-- 9. Подписки/участие (пример)
INSERT INTO participates (user_id, post_id) VALUES
(2, 1),  -- petrov подписан на объявление 1
(3, 1),  -- sidorova подписана на объявление 1
(1, 2);  -- ivanov подписан на объявление 2