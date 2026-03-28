package poly.edu.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.edu.entity.Review;
import poly.edu.repository.ReviewRepository;
import poly.edu.service.ReviewService;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    ReviewRepository reviewRepository;

    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> findByUsername(String username) {
        return reviewRepository.findByUsername(username);
    }
    @Override
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        reviewRepository.deleteById(id);
    }
    @Override
    public List<Review> search(String user, String product) {
        return reviewRepository.search(user, product);
    }
}