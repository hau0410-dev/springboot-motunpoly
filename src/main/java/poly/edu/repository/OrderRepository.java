package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // tìm theo tên
    List<Order> findByFullnameContaining(String fullname);

    // tìm theo trạng thái
    List<Order> findByStatus(String status);

    // tìm theo tên + trạng thái
    List<Order> findByFullnameContainingAndStatus(String fullname, String status);
    
    List<Order> findByShipper_Id(Integer shipperId);
    
    List<Order> findByUser_Id(Integer userId);
    
    
}