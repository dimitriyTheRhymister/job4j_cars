package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import ru.job4j.cars.model.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class PostRepository {
    private final CrudRepository crudRepository;

    public Post save(Post post) {
        crudRepository.run(session -> session.save(post));
        return post;
    }

    public void update(Post post) {
        crudRepository.run(session -> session.update(post));
    }

    public void delete(int id) {
        crudRepository.run(
                "DELETE FROM Post WHERE id = :id",
                Map.of("id", id)
        );
    }

    public Optional<Post> findById(int id) {
        return crudRepository.optional(
                "SELECT DISTINCT p FROM Post p "
                        + "LEFT JOIN FETCH p.user "
                        + "LEFT JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH c.engine "
                        + "LEFT JOIN FETCH p.photoUrls "  // ДОБАВЛЕНО
                        + "WHERE p.id = :id",
                Post.class,
                Map.of("id", id)
        );
    }

    public List<Post> findAll() {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "LEFT JOIN FETCH p.user "
                        + "LEFT JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH c.engine "
                        + "LEFT JOIN FETCH p.photoUrls "  // ДОБАВЛЕНО
                        + "ORDER BY p.created DESC",
                Post.class
        );
    }

    public List<Post> findActive() {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "LEFT JOIN FETCH p.user "
                        + "LEFT JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH c.engine "
                        + "LEFT JOIN FETCH p.photoUrls "  // ДОБАВЛЕНО
                        + "WHERE p.status = 'ACTIVE' "
                        + "ORDER BY p.created DESC",
                Post.class
        );
    }

    public List<Post> findByUserId(int userId) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "LEFT JOIN FETCH p.user "
                        + "LEFT JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH c.engine "
                        + "LEFT JOIN FETCH p.photoUrls "  // ДОБАВЛЕНО
                        + "WHERE p.user.id = :userId "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("userId", userId)
        );
    }

    public List<String> findDistinctBrands() {
        return crudRepository.query(
                "SELECT DISTINCT c.brand FROM Car c ORDER BY c.brand",
                String.class
        );
    }

    public List<String> findDistinctBodyTypes() {
        return crudRepository.query(
                "SELECT DISTINCT p.bodyType FROM Post p WHERE p.bodyType IS NOT NULL ORDER BY p.bodyType",
                String.class
        );
    }

    public void updateStatus(int postId, Post.PostStatus status, int userId) {
        crudRepository.run(
                "UPDATE Post p SET p.status = :status WHERE p.id = :postId AND p.user.id = :userId",
                Map.of("status", status, "postId", postId, "userId", userId)
        );
    }

    public List<String> getPhotosByPostId(int postId) {
        return crudRepository.tx(session -> {
            Post post = session.find(Post.class, postId);
            if (post != null) {
                Hibernate.initialize(post.getPhotoUrls());
                return post.getPhotoUrls();
            }
            return List.of();
        });
    }

    // В PostRepository.java добавляем:
    public List<Post> findByStatus(Post.PostStatus status) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "LEFT JOIN FETCH p.user "
                        + "LEFT JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH c.engine "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.status = :status "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("status", status)
        );
    }

    public List<Post> findByUserIdAndStatus(int userId, Post.PostStatus status) {
        return crudRepository.query(
                "SELECT DISTINCT p FROM Post p "
                        + "LEFT JOIN FETCH p.user "
                        + "LEFT JOIN FETCH p.car c "
                        + "LEFT JOIN FETCH c.engine "
                        + "LEFT JOIN FETCH p.photoUrls "
                        + "WHERE p.user.id = :userId AND p.status = :status "
                        + "ORDER BY p.created DESC",
                Post.class,
                Map.of("userId", userId, "status", status)
        );
    }
}