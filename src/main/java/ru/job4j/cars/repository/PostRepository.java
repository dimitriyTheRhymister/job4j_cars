package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import ru.job4j.cars.model.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class PostRepository {
    private final CrudRepository crudRepository;

    /**
     * Сохранить в базе.
     * @param post объявление.
     * @return объявление с id.
     */
    public Post create(Post post) {
        crudRepository.run(session -> session.persist(post));
        return post;
    }

    /**
     * Обновить в базе объявление.
     * @param post объявление.
     */
    public void update(Post post) {
        crudRepository.run(session -> session.merge(post));
    }

    /**
     * Удалить объявление по id.
     * @param postId ID
     */
    public void delete(int postId) {
        crudRepository.run(
                "DELETE FROM Post WHERE id = :fId",
                Map.of("fId", postId)
        );
    }

    /**
     * Найти объявление по ID
     * @param postId ID
     * @return объявление.
     */
    public Optional<Post> findById(int postId) {
        return crudRepository.optional(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.id = :fId",
                Post.class,
                Map.of("fId", postId)
        );
    }

    /**
     * Список всех объявлений отсортированных по id.
     * @return список объявлений.
     */
    public List<Post> findAllOrderById() {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "ORDER BY p.id ASC",
                Post.class
        );
    }

    /**
     * Список всех объявлений отсортированных по дате (новые сначала).
     * @return список объявлений.
     */
    public List<Post> findAllOrderByCreatedDesc() {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "ORDER BY p.created DESC",
                Post.class
        );
    }

    /**
     * Показать объявления за последний день.
     * @return список объявлений за последние 24 часа.
     */
    public List<Post> findPostsFromLastDay() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.created >= :fYesterday "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fYesterday", yesterday)
        );
    }

    /**
     * Показать объявления за последние N дней.
     * @param days количество дней
     * @return список объявлений
     */
    public List<Post> findPostsFromLastDays(int days) {
        LocalDateTime dateFrom = LocalDateTime.now().minusDays(days);
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.created >= :fDateFrom "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fDateFrom", dateFrom)
        );
    }

    /**
     * Показать объявления с фото.
     * Ищет объявления, у которых есть хотя бы одно фото в списке photoUrls.
     * @return список объявлений с фото.
     */
    public List<Post> findPostsWithPhotos() {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE SIZE(p.photoUrls) > 0 "
                        + "ORDER BY p.created DESC",
                Post.class
        );
    }

    /**
     * Показать объявления БЕЗ фото.
     * @return список объявлений без фото.
     */
    public List<Post> findPostsWithoutPhotos() {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE SIZE(p.photoUrls) = 0 "
                        + "ORDER BY p.created DESC",
                Post.class
        );
    }

    /**
     * Показать объявления определенной марки автомобиля.
     * @param brand марка автомобиля (например, "Toyota").
     * @return список объявлений с указанной маркой.
     */
    public List<Post> findPostsByCarBrand(String brand) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE UPPER(c.name) = UPPER(:fBrand) "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fBrand", brand)
        );
    }

    /**
     * Показать объявления определенной модели автомобиля.
     * @param model модель автомобиля (например, "Camry").
     * @return список объявлений с указанной моделью.
     */
    public List<Post> findPostsByCarModel(String model) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE UPPER(c.model) = UPPER(:fModel) "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fModel", model)
        );
    }

    /**
     * Показать объявления по марке и модели.
     * @param brand марка автомобиля
     * @param model модель автомобиля
     * @return список объявлений
     */
    public List<Post> findPostsByBrandAndModel(String brand, String model) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE UPPER(c.name) = UPPER(:fBrand) AND UPPER(c.model) = UPPER(:fModel) "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fBrand", brand, "fModel", model)
        );
    }

    /**
     * Показать объявления по ID пользователя.
     * @param userId ID пользователя.
     * @return список объявлений пользователя.
     */
    public List<Post> findPostsByUserId(int userId) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.user.id = :fUserId "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fUserId", userId)
        );
    }

    /**
     * Показать объявления с ценой в диапазоне.
     * @param minPrice минимальная цена.
     * @param maxPrice максимальная цена.
     * @return список объявлений.
     */
    public List<Post> findPostsByPriceRange(Long minPrice, Long maxPrice) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.currentPrice BETWEEN :fMinPrice AND :fMaxPrice "
                        + "ORDER BY p.currentPrice ASC",
                Post.class,
                Map.of("fMinPrice", minPrice, "fMaxPrice", maxPrice)
        );
    }

    /**
     * Показать объявления дешевле указанной цены.
     * @param maxPrice максимальная цена.
     * @return список объявлений.
     */
    public List<Post> findPostsCheaperThan(Long maxPrice) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.currentPrice <= :fMaxPrice "
                        + "ORDER BY p.currentPrice ASC",
                Post.class,
                Map.of("fMaxPrice", maxPrice)
        );
    }

    /**
     * Показать объявления дороже указанной цены.
     * @param minPrice минимальная цена.
     * @return список объявлений.
     */
    public List<Post> findPostsMoreExpensiveThan(Long minPrice) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.currentPrice >= :fMinPrice "
                        + "ORDER BY p.currentPrice DESC",
                Post.class,
                Map.of("fMinPrice", minPrice)
        );
    }

    /**
     * Поиск объявлений по ключевым словам в описании.
     * @param keyword ключевое слово.
     * @return список объявлений.
     */
    public List<Post> searchByKeyword(String keyword) {
        String searchPattern = "%"
                + "%";
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE LOWER(p.description) LIKE :fKeyword "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fKeyword", searchPattern)
        );
    }

    /**
     * Показать объявления с определенным годом выпуска автомобиля.
     * @param year год выпуска.
     * @return список объявлений.
     */
    public List<Post> findPostsByCarYear(int year) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE c.manufactureYear = :fYear "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fYear", year)
        );
    }

    /**
     * Показать объявления с автомобилями новее указанного года.
     * @param minYear минимальный год выпуска.
     * @return список объявлений.
     */
    public List<Post> findPostsByCarYearGreaterThan(int minYear) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE c.manufactureYear >= :fMinYear "
                        + "ORDER BY c.manufactureYear DESC, p.created DESC",
                Post.class,
                Map.of("fMinYear", minYear)
        );
    }

    /**
     * Показать объявления за последний час (для тестирования).
     * @return список объявлений за последний час.
     */
    public List<Post> findPostsFromLastHour() {
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.created >= :fHourAgo "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fHourAgo", hourAgo)
        );
    }

    /**
     * Показать объявления с фото определенного пользователя.
     * @param userId ID пользователя
     * @return список объявлений с фото
     */
    public List<Post> findPostsWithPhotosByUserId(int userId) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "JOIN FETCH p.user "
                        + "JOIN FETCH p.car "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE SIZE(p.photoUrls) > 0 AND p.user.id = :fUserId "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("fUserId", userId)
        );
    }

    /**
     * Получить количество объявлений с фото.
     * @return количество объявлений с фото.
     */
    public long countPostsWithPhotos() {
        return crudRepository.query(
                "SELECT COUNT(DISTINCT p) FROM Post p WHERE SIZE(p.photoUrls) > 0",
                Long.class
        ).get(0);
    }

    /**
     * Получить количество объявлений без фото.
     * @return количество объявлений без фото.
     */
    public long countPostsWithoutPhotos() {
        return crudRepository.query(
                "SELECT COUNT(DISTINCT p) FROM Post p WHERE SIZE(p.photoUrls) = 0",
                Long.class
        ).get(0);
    }

    /**
     * Получить общее количество объявлений.
     * @return общее количество объявлений.
     */
    public long countAllPosts() {
        return crudRepository.query(
                "SELECT COUNT(p) FROM Post p",
                Long.class
        ).get(0);
    }

    /**
     * Получить количество объявлений пользователя.
     * @param userId ID пользователя
     * @return количество объявлений
     */
    public long countPostsByUserId(int userId) {
        return crudRepository.query(
                "SELECT COUNT(p) FROM Post p WHERE p.user.id = :fUserId",
                Long.class,
                Map.of("fUserId", userId)
        ).get(0);
    }

    /**
     * Получить количество объявлений с фото у пользователя.
     * @param userId ID пользователя
     * @return количество объявлений с фото
     */
    public long countPostsWithPhotosByUserId(int userId) {
        return crudRepository.query(
                "SELECT COUNT(DISTINCT p) FROM Post p WHERE SIZE(p.photoUrls) > 0 AND p.user.id = :fUserId",
                Long.class,
                Map.of("fUserId", userId)
        ).get(0);
    }
}