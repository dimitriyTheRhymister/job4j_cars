-- Создание таблицы объявлений
CREATE TABLE auto_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    auto_user_id BIGINT NOT NULL,
    CONSTRAINT fk_auto_post_user
        FOREIGN KEY (auto_user_id) REFERENCES auto_user(id)
);

-- Комментарии
COMMENT ON TABLE auto_post IS 'Таблица объявлений о продаже автомобилей';
COMMENT ON COLUMN auto_post.description IS 'Описание автомобиля';
COMMENT ON COLUMN auto_post.created IS 'Дата создания объявления';
COMMENT ON COLUMN auto_post.auto_user_id IS 'Ссылка на пользователя-автора';