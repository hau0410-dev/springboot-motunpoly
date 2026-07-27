package poly.edu.model;
import java.util.*;

public class Cart {

    private Map<Integer, CartItem> items = new HashMap<>();

    public void add(CartItem item) {

        CartItem existed = items.get(item.getProductId());

        if (existed == null) {

            items.put(item.getProductId(), item);

        } else {

            int newQty =
                    existed.getQuantity()
                    + item.getQuantity();

            if(newQty > existed.getStock()){

                newQty = existed.getStock();
            }

            existed.setQuantity(newQty);
        }
    }

    public void remove(Integer productId) {
        items.remove(productId);
    }

    public void update(Integer productId, int quantity) {
        if (items.containsKey(productId)) {
            items.get(productId).setQuantity(quantity);
        }
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public CartItem getItem(Integer productId) {
        return items.get(productId);
    }

    // Tổng tiền THỰC TẾ khách phải trả (đã áp khuyến mãi PERCENT/AMOUNT nếu có, chưa gồm ship)
    public double getTotalAmount() {
        return items.values()
                .stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    // Tổng tiền GỐC (chưa áp khuyến mãi) - dùng để hiển thị gạch đỏ khi có ưu đãi
    public double getOriginalTotalAmount() {
        return items.values()
                .stream()
                .mapToDouble(CartItem::getTotalOriginalPrice)
                .sum();
    }

    // Số tiền được giảm nhờ khuyến mãi (PERCENT/AMOUNT). Khuyến mãi GIFT không làm giảm số này
    // (giá trị "được tặng" không quy đổi ra tiền giảm ở đây, chỉ hiện ở nhãn promo).
    public double getDiscountAmount() {
        double discount = getOriginalTotalAmount() - getTotalAmount();
        return Math.max(discount, 0);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}