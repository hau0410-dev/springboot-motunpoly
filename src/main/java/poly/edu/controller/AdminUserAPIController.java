package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.User;
import poly.edu.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserAPIController {

    @Autowired
    private UserRepository userRepo;

    // ===== 1. DANH SÁCH =====
    @GetMapping
    public List<User> list() {
        return userRepo.findAll();
    }

    // ===== 2. CHI TIẾT =====
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable("id") Integer id) {
        return userRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ===== 3. CREATE =====
    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {

        // FIX: username là UNIQUE trong DB (bảng users) -> kiểm tra trước để trả lỗi rõ ràng (400)
        // thay vì để Postman nhận 500 khi SQL Server báo vi phạm unique constraint.
        if (user.getUsername() != null && userRepo.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Username '" + user.getUsername() + "' đã tồn tại");
        }

        if (user.getEnabled() == null) {
            user.setEnabled(true); // FIX: tránh lưu NULL nếu Postman quên gửi field enabled
        }

        try {
            User saved = userRepo.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Không thể tạo user: dữ liệu vi phạm ràng buộc (username/email trùng, thiếu field NOT NULL...)");
        }
    }

    // ===== 4. UPDATE =====
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Integer id, @RequestBody User user) {
        User old = userRepo.findById(id).orElse(null);

        if (old == null) {
            return ResponseEntity.notFound().build();
        }

        user.setId(id);

        // FIX: nếu Postman không gửi password mới thì giữ lại password cũ (tránh set null)
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(old.getPassword());
        }

        try {
            return ResponseEntity.ok(userRepo.save(user));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Không thể cập nhật: dữ liệu vi phạm ràng buộc (username/email trùng...)");
        }
    }

    // ===== 5. DELETE =====
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id) {

        if (!userRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            userRepo.deleteById(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (DataIntegrityViolationException e) {
            // FIX: user đang được orders/reviews/return_orders tham chiếu (FK không CASCADE)
            // -> SQL Server chặn xoá. Trả 409 kèm lý do thay vì HTML lỗi 500.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xoá user id=" + id + ": user đang có đơn hàng/đánh giá liên quan (FK constraint).");
        }
    }
}