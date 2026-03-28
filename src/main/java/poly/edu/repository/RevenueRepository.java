package poly.edu.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.Revenue;
import java.time.LocalDate;

public interface RevenueRepository extends JpaRepository<Revenue, Integer> {

    @Query("SELECT SUM(r.subtotal) FROM Revenue r")
    Double getTotalRevenue();

    @Query("SELECT r FROM Revenue r WHERE r.subtotal >= :amount")
    List<Revenue> searchByAmount(@Param("amount") Double amount);

    // Doanh thu cao nhất hôm nay
    @Query(value = """
        SELECT MAX(subtotal)
        FROM revenue
        WHERE CAST(created_date AS DATE) = CAST(GETDATE() AS DATE)
        """, nativeQuery = true)
    Double getMaxToday();

    // Doanh thu trung bình hôm nay
    @Query(value = """
        SELECT AVG(subtotal)
        FROM revenue
        WHERE CAST(created_date AS DATE) = CAST(GETDATE() AS DATE)
        """, nativeQuery = true)
    Double getAvgToday();

    // Lọc theo ngày
    @Query(value = """
        SELECT * 
        FROM revenue
        WHERE CAST(created_date AS DATE) = :date
        """, nativeQuery = true)
    List<Revenue> findByDate(@Param("date") Date sqlDate);

    // Ngày cho biểu đồ
    @Query(value = """
            SELECT CAST(created_date AS DATE)
            FROM revenue
            GROUP BY CAST(created_date AS DATE)
            ORDER BY CAST(created_date AS DATE)
            """, nativeQuery = true)
    List<LocalDate> getChartDates();

    // Tổng tiền theo ngày
    @Query(value = """
        SELECT SUM(subtotal)
        FROM revenue
        GROUP BY CAST(created_date AS DATE)
        ORDER BY CAST(created_date AS DATE)
        """, nativeQuery = true)
    List<Double> getChartTotals();

    // Top sản phẩm bán chạy
    @Query(value = """
        SELECT product_name, SUM(quantity) as total_qty
        FROM revenue
        GROUP BY product_name
        ORDER BY total_qty DESC
        """, nativeQuery = true)
    List<Object[]> getTopProducts();
}