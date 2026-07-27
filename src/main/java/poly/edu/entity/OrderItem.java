package poly.edu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Tổng số lượng THỰC XUẤT KHO / giao cho khách (đã gồm hàng tặng nếu có khuyến mãi mua 1 tặng 1)
    private Integer quantity;

    // Đơn giá THỰC TẾ tính tiền (đã áp khuyến mãi PERCENT/AMOUNT nếu có)
    private Double price;

    // Giá GỐC (trước khuyến mãi) - dùng để hiển thị gạch đỏ trong lịch sử đơn hàng
    @Column(name = "original_price")
    private Double originalPrice;

    // Số lượng được TẶNG KÈM, không tính tiền (khuyến mãi "mua 1 tặng 1").
    // Số lượng THỰC TÍNH TIỀN = quantity - bonusQuantity
    @Column(name = "bonus_quantity")
    private Integer bonusQuantity = 0;

    // Nhãn khuyến mãi đã áp dụng tại thời điểm đặt hàng (lưu lại để hiển thị, không phụ thuộc
    // vào khuyến mãi có còn tồn tại/active hay không về sau)
    @Column(name = "promo_label")
    private String promoLabel;

    // Số tiền khách THỰC SỰ trả cho dòng sản phẩm này = price * (quantity - bonusQuantity)
    private Double subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // ===== Getter & Setter =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getBonusQuantity() {
        return bonusQuantity;
    }

    public void setBonusQuantity(Integer bonusQuantity) {
        this.bonusQuantity = bonusQuantity;
    }

    public String getPromoLabel() {
        return promoLabel;
    }

    public void setPromoLabel(String promoLabel) {
        this.promoLabel = promoLabel;
    }

    // Số lượng THỰC TÍNH TIỀN (không gồm hàng tặng)
    public Integer getPaidQuantity() {
        int bonus = (bonusQuantity == null) ? 0 : bonusQuantity;
        int qty = (quantity == null) ? 0 : quantity;
        return qty - bonus;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}