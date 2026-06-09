package poly.edu.service;

import java.util.List;

import poly.edu.entity.Order;
import poly.edu.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService {

    List<Product> getSuggestProducts();

    List<Product> search(String keyword);

	Product findById(Integer id);
	
	List<Product> findAll();
	
	Product save(Product product);
	
	void deleteById(Integer id);
	
	
	
	Product findByName(String name);
	
	Page<Product> filter(String keyword, Integer categoryId, Double min, Double max, Pageable pageable);
	
	Page<Product> getSuggestProducts(Pageable pageable);

	
	
	
}
