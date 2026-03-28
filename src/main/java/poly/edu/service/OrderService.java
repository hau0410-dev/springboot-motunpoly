package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.entity.Order;
import poly.edu.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    OrderRepository repo;

    public void save(Order order) {
        order.setCreatedDate(LocalDateTime.now());
        repo.save(order);
    }

    public List<Order> findAll() {
        return repo.findAll();
    }

    public Order findById(Integer id) {
        return repo.findById(id).orElse(null);
    }
    
    public List<Order> search(String name, String status){

        if(name != null && !name.isEmpty() && status != null && !status.isEmpty()){
            return repo.findByFullnameContainingAndStatus(name, status);
        }

        if(name != null && !name.isEmpty()){
            return repo.findByFullnameContaining(name);
        }

        if(status != null && !status.isEmpty()){
            return repo.findByStatus(status);
        }

        return repo.findAll();
    }
}
