package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // đơn đang giao của 1 shipper cụ thể
    List<Order> findByStatusAndShipper_Id(String status, Integer shipperId);

    // lịch sử đơn ĐÃ GIAO / HOÀN THÀNH / RỦI RO của shipper, lọc theo ngày
    @Query("""
        SELECT o FROM Order o
        WHERE o.shipper.id = :shipperId
          AND o.status IN ('DA_GIAO','HOAN_THANH','KHIEU_NAI')
          AND (:from IS NULL OR o.createdDate >= :from)
          AND (:to   IS NULL OR o.createdDate <= :to)
        ORDER BY o.createdDate DESC
    """)
    List<Order> findShipperHistory(
        @Param("shipperId") Integer shipperId,
        @Param("from") java.time.LocalDateTime from,
        @Param("to")   java.time.LocalDateTime to
    );

    List<Order> findByUser_Id(Integer userId);

    // Tổng tiền user đã mua (chỉ tính đơn đã giao) - dùng để xác định khuyến mãi
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId AND o.status = 'DA_GIAO'")
    Double getTotalSpentByUser(@Param("userId") Integer userId);
    
}