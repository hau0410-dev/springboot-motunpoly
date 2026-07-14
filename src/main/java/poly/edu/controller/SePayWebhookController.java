package poly.edu.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import poly.edu.dto.SePayWebhookPayload;
import poly.edu.entity.Order;
import poly.edu.entity.Payment;
import poly.edu.repository.OrderRepository;
import poly.edu.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
public class SePayWebhookController {
	
    private static final Logger log = LoggerFactory.getLogger(SePayWebhookController.class);

    // Khớp nội dung chuyển khoản dạng "DH105 ..." -> lấy ra orderId = 105
    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("DH(\\d+)", Pattern.CASE_INSENSITIVE);

    @Value("${sepay.api-key}")
    private String sepayApiKey;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/sepay-webhook")
    
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody SePayWebhookPayload payload) {

        // ===== 1. XÁC THỰC API KEY =====
    	
        String expected = "Apikey " + sepayApiKey;
        log.warn("DEBUG - Header nhận được: [{}]", authorization);
        log.warn("DEBUG - Kỳ vọng: [{}]", expected);
        if (authorization == null || !authorization.equals(expected)) {
            log.warn("SePay webhook: Unauthorized - header nhận được = {}", authorization);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response(false, "Unauthorized"));
        }

        log.info("SePay webhook nhận được: id={}, content={}, amount={}",
                payload.getId(), payload.getContent(), payload.getTransferAmount());

        // ===== 2. CHỈ XỬ LÝ GIAO DỊCH TIỀN VÀO =====
        if (payload.getTransferType() == null || !payload.getTransferType().equalsIgnoreCase("in")) {
            return ResponseEntity.ok(response(true, "Ignored (not an incoming transaction)"));
        }

        // ===== 3. CHỐNG XỬ LÝ TRÙNG (SePay có thể gửi lại cùng 1 giao dịch) =====
        String transactionId = String.valueOf(payload.getId());
        if (paymentService.findByTransactionId(transactionId) != null) {
            log.info("SePay webhook: giao dịch {} đã được xử lý trước đó, bỏ qua.", transactionId);
            return ResponseEntity.ok(response(true, "Already processed"));
        }

        // ===== 4. TÁCH MÃ ĐƠN HÀNG TỪ NỘI DUNG CHUYỂN KHOẢN =====
        String content = payload.getContent() == null ? "" : payload.getContent();
        Matcher matcher = ORDER_CODE_PATTERN.matcher(content);

        if (!matcher.find()) {
            log.warn("SePay webhook: không tìm thấy mã đơn hàng trong nội dung '{}'", content);
            return ResponseEntity.ok(response(true, "No matching order code"));
        }

        Integer orderId = Integer.parseInt(matcher.group(1));
        Order order = orderRepo.findById(orderId).orElse(null);

        if (order == null) {
            log.warn("SePay webhook: không tìm thấy đơn hàng id={}", orderId);
            return ResponseEntity.ok(response(true, "Order not found"));
        }

        Payment payment = paymentService.findByOrderId(orderId);

        if (payment == null) {
            log.warn("SePay webhook: đơn hàng {} không có payment record", orderId);
            return ResponseEntity.ok(response(true, "Payment record not found"));
        }

        if ("THANH_CONG".equals(payment.getPaymentStatus())) {
            log.info("SePay webhook: đơn hàng {} đã được xác nhận thanh toán trước đó.", orderId);
            return ResponseEntity.ok(response(true, "Already paid"));
        }

        // ===== 5. ĐỐI CHIẾU SỐ TIỀN =====
        long expectedAmount = order.getTotalAmount() == null ? 0L : order.getTotalAmount().longValue();
        long receivedAmount = payload.getTransferAmount() == null ? 0L : payload.getTransferAmount();

        if (receivedAmount < expectedAmount) {
            log.warn("SePay webhook: đơn hàng {} thiếu tiền. Cần {} nhưng nhận {}",
                    orderId, expectedAmount, receivedAmount);
            return ResponseEntity.ok(response(true, "Amount mismatch, needs manual review"));
        }

        // ===== 6. CẬP NHẬT TRẠNG THÁI THANH TOÁN THÀNH CÔNG =====
        payment.setPaymentStatus("THANH_CONG");
        payment.setTransactionId(transactionId);
        payment.setPaymentDate(LocalDateTime.now());
        paymentService.save(payment);

        log.info("SePay webhook: đơn hàng {} đã thanh toán thành công.", orderId);

        return ResponseEntity.ok(response(true, "OK"));
    }

    private Map<String, Object> response(boolean success, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        return body;
    }
}