package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Payment;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.PaymentRepository;
import poly.edu.service.OrderService;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderAPIController {

	@Autowired
	private OrderRepository orderRepo;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemRespository orderItemRepo;

    // FIX: cần thêm để dọn payment trước khi xoá order (payments.order_id có FK không CASCADE)
    @Autowired
    private PaymentRepository paymentRepo;

    // ===== DANH SÁCH =====
    @GetMapping
    public List<Order> list(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "status", required = false) String status) {

        return orderService.search(name, status);
    }

    // ===== CHI TIẾT =====
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable("id") Integer id) {
        Order order = orderService.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    // ===== LẤY ITEM =====
    @GetMapping("/{id}/items")
    public List<OrderItem> getItems(@PathVariable("id") Integer id) {
        return orderItemRepo.findByOrderId(id);
    }

    // ===== UPDATE STATUS =====
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Integer id) {

        Order order = orderService.findById(id);

        if (order == null) {
            return ResponseEntity.notFound().build(); // FIX: trước đây trả về null (200 rỗng), giờ trả 404 đúng nghĩa
        }

        if ("ĐANG_GIAO".equals(order.getStatus())) {
            order.setStatus("DA_GIAO");
        } else {
            order.setStatus("ĐANG_GIAO");
        }

        orderService.save(order);

        return ResponseEntity.ok(order);
    }

    // ===== CREATE =====
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Order order) {
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus("ĐANG_GIAO"); // FIX: giá trị mặc định hợp lý nếu Postman không gửi status
        }
        try {
            orderService.save(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Không thể tạo đơn hàng: user_id/shipper_id không tồn tại hoặc dữ liệu không hợp lệ.");
        }
    }

    // ===== DELETE =====
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id) {

        if (!orderRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            // FIX: order_items.order_id và payments.order_id đều là FOREIGN KEY KHÔNG có
            // ON DELETE CASCADE trong DB -> phải xoá con trước, nếu không SQL Server sẽ chặn
            // và trả lỗi 500 khi gọi orderRepo.deleteById() trực tiếp như code cũ.
            List<OrderItem> items = orderItemRepo.findByOrderId(id);
            orderItemRepo.deleteAll(items);

            Payment payment = paymentRepo.findByOrder_Id(id);
            if (payment != null) {
                paymentRepo.delete(payment);
            }

            orderRepo.deleteById(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (DataIntegrityViolationException e) {
            // Trường hợp còn bị return_orders tham chiếu tới order này
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xoá đơn hàng id=" + id + ": vẫn còn dữ liệu liên quan (yêu cầu hoàn trả...).");
        }
    }
}