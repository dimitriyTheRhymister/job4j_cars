-- Добавление тестовых пользователей
INSERT INTO auto_user (login, password) VALUES
('ivanov', 'password123'),
('petrov', 'qwerty456'),
('sidorova', 'secret789');

-- Комментарий
COMMENT ON TABLE auto_user IS 'Тестовые пользователи добавлены';