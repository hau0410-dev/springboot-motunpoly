package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.edu.entity.Payment;
import poly.edu.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepo;

    public Payment save(Payment payment) {
        return paymentRepo.save(payment);
    }

    public Payment findByOrderId(Integer orderId) {
        return paymentRepo.findByOrder_Id(orderId);
    }

    // Lấy thông tin chuyển khoản gần nhất của user (để gợi ý điền sẵn)
    public Payment findLastBankingInfo(Integer userId) {
        if (userId == null) return null;
        return paymentRepo.findTopByOrder_User_IdAndCustomerBankIsNotNullOrderByIdDesc(userId);
    }

    // Dùng khi xử lý webhook SePay để chống xử lý trùng 1 giao dịch nhiều lần
    public Payment findByTransactionId(String transactionId) {
        if (transactionId == null) return null;
        return paymentRepo.findByTransactionId(transactionId);
    }
}