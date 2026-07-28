package poly.edu.dto.ghn;

// FE chỉ cần 2 select: Tỉnh -> Phường. Nhưng GHN tính phí vẫn cần districtId đi kèm ward,
// nên mỗi lựa chọn "phường" ở đây mang theo sẵn districtId + tên quận (hiển thị phụ, VD: "Phường Hải Châu 1 (Q. Hải Châu)")
public class GhnWardOption {
    private String wardCode;
    private String wardName;
    private Integer districtId;
    private String districtName;

    public GhnWardOption() {}
    public GhnWardOption(String wardCode, String wardName, Integer districtId, String districtName) {
        this.wardCode = wardCode; this.wardName = wardName;
        this.districtId = districtId; this.districtName = districtName;
    }

    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
}