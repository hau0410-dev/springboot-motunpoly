package poly.edu.model;

public class CartItem {

    private Integer productId;
    private String productName;

    // Đơn giá THỰC TẾ khách phải trả cho 1 sản phẩm (đã áp khuyến mãi PERCENT/AMOUNT nếu có).
    // Với khuyến mãi GIFT (mua 1 tặng 1) thì price giữ nguyên giá gốc, phần "tặng" nằm ở bonusQuantity.
    private double price;

    // Giá gốc (trước khuyến mãi) - dùng để hiển thị gạch đỏ khi có ưu đãi
    private double originalPrice;

    private int quantity;

    // Số lượng được TẶNG KÈM (không tính tiền) do khuyến mãi loại "mua 1 tặng 1" (GIFT).
    // Số lượng thực xuất kho / giao cho khách = quantity + bonusQuantity.
    private int bonusQuantity = 0;

    private String image;
    private Integer stock;

    // Thông tin khuyến mãi đang áp dụng cho sản phẩm này (nếu có) - để hiển thị badge trong giỏ hàng
    private String promoLabel;      // VD: "Giảm 10%", "Mua 1 tặng 1"
    private String promoType;       // PERCENT | AMOUNT | GIFT

    // Số tiền khách phải trả cho dòng sản phẩm này (không tính hàng tặng)
    public double getTotalPrice() {
        return price * quantity;
    }

    // Giá trị GỐC (chưa khuyến mãi) của dòng sản phẩm này - dùng để hiển thị gạch đỏ
    public double getTotalOriginalPrice() {
        return originalPrice * quantity;
    }

    // Có đang được khuyến mãi hay không (để template dễ kiểm tra th:if)
    public boolean isHasPromo() {
        return promoType != null;
    }

    // ===== getter & setter =====
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBonusQuantity() {
        return bonusQuantity;
    }

    public void setBonusQuantity(int bonusQuantity) {
        this.bonusQuantity = bonusQuantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getPromoLabel() {
        return promoLabel;
    }

    public void setPromoLabel(String promoLabel) {
        this.promoLabel = promoLabel;
    }

    public String getPromoType() {
        return promoType;
    }

    public void setPromoType(String promoType) {
        this.promoType = promoType;
    }
}