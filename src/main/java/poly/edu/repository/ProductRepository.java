package poly.edu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import poly.edu.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findTop12ByActiveTrueOrderByIdDesc();

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    @Query("""
    	    SELECT p FROM Product p
    	    WHERE p.active = true
    	    ORDER BY p.id DESC
    	""")
    	Page<Product> findSuggestProducts(Pageable pageable);
    
    @Query("""
    		SELECT p FROM Product p
    		WHERE
    		(:keyword IS NULL OR p.name LIKE %:keyword%)
    		AND (:categoryId IS NULL OR p.category.id = :categoryId)
    		AND (:min IS NULL OR p.price >= :min)
    		AND (:max IS NULL OR p.price <= :max)
    		""")
    		Page<Product> filter(
    		        @Param("keyword") String keyword,
    		        @Param("categoryId") Integer categoryId,
    		        @Param("min") Double min,
    		        @Param("max") Double max,
    		        Pageable pageable
    		);
    Product findByName(String name);
}
