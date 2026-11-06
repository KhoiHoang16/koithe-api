package com.milktea.report.repository;

import com.milktea.order.entity.DonHang;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ReportRepository extends JpaRepository<DonHang, Long> {

    @Query("""
        SELECT COALESCE(SUM(o.tongTienThanhToan), 0), COUNT(o)
        FROM DonHang o
        WHERE o.deletedAt IS NULL
          AND o.ngayTao >= :from AND o.ngayTao <= :to
          AND o.trangThai NOT IN :excludedStatuses
    """)
    List<Object[]> sumRevenueAndCountOrders(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses
    );

    @Query("""
        SELECT COALESCE(SUM(ct.soLuong), 0)
        FROM ChiTietDonHang ct
        JOIN ct.donHang o
        WHERE ct.deletedAt IS NULL
          AND o.deletedAt IS NULL
          AND o.ngayTao >= :from AND o.ngayTao <= :to
          AND o.trangThai NOT IN :excludedStatuses
    """)
    Long sumTotalCupsSold(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses
    );

    @Query(value = """
        SELECT DATE(o.ngay_tao AT TIME ZONE 'Asia/Ho_Chi_Minh') AS ngay,
               COALESCE(SUM(o.tong_tien_thanh_toan), 0) AS doanh_thu,
               COUNT(o.id) AS so_don
        FROM don_hang o
        WHERE o.deleted_at IS NULL
          AND o.ngay_tao >= :from AND o.ngay_tao <= :to
          AND o.trang_thai NOT IN :excludedStatuses
        GROUP BY DATE(o.ngay_tao AT TIME ZONE 'Asia/Ho_Chi_Minh')
        ORDER BY ngay ASC
    """, nativeQuery = true)
    List<Object[]> dailyRevenue(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses
    );

    @Query("""
        SELECT o.kenhDatHang, COALESCE(SUM(o.tongTienThanhToan), 0), COUNT(o)
        FROM DonHang o
        WHERE o.deletedAt IS NULL
          AND o.ngayTao >= :from AND o.ngayTao <= :to
          AND o.trangThai NOT IN :excludedStatuses
        GROUP BY o.kenhDatHang
    """)
    List<Object[]> revenueByChannel(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses
    );

    @Query("""
        SELECT g.phuongThucThanhToan, COALESCE(SUM(g.soTien), 0), COUNT(g)
        FROM GiaoDichThanhToan g
        JOIN g.donHang o
        WHERE g.deletedAt IS NULL
          AND o.deletedAt IS NULL
          AND g.trangThai = 'THANH_CONG'
          AND o.ngayTao >= :from AND o.ngayTao <= :to
          AND o.trangThai NOT IN :excludedStatuses
        GROUP BY g.phuongThucThanhToan
    """)
    List<Object[]> revenueByPaymentMethod(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses
    );

    @Query("""
        SELECT bt.id, sp.tenSanPham, bt.kichCo,
               COALESCE(SUM(ct.soLuong), 0),
               COALESCE(SUM(ct.thanhTien), 0)
        FROM ChiTietDonHang ct
        JOIN ct.donHang o
        JOIN ct.bienThe bt
        JOIN bt.sanPham sp
        WHERE ct.deletedAt IS NULL
          AND o.deletedAt IS NULL
          AND o.ngayTao >= :from AND o.ngayTao <= :to
          AND o.trangThai NOT IN :excludedStatuses
        GROUP BY bt.id, sp.tenSanPham, bt.kichCo
        ORDER BY SUM(ct.soLuong) DESC
    """)
    List<Object[]> topSellingProducts(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses,
        Pageable pageable
    );

    @Query(value = """
        SELECT 
            p.period_key,
            p.doanh_thu,
            p.so_don,
            COALESCE(c.so_ly, 0) AS so_ly
        FROM (
            SELECT 
                to_char(o.ngay_tao AT TIME ZONE 'Asia/Ho_Chi_Minh', CAST(:dateFormat AS text)) AS period_key,
                COALESCE(SUM(o.tong_tien_thanh_toan), 0) AS doanh_thu,
                COUNT(o.id) AS so_don
            FROM don_hang o
            WHERE o.deleted_at IS NULL
              AND o.ngay_tao >= :from AND o.ngay_tao <= :to
              AND o.trang_thai NOT IN :excludedStatuses
            GROUP BY 1
        ) p
        LEFT JOIN (
            SELECT 
                to_char(o.ngay_tao AT TIME ZONE 'Asia/Ho_Chi_Minh', CAST(:dateFormat AS text)) AS period_key,
                COALESCE(SUM(ct.so_luong), 0) AS so_ly
            FROM chi_tiet_don_hang ct
            JOIN don_hang o ON ct.ma_don_hang = o.id
            WHERE ct.deleted_at IS NULL
              AND o.deleted_at IS NULL
              AND o.ngay_tao >= :from AND o.ngay_tao <= :to
              AND o.trang_thai NOT IN :excludedStatuses
            GROUP BY 1
        ) c ON p.period_key = c.period_key
        ORDER BY p.period_key ASC
    """, nativeQuery = true)
    List<Object[]> revenueTrend(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses,
        @Param("dateFormat") String dateFormat
    );

    @Query(value = """
        SELECT sp.id, sp.ten_san_pham, dm.ten_danh_muc, sp.hinh_anh,
               COALESCE(SUM(ct.so_luong), 0) AS tong_ly,
               COALESCE(SUM(ct.thanh_tien), 0) AS tong_tien
        FROM chi_tiet_don_hang ct
        JOIN don_hang o ON ct.ma_don_hang = o.id
        JOIN bien_the_san_pham bt ON ct.ma_bien_the = bt.id
        JOIN san_pham sp ON bt.ma_san_pham = sp.id
        JOIN loai_san_pham dm ON sp.ma_danh_muc = dm.id
        WHERE ct.deleted_at IS NULL
          AND o.deleted_at IS NULL
          AND sp.deleted_at IS NULL
          AND o.ngay_tao >= :from AND o.ngay_tao <= :to
          AND o.trang_thai NOT IN :excludedStatuses
          AND (:categoryId IS NULL OR dm.id = :categoryId)
        GROUP BY sp.id, sp.ten_san_pham, dm.ten_danh_muc, sp.hinh_anh
        ORDER BY SUM(ct.thanh_tien) DESC, sp.id ASC
    """, countQuery = """
        SELECT COUNT(DISTINCT sp.id)
        FROM chi_tiet_don_hang ct
        JOIN don_hang o ON ct.ma_don_hang = o.id
        JOIN bien_the_san_pham bt ON ct.ma_bien_the = bt.id
        JOIN san_pham sp ON bt.ma_san_pham = sp.id
        JOIN loai_san_pham dm ON sp.ma_danh_muc = dm.id
        WHERE ct.deleted_at IS NULL
          AND o.deleted_at IS NULL
          AND sp.deleted_at IS NULL
          AND o.ngay_tao >= :from AND o.ngay_tao <= :to
          AND o.trang_thai NOT IN :excludedStatuses
          AND (:categoryId IS NULL OR dm.id = :categoryId)
    """, nativeQuery = true)
    org.springframework.data.domain.Page<Object[]> findProductPerformanceByRevenue(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    @Query(value = """
        SELECT sp.id, sp.ten_san_pham, dm.ten_danh_muc, sp.hinh_anh,
               COALESCE(SUM(ct.so_luong), 0) AS tong_ly,
               COALESCE(SUM(ct.thanh_tien), 0) AS tong_tien
        FROM chi_tiet_don_hang ct
        JOIN don_hang o ON ct.ma_don_hang = o.id
        JOIN bien_the_san_pham bt ON ct.ma_bien_the = bt.id
        JOIN san_pham sp ON bt.ma_san_pham = sp.id
        JOIN loai_san_pham dm ON sp.ma_danh_muc = dm.id
        WHERE ct.deleted_at IS NULL
          AND o.deleted_at IS NULL
          AND sp.deleted_at IS NULL
          AND o.ngay_tao >= :from AND o.ngay_tao <= :to
          AND o.trang_thai NOT IN :excludedStatuses
          AND (:categoryId IS NULL OR dm.id = :categoryId)
        GROUP BY sp.id, sp.ten_san_pham, dm.ten_danh_muc, sp.hinh_anh
        ORDER BY SUM(ct.so_luong) DESC, sp.id ASC
    """, countQuery = """
        SELECT COUNT(DISTINCT sp.id)
        FROM chi_tiet_don_hang ct
        JOIN don_hang o ON ct.ma_don_hang = o.id
        JOIN bien_the_san_pham bt ON ct.ma_bien_the = bt.id
        JOIN san_pham sp ON bt.ma_san_pham = sp.id
        JOIN loai_san_pham dm ON sp.ma_danh_muc = dm.id
        WHERE ct.deleted_at IS NULL
          AND o.deleted_at IS NULL
          AND sp.deleted_at IS NULL
          AND o.ngay_tao >= :from AND o.ngay_tao <= :to
          AND o.trang_thai NOT IN :excludedStatuses
          AND (:categoryId IS NULL OR dm.id = :categoryId)
    """, nativeQuery = true)
    org.springframework.data.domain.Page<Object[]> findProductPerformanceByQuantity(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    @Query(value = """
        SELECT bt.ma_san_pham, bt.id, bt.kich_co,
               COALESCE(SUM(ct.so_luong), 0) AS so_luong,
               COALESCE(SUM(ct.thanh_tien), 0) AS doanh_thu
        FROM chi_tiet_don_hang ct
        JOIN don_hang o ON ct.ma_don_hang = o.id
        JOIN bien_the_san_pham bt ON ct.ma_bien_the = bt.id
        WHERE ct.deleted_at IS NULL
          AND o.deleted_at IS NULL
          AND o.ngay_tao >= :from AND o.ngay_tao <= :to
          AND o.trang_thai NOT IN :excludedStatuses
          AND bt.ma_san_pham IN :productIds
        GROUP BY bt.ma_san_pham, bt.id, bt.kich_co
        ORDER BY bt.ma_san_pham, bt.id
    """, nativeQuery = true)
    List<Object[]> findVariantBreakdownByProductIds(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("excludedStatuses") Collection<String> excludedStatuses,
        @Param("productIds") Collection<Long> productIds
    );
}
