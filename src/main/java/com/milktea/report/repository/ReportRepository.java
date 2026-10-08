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
}
