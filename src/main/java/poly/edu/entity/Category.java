package poly.edu.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String icon;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Loại: PRODUCT (danh mục sản phẩm) | BRAND (hãng xe) | PARTS_BRAND (hãng phụ tùng)
    @Column(name = "type")
    private String type = "PRODUCT";

    // ===== Constructor =====
    public Category() {
    }

    public Category(Integer id, String name, String icon, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.isActive = isActive;
    }

    // ===== Getter & Setter =====
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}