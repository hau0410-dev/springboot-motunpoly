package poly.edu.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByIsActiveTrue();
}
