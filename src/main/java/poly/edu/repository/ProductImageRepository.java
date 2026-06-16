package poly.edu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.edu.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProduct_IdOrderBySortOrderAsc(Integer productId);

    void deleteByProduct_Id(Integer productId);
}