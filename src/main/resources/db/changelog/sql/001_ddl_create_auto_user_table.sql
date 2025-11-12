-- Создание таблицы пользователей
CREATE TABLE auto_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE auto_user IS 'Таблица пользователей автосайта';
COMMENT ON COLUMN auto_user.login IS 'Уникальный логин пользователя';
COMMENT ON COLUMN auto_user.password IS 'Пароль пользователя';