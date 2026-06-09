package poly.edu.service;

import java.util.List;
import poly.edu.entity.Review;

public interface ReviewService {

    Review save(Review review);

    List<Review> findByUsername(String username);
    List<Review> findAll();
    void deleteById(Integer id);
    List<Review> search(String user, String product);
    List<Review> findByUserId(Integer userId);
}