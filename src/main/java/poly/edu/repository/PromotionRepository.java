package poly.edu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.edu.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    List<Promotion> findByActiveTrue();
}