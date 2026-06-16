package poly.edu.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import poly.edu.entity.Product;
import poly.edu.repository.ProductRepository;
import poly.edu.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    // ===== Constructor Injection =====
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    @Autowired
    ProductRepository productRepo;

    @Override
    public List<Product> findAll() {
        return productRepo.findAll();
    }

    @Override
    public Product findById(Integer id) {
        return productRepo.findById(id).orElse(null);
    }

    @Override
    public Product save(Product product) {
        return productRepo.save(product);
    }
    


    // ===== Getter / Setter =====
    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ===== Business Logic =====
    @Override
    public List<Product> getSuggestProducts() {
        return productRepository.findTop12ByActiveTrueOrderByIdDesc();
    }

    @Override
    public List<Product> search(String keyword) {
        return productRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(keyword);
    }
    @Override
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }
    @Override
    public Page<Product> filter(String keyword, Integer categoryId, Double min, Double max,
            String brand, String vehicleType, String partsBrand, Pageable pageable) {
        return productRepository.filter(keyword, categoryId, min, max, brand, vehicleType, partsBrand, pageable);
    }
    
    @Override
    public Product findByName(String name) {
        return productRepo.findByName(name);
    }
    @Override
    public Page<Product> getSuggestProducts(Pageable pageable) {
        return productRepository.findSuggestProducts(pageable);
    }
}