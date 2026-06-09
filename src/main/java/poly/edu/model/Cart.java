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

    public double getTotalAmount() {
        return items.values()
                .stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}