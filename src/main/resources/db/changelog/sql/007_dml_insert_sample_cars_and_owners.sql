-- Добавляем двигатели
INSERT INTO engines (name, volume, power) VALUES
('V6 3.5L', 3.5, 249),
('I4 2.0L', 2.0, 150),
('V8 5.7L', 5.7, 345);

-- Добавляем машины
INSERT INTO cars (name, model, manufacture_year, engine_id) VALUES
('Toyota', 'Camry', 2015, 1),
('Honda', 'Civic', 2018, 2),
('Ford', 'Mustang', 2020, 3);

-- Добавляем владельцев
INSERT INTO owners (name, user_id) VALUES
('Иванов Иван Иванович', 1),
('Петров Петр Петрович', 2),
('Сидоров Алексей', 3);

-- Добавляем связи ManyToMany
INSERT INTO history_owner (car_id, owner_id) VALUES
(1, 1), (1, 2),  -- Toyota имеет 2 владельца
(2, 2),          -- Honda имеет 1 владельца
(3, 1), (3, 3);  -- Ford имеет 2 владельца

-- Обновляем объявления с ссылками на машины
UPDATE auto_post SET car_id = 1 WHERE id = 1;
UPDATE auto_post SET car_id = 2 WHERE id = 2;
UPDATE auto_post SET car_id = 3 WHERE id = 3;