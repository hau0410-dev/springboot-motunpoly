package poly.edu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.edu.entity.ReturnOrder;

public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Integer> {

    ReturnOrder findByOrder_Id(Integer orderId);

    List<ReturnOrder> findByUser_IdOrderByIdDesc(Integer userId);

    List<ReturnOrder> findByStatusOrderByIdDesc(String status);

    List<ReturnOrder> findAllByOrderByIdDesc();

    // Đơn hoàn đã được admin duyệt, đang chờ shipper nhận (chưa có ai nhận)
    List<ReturnOrder> findByStatusAndShipperIsNullOrderByIdDesc(String status);

    // Đơn hoàn mà 1 shipper cụ thể đang xử lý
    List<ReturnOrder> findByStatusAndShipper_IdOrderByIdDesc(String status, Integer shipperId);
}