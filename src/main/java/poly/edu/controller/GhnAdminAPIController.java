package poly.edu.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import poly.edu.dto.ghn.GhnCreateOrderData;
import poly.edu.service.GhnService;

@RestController
@RequestMapping("/api/admin/shipping/ghn")
public class GhnAdminAPIController {

    @Autowired private GhnService ghnService;

    // FIX: thêm tên tường minh "orderId" vào @PathVariable - bản build hiện tại của project
    // không bật cờ compiler '-parameters', nên Spring không tự đọc được tên tham số qua
    // reflection -> phải khai rõ @PathVariable("orderId") thay vì chỉ @PathVariable.
    @PostMapping("/orders/{orderId}/create")
    public ResponseEntity<?> create(@PathVariable("orderId") Integer orderId,
                                     @RequestBody(required = false) Map<String, Object> body) {
        try {
            Integer cod = body != null && body.get("codAmount") != null ? ((Number) body.get("codAmount")).intValue() : null;
            Integer weight = body != null && body.get("weightGram") != null ? ((Number) body.get("weightGram")).intValue() : null;
            String note = body != null ? (String) body.get("note") : null;
            GhnCreateOrderData data = ghnService.createGhnOrder(orderId, cod, weight, note);
            return ResponseEntity.status(HttpStatus.CREATED).body(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/orders/{orderId}/track")
    public ResponseEntity<?> track(@PathVariable("orderId") Integer orderId) {
        try {
            return ResponseEntity.ok(ghnService.trackGhnOrder(orderId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/orders/{orderId}/simulate-status")
    public ResponseEntity<?> simulateStatus(@PathVariable("orderId") Integer orderId,
                                             @RequestBody Map<String, String> body) {
        try {
            ghnService.handleStatusEvent(orderId, body.get("event"));
            return ResponseEntity.ok("Đã cập nhật trạng thái: " + body.get("event"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}