package poly.edu.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.Revenue;

public interface RevenueRepository extends JpaRepository<Revenue, Integer> {

    // ===== DANH SÁCH DÒNG DOANH THU: lọc theo khoảng thời gian + số tiền tối thiểu (mọi tham số có thể NULL = bỏ qua điều kiện đó) =====
    @Query(value = """
        SELECT * FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
          AND (:minAmount IS NULL OR subtotal >= :minAmount)
        ORDER BY created_date DESC
        """, nativeQuery = true)
    List<Revenue> findFiltered(@Param("from") Date from, @Param("to") Date to, @Param("minAmount") Double minAmount);

    // ===== TỔNG / CAO NHẤT / TRUNG BÌNH TRONG KHOẢNG THỜI GIAN ĐANG LỌC =====
    @Query(value = """
        SELECT SUM(subtotal) FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
        """, nativeQuery = true)
    Double getTotalInRange(@Param("from") Date from, @Param("to") Date to);

    @Query(value = """
        SELECT MAX(subtotal) FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
        """, nativeQuery = true)
    Double getMaxInRange(@Param("from") Date from, @Param("to") Date to);

    @Query(value = """
        SELECT AVG(subtotal) FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
        """, nativeQuery = true)
    Double getAvgInRange(@Param("from") Date from, @Param("to") Date to);

    // ===== DỮ LIỆU BIỂU ĐỒ: gom theo NGÀY =====
    @Query(value = """
        SELECT CAST(created_date AS DATE) AS d, SUM(subtotal) AS total
        FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
        GROUP BY CAST(created_date AS DATE)
        ORDER BY CAST(created_date AS DATE)
        """, nativeQuery = true)
    List<Object[]> getChartByDay(@Param("from") Date from, @Param("to") Date to);

    // ===== DỮ LIỆU BIỂU ĐỒ: gom theo TUẦN (ISO week, tránh lệch năm) =====
    @Query(value = """
        SELECT YEAR(created_date) AS yr, DATEPART(iso_week, created_date) AS wk, SUM(subtotal) AS total
        FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
        GROUP BY YEAR(created_date), DATEPART(iso_week, created_date)
        ORDER BY YEAR(created_date), DATEPART(iso_week, created_date)
        """, nativeQuery = true)
    List<Object[]> getChartByWeek(@Param("from") Date from, @Param("to") Date to);

    // ===== DỮ LIỆU BIỂU ĐỒ: gom theo THÁNG =====
    @Query(value = """
        SELECT YEAR(created_date) AS yr, MONTH(created_date) AS mo, SUM(subtotal) AS total
        FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
        GROUP BY YEAR(created_date), MONTH(created_date)
        ORDER BY YEAR(created_date), MONTH(created_date)
        """, nativeQuery = true)
    List<Object[]> getChartByMonth(@Param("from") Date from, @Param("to") Date to);

    // ===== DỮ LIỆU BIỂU ĐỒ: gom theo NĂM =====
    @Query(value = """
        SELECT YEAR(created_date) AS yr, SUM(subtotal) AS total
        FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
        GROUP BY YEAR(created_date)
        ORDER BY YEAR(created_date)
        """, nativeQuery = true)
    List<Object[]> getChartByYear(@Param("from") Date from, @Param("to") Date to);

    // ===== TOP SẢN PHẨM BÁN CHẠY TRONG KHOẢNG THỜI GIAN ĐANG LỌC =====
    @Query(value = """
        SELECT product_name, SUM(quantity) as total_qty
        FROM revenue
        WHERE (:from IS NULL OR created_date >= :from)
          AND (:to IS NULL OR created_date < DATEADD(day, 1, :to))
        GROUP BY product_name
        ORDER BY total_qty DESC
        """, nativeQuery = true)
    List<Object[]> getTopProducts(@Param("from") Date from, @Param("to") Date to);

    // Danh sách orderItemId đã được tính doanh thu rồi -> dùng để đồng bộ kiểu cộng dồn,
    // tránh tính trùng và không cần xoá sạch bảng mỗi lần đồng bộ.
    @Query("SELECT r.orderItemId FROM Revenue r WHERE r.orderItemId IS NOT NULL")
    List<Integer> findAllSyncedOrderItemIds();
}