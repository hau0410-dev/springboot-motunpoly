package poly.edu.entity;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Tên chương trình khuyến mãi - VD: "Ưu đãi khách hàng Vàng"
    @Column(nullable = false)
    private String title;

    // Nhãn hiển thị trên sản phẩm - VD: "Giảm 10%", "Mua 1 tặng 1"
    @Column(name = "badge_text")
    private String badgeText;

    // Mô tả chi tiết
    private String description;

    // Icon hiển thị kèm badge - VD: 🎁, 🔥, 💎
    private String icon;

    // Loại giảm giá: PERCENT | AMOUNT | GIFT
    @Column(name = "discount_type")
    private String discountType;

    // Giá trị giảm (theo % hoặc theo số tiền, tuỳ discountType)
    @Column(name = "discount_value")
    private Double discountValue;

    // Điều kiện: tổng tiền user đã mua (đơn DA_GIAO) phải >= giá trị này
    @Column(name = "min_total_spent")
    private Double minTotalSpent;

    @Column(name = "is_active")
    private Boolean active = true;

    // Danh sách sản phẩm được áp dụng khuyến mãi này
    @ManyToMany
    @JoinTable(
            name = "promotion_products",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    // ===== Constructor =====
    public Promotion() {
    }

    // ===== Getter & Setter =====
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public Double getMinTotalSpent() {
        return minTotalSpent;
    }

    public void setMinTotalSpent(Double minTotalSpent) {
        this.minTotalSpent = minTotalSpent;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}