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
}