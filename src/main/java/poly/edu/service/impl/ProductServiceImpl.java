package poly.edu.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Product> filter(String keyword, Integer categoryId, Double min, Double max) {
        return productRepo.filter(keyword, categoryId, min, max);
    }
}
