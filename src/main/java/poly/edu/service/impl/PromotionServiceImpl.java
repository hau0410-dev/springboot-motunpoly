package poly.edu.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.edu.entity.Product;
import poly.edu.entity.Promotion;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.PromotionRepository;
import poly.edu.service.PromotionService;

@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    PromotionRepository promotionRepo;

    @Autowired
    OrderRepository orderRepo;

    @Override
    public List<Promotion> findAll() {
        return promotionRepo.findAll();
    }

    @Override
    public List<Promotion> findActive() {
        return promotionRepo.findByActiveTrue();
    }

    @Override
    public Promotion findById(Integer id) {
        return promotionRepo.findById(id).orElse(null);
    }

    @Override
    public Promotion save(Promotion promotion) {
        return promotionRepo.save(promotion);
    }

    @Override
    public void deleteById(Integer id) {
        promotionRepo.deleteById(id);
    }

    @Override
    public Double getTotalSpent(Integer userId) {
        if (userId == null) {
            return 0.0;
        }
        Double total = orderRepo.getTotalSpentByUser(userId);
        return total == null ? 0.0 : total;
    }

    @Override
    public List<Promotion> getEligiblePromotions(Integer userId) {

        Double totalSpent = getTotalSpent(userId);

        List<Promotion> result = new ArrayList<>();

        for (Promotion p : promotionRepo.findByActiveTrue()) {

            double minSpent = (p.getMinTotalSpent() == null) ? 0 : p.getMinTotalSpent();

            if (totalSpent >= minSpent) {
                result.add(p);
            }
        }

        return result;
    }

    @Override
    public Promotion getPromotionForProduct(Integer productId, List<Promotion> eligiblePromotions) {

        if (productId == null || eligiblePromotions == null) {
            return null;
        }

        Promotion best = null;

        for (Promotion promo : eligiblePromotions) {

            if (promo.getProducts() == null) {
                continue;
            }

            for (Product p : promo.getProducts()) {

                if (p.getId().equals(productId)) {

                    double minSpent = (promo.getMinTotalSpent() == null) ? 0 : promo.getMinTotalSpent();
                    double bestMin = (best == null || best.getMinTotalSpent() == null) ? -1 : best.getMinTotalSpent();

                    // Ưu tiên khuyến mãi có điều kiện cao hơn (ưu đãi tốt hơn)
                    if (best == null || minSpent > bestMin) {
                        best = promo;
                    }

                    break;
                }
            }
        }

        return best;
    }
}