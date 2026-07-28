package poly.edu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "shipper_id")
    private User shipper;

    @OneToOne(mappedBy = "order")
    private Payment payment;

    private String fullname;
    private String email;
    private String phone;
    private String address;

    @Column(name = "original_amount")
    private Double originalAmount;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "shipping_fee")
    private Double shippingFee;

    private String district;

    private Double totalAmount;

    private String status;

    private LocalDateTime createdDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    // ===== THÊM MỚI: mô hình lai NỘI BỘ (TP.HCM) / GHN (ngoài tỉnh) =====
    @Column(name = "shipping_method")
    private String shippingMethod = "NOI_BO";

    @Column(name = "ghn_to_province_id")
    private Integer ghnToProvinceId;

    @Column(name = "ghn_to_district_id")
    private Integer ghnToDistrictId;

    @Column(name = "ghn_to_ward_code")
    private String ghnToWardCode;

    @Column(name = "ghn_order_code")
    private String ghnOrderCode;

    @Column(name = "ghn_status")
    private String ghnStatus;

    @Column(name = "ghn_fee")
    private Double ghnFee;

    @Column(name = "ghn_expected_delivery")
    private LocalDateTime ghnExpectedDelivery;

    // FIX: THÊM MỚI - tên chữ (Tỉnh/Quận/Phường) để hiển thị địa chỉ đầy đủ, khỏi phải gọi lại GHN
    @Column(name = "ghn_to_province_name")
    private String ghnToProvinceName;

    @Column(name = "ghn_to_district_name")
    private String ghnToDistrictName;

    @Column(name = "ghn_to_ward_name")
    private String ghnToWardName;

    // ===== Getter & Setter =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(Double originalAmount) {
        this.originalAmount = originalAmount;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public boolean isHasDiscount() {
        return discountAmount != null && discountAmount > 0;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }

    public User getShipper() {
        return shipper;
    }

    public void setShipper(User shipper) {
        this.shipper = shipper;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }

    public Integer getGhnToProvinceId() { return ghnToProvinceId; }
    public void setGhnToProvinceId(Integer ghnToProvinceId) { this.ghnToProvinceId = ghnToProvinceId; }

    public Integer getGhnToDistrictId() { return ghnToDistrictId; }
    public void setGhnToDistrictId(Integer ghnToDistrictId) { this.ghnToDistrictId = ghnToDistrictId; }

    public String getGhnToWardCode() { return ghnToWardCode; }
    public void setGhnToWardCode(String ghnToWardCode) { this.ghnToWardCode = ghnToWardCode; }

    public String getGhnOrderCode() { return ghnOrderCode; }
    public void setGhnOrderCode(String ghnOrderCode) { this.ghnOrderCode = ghnOrderCode; }

    public String getGhnStatus() { return ghnStatus; }
    public void setGhnStatus(String ghnStatus) { this.ghnStatus = ghnStatus; }

    public Double getGhnFee() { return ghnFee; }
    public void setGhnFee(Double ghnFee) { this.ghnFee = ghnFee; }

    public LocalDateTime getGhnExpectedDelivery() { return ghnExpectedDelivery; }
    public void setGhnExpectedDelivery(LocalDateTime ghnExpectedDelivery) { this.ghnExpectedDelivery = ghnExpectedDelivery; }

    public String getGhnToProvinceName() { return ghnToProvinceName; }
    public void setGhnToProvinceName(String ghnToProvinceName) { this.ghnToProvinceName = ghnToProvinceName; }

    public String getGhnToDistrictName() { return ghnToDistrictName; }
    public void setGhnToDistrictName(String ghnToDistrictName) { this.ghnToDistrictName = ghnToDistrictName; }

    public String getGhnToWardName() { return ghnToWardName; }
    public void setGhnToWardName(String ghnToWardName) { this.ghnToWardName = ghnToWardName; }

    // FIX: gộp địa chỉ đầy đủ để hiển thị (thay vì chỉ hiện mỗi order.address như code gốc)
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);

        if ("GHN".equals(shippingMethod)) {
            if (ghnToWardName != null) sb.append(", ").append(ghnToWardName);
            if (ghnToDistrictName != null) sb.append(", ").append(ghnToDistrictName);
            if (ghnToProvinceName != null) sb.append(", ").append(ghnToProvinceName);
        } else {
            if (district != null) sb.append(", ").append(district);
            sb.append(", TP. Hồ Chí Minh");
        }
        return sb.toString();
    }
}