package poly.edu.service;

import java.util.List;

import poly.edu.entity.Promotion;

public interface PromotionService {

    List<Promotion> findAll();

    List<Promotion> findActive();

    Promotion findById(Integer id);

    Promotion save(Promotion promotion);

    void deleteById(Integer id);

    // Tổng tiền user đã mua (chỉ tính đơn đã giao)
    Double getTotalSpent(Integer userId);

    // Danh sách khuyến mãi mà user hiện tại đã đủ điều kiện (đang active)
    List<Promotion> getEligiblePromotions(Integer userId);

    // Khuyến mãi áp dụng cho 1 sản phẩm cụ thể, dựa trên danh sách khuyến mãi user đã đủ điều kiện
    Promotion getPromotionForProduct(Integer productId, List<Promotion> eligiblePromotions);
}