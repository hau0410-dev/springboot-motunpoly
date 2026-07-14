package poly.edu.dto;

/**
 * Cấu trúc JSON mà SePay POST tới endpoint webhook mỗi khi có biến động số dư.
 * Tham khảo: https://docs.sepay.vn/tich-hop-webhooks.html
 */
public class SePayWebhookPayload {

    private Long id;                 // id giao dịch bên SePay -> dùng chống trùng
    private String gateway;          // Tên ngân hàng, vd: "MBBank"
    private String transactionDate;  // "2024-07-02 11:08:33"
    private String accountNumber;    // Số tài khoản nhận tiền
    private String subAccount;
    private String code;             // Mã do SePay tự nhận diện được (có thể null)
    private String content;          // Nội dung chuyển khoản đầy đủ, vd: "DH105 chuyen tien"
    private String transferType;     // "in" hoặc "out"
    private String description;
    private Long transferAmount;     // Số tiền giao dịch (VND)
    private Long accumulated;
    private String referenceCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getSubAccount() { return subAccount; }
    public void setSubAccount(String subAccount) { this.subAccount = subAccount; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTransferType() { return transferType; }
    public void setTransferType(String transferType) { this.transferType = transferType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTransferAmount() { return transferAmount; }
    public void setTransferAmount(Long transferAmount) { this.transferAmount = transferAmount; }

    public Long getAccumulated() { return accumulated; }
    public void setAccumulated(Long accumulated) { this.accumulated = accumulated; }

    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }
}