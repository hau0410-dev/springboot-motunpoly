package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.edu.entity.Payment;

public interface PaymentRepository
        extends JpaRepository<Payment, Integer> {

	Payment findByOrder_Id(Integer orderId);

	// Lấy lần chuyển khoản gần nhất của user (để gợi ý điền sẵn lần sau)
	Payment findTopByOrder_User_IdAndCustomerBankIsNotNullOrderByIdDesc(Integer userId);

}