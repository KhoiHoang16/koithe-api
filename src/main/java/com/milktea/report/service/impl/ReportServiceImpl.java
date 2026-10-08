package com.milktea.report.service.impl;

import com.milktea.report.dto.*;
import com.milktea.report.repository.ReportRepository;
import com.milktea.report.service.ReportService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.*;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    // Loại trừ đơn chưa thanh toán và đơn đã hủy
    private static final Set<String> EXCLUDED_STATUSES = Set.of("CHO_XAC_NHAN", "DA_HUY");

    private final ReportRepository reportRepository;

    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportSummaryResponse getSummary(LocalDate from, LocalDate to, Integer top) {
        // 1. Mặc định là hôm nay nếu không chọn khoảng ngày
        LocalDate today = LocalDate.now(VN_ZONE);
        LocalDate startDate = (from != null) ? from : today;
        LocalDate endDate = (to != null) ? to : today;
        int limit = (top != null && top > 0) ? top : 5;

        // 2. Validate khoảng thời gian
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu (from) không được lớn hơn ngày kết thúc (to)");
        }

        // 3. Chuẩn hóa sang Instant theo múi giờ Việt Nam (00:00:00 -> 23:59:59.999)
        Instant fromInstant = startDate.atStartOfDay(VN_ZONE).toInstant();
        Instant toInstant = endDate.atTime(LocalTime.MAX).atZone(VN_ZONE).toInstant();

        // 4. Tổng doanh thu & Tổng số đơn
        List<Object[]> revenueRows = reportRepository.sumRevenueAndCountOrders(fromInstant, toInstant, EXCLUDED_STATUSES);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        if (!revenueRows.isEmpty() && revenueRows.get(0) != null) {
            totalRevenue = (BigDecimal) revenueRows.get(0)[0];
            totalOrders = ((Number) revenueRows.get(0)[1]).longValue();
        }

        // 5. Tổng số ly nước đã bán
        Long totalCups = reportRepository.sumTotalCupsSold(fromInstant, toInstant, EXCLUDED_STATUSES);
        long totalCupsSold = (totalCups != null) ? totalCups : 0L;

        // 6. Doanh thu theo từng ngày
        List<Object[]> dailyRows = reportRepository.dailyRevenue(fromInstant, toInstant, EXCLUDED_STATUSES);
        List<DoanhThuTheoNgayResponse> dailyMetrics = new ArrayList<>();
        for (Object[] row : dailyRows) {
            LocalDate d;
            if (row[0] instanceof LocalDate ld) {
                d = ld;
            } else if (row[0] instanceof Date sqlDate) {
                d = sqlDate.toLocalDate();
            } else {
                d = LocalDate.parse(row[0].toString());
            }
            BigDecimal rev = (BigDecimal) row[1];
            Long count = ((Number) row[2]).longValue();
            dailyMetrics.add(new DoanhThuTheoNgayResponse(d, rev, count));
        }

        // 7. Doanh thu theo 2 kênh bán: Tại quầy POS vs Website / Quét QR
        List<Object[]> channelRows = reportRepository.revenueByChannel(fromInstant, toInstant, EXCLUDED_STATUSES);
        BigDecimal posRevenue = BigDecimal.ZERO;
        long posCount = 0;
        BigDecimal onlineRevenue = BigDecimal.ZERO;
        long onlineCount = 0;

        for (Object[] row : channelRows) {
            String channel = (String) row[0];
            BigDecimal rev = (BigDecimal) row[1];
            Long count = ((Number) row[2]).longValue();
            if ("TAI_QUAY_POS".equals(channel)) {
                posRevenue = posRevenue.add(rev);
                posCount += count;
            } else {
                onlineRevenue = onlineRevenue.add(rev);
                onlineCount += count;
            }
        }
        List<DoanhThuKenhResponse> channelMetrics = List.of(
            new DoanhThuKenhResponse("TAI_QUAY_POS", "Tại quầy POS", posRevenue, posCount),
            new DoanhThuKenhResponse("ONLINE_WEB", "Website / Quét QR bàn", onlineRevenue, onlineCount)
        );

        // 8. Doanh thu theo phương thức thanh toán
        List<Object[]> paymentRows = reportRepository.revenueByPaymentMethod(fromInstant, toInstant, EXCLUDED_STATUSES);
        List<DoanhThuPhuongThucResponse> paymentMetrics = new ArrayList<>();
        for (Object[] row : paymentRows) {
            String method = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            Long count = ((Number) row[2]).longValue();
            String displayName = switch (method) {
                case "TIEN_MAT" -> "Tiền mặt";
                case "CHUYEN_KHOAN_QR" -> "VietQR";
                case "MOMO" -> "Ví MoMo";
                case "VNPAY" -> "Cổng VNPay";
                case "THE_NGAN_HANG" -> "Thẻ ngân hàng";
                default -> method;
            };
            paymentMetrics.add(new DoanhThuPhuongThucResponse(method, displayName, amount, count));
        }

        // 9. Top sản phẩm bán chạy nhất
        List<Object[]> topRows = reportRepository.topSellingProducts(fromInstant, toInstant, EXCLUDED_STATUSES, PageRequest.of(0, limit));
        List<TopSanPhamResponse> topProducts = new ArrayList<>();
        for (Object[] row : topRows) {
            Long variantId = ((Number) row[0]).longValue();
            String prodName = (String) row[1];
            String size = (String) row[2];
            Long qty = ((Number) row[3]).longValue();
            BigDecimal amount = (BigDecimal) row[4];
            topProducts.add(new TopSanPhamResponse(variantId, prodName, size, qty, amount));
        }

        return new ReportSummaryResponse(
            startDate,
            endDate,
            totalRevenue,
            totalOrders,
            totalCupsSold,
            dailyMetrics,
            channelMetrics,
            paymentMetrics,
            topProducts
        );
    }
}
