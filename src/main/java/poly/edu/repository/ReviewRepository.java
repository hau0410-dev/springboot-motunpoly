package poly.edu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByUsername(String username);

    @Query("""
            SELECT r FROM Review r
            WHERE (:user IS NULL OR r.fullname LIKE %:user%)
            AND (:product IS NULL OR r.productName LIKE %:product%)
        """)
    List<Review> search(
            @Param("user") String user,
            @Param("product") String product
    );
    List<Review> findByUser_Id(Integer userId);
}