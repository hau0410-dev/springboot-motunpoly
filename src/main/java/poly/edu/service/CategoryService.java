package poly.edu.service;
import poly.edu.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getActiveCategories();
    List<Category> findAll();  
    List<Category> findByType(String type);
    Category findById(Integer id);
    Category save(Category category);
    void delete(Integer id);
}