package poly.edu.service;

import java.util.List;

import poly.edu.entity.Order;
import poly.edu.entity.Product;

public interface ProductService {

    List<Product> getSuggestProducts();

    List<Product> search(String keyword);

	Product findById(Integer id);
	
	List<Product> findAll();
	
	Product save(Product product);
	
	void deleteById(Integer id);
	
	List<Product> filter(String keyword, Integer categoryId, Double min, Double max);
	
	Product findByName(String name);
	
	
	
}
