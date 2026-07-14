package poly.edu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_orders")
public class ReturnOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "shipper_id")
    private User shipper;

    private String reason;

    @Column(name = "reason_note")
    private String reasonNote;

    @Column(name = "refund_amount")
    private Double refundAmount;

    // CHO_XAC_NHAN -> DA_TU_CHOI
    //              -> DA_XAC_NHAN -> DANG_LAY_HANG -> DA_LAY_HANG -> HOAN_KHO
    private String status;

    @Column(name = "admin_note")
    private String adminNote;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "picked_date")
    private LocalDateTime pickedDate;

    @Column(name = "restocked_date")
    private LocalDateTime restockedDate;

    // ===== Getter & Setter =====

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getShipper() { return shipper; }
    public void setShipper(User shipper) { this.shipper = shipper; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReasonNote() { return reasonNote; }
    public void setReasonNote(String reasonNote) { this.reasonNote = reasonNote; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }

    public LocalDateTime getConfirmedDate() { return confirmedDate; }
    public void setConfirmedDate(LocalDateTime confirmedDate) { this.confirmedDate = confirmedDate; }

    public LocalDateTime getRejectedDate() { return rejectedDate; }
    public void setRejectedDate(LocalDateTime rejectedDate) { this.rejectedDate = rejectedDate; }

    public LocalDateTime getPickedDate() { return pickedDate; }
    public void setPickedDate(LocalDateTime pickedDate) { this.pickedDate = pickedDate; }

    public LocalDateTime getRestockedDate() { return restockedDate; }
    public void setRestockedDate(LocalDateTime restockedDate) { this.restockedDate = restockedDate; }
}