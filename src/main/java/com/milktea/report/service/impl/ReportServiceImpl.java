package com.milktea.report.service.impl;

import com.milktea.common.response.PageResponse;
import com.milktea.report.dto.*;
import com.milktea.report.repository.ReportRepository;
import com.milktea.report.service.ReportService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private record TrendAccumulator(BigDecimal revenue, Long orders, Long cups) {
        static final TrendAccumulator EMPTY = new TrendAccumulator(BigDecimal.ZERO, 0L, 0L);
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

    @Override
    @Transactional(readOnly = true)
    public List<RevenueTrendResponse> getRevenueTrend(
            Integer year,
            ReportInterval interval) {
        ReportInterval chosenInterval = (interval != null) ? interval : ReportInterval.MONTH;
        int targetYear = (year != null) ? year : LocalDate.now(VN_ZONE).getYear();

        LocalDate startDate;
        LocalDate endDate;

        if (chosenInterval == ReportInterval.YEAR) {
            startDate = LocalDate.of(targetYear - 4, 1, 1);
            endDate = LocalDate.of(targetYear, 12, 31);
        } else {
            startDate = LocalDate.of(targetYear, 1, 1);
            endDate = LocalDate.of(targetYear, 12, 31);
        }

        Instant fromInstant = startDate.atStartOfDay(VN_ZONE).toInstant();
        Instant toInstant = endDate.atTime(LocalTime.MAX).atZone(VN_ZONE).toInstant();

        String sqlFormat = switch (chosenInterval) {
            case MONTH -> "YYYY-MM";
            case QUARTER -> "YYYY-\"Q\"Q";
            case YEAR -> "YYYY";
        };

        List<Object[]> rows = reportRepository.revenueTrend(fromInstant, toInstant, EXCLUDED_STATUSES, sqlFormat);

        Map<String, TrendAccumulator> dbMap = new HashMap<>();
        for (Object[] row : rows) {
            String key = row[0] != null ? row[0].toString() : "";
            BigDecimal rev = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            Long orders = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long cups = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            dbMap.put(key, new TrendAccumulator(rev, orders, cups));
        }

        List<RevenueTrendResponse> result = new ArrayList<>();
        switch (chosenInterval) {
            case MONTH -> {
                YearMonth startYm = YearMonth.from(startDate);
                YearMonth endYm = YearMonth.from(endDate);
                for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
                    String key = ym.toString();
                    String label = String.format("Tháng %02d/%d", ym.getMonthValue(), ym.getYear());
                    TrendAccumulator val = dbMap.getOrDefault(key, TrendAccumulator.EMPTY);
                    result.add(new RevenueTrendResponse(key, label, val.revenue, val.orders, val.cups));
                }
            }
            case QUARTER -> {
                for (int q = 1; q <= 4; q++) {
                    String key = String.format("%d-Q%d", targetYear, q);
                    String label = String.format("Quý %d/%d", q, targetYear);
                    TrendAccumulator val = dbMap.getOrDefault(key, TrendAccumulator.EMPTY);
                    result.add(new RevenueTrendResponse(key, label, val.revenue, val.orders, val.cups));
                }
            }
            case YEAR -> {
                for (int y = startDate.getYear(); y <= endDate.getYear(); y++) {
                    String key = String.valueOf(y);
                    String label = "Năm " + y;
                    TrendAccumulator val = dbMap.getOrDefault(key, TrendAccumulator.EMPTY);
                    result.add(new RevenueTrendResponse(key, label, val.revenue, val.orders, val.cups));
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductPerformanceResponse> getProductPerformance(
            Integer year,
            Integer month,
            Integer quarter,
            Long categoryId,
            String sortBy,
            int page,
            int size) {
        int targetYear = (year != null) ? year : LocalDate.now(VN_ZONE).getYear();
        LocalDate startDate;
        LocalDate endDate;

        if (month != null && month >= 1 && month <= 12) {
            YearMonth ym = YearMonth.of(targetYear, month);
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        } else if (quarter != null && quarter >= 1 && quarter <= 4) {
            int firstMonth = (quarter - 1) * 3 + 1;
            startDate = LocalDate.of(targetYear, firstMonth, 1);
            endDate = YearMonth.of(targetYear, firstMonth + 2).atEndOfMonth();
        } else {
            // Mặc định cả năm
            startDate = LocalDate.of(targetYear, 1, 1);
            endDate = LocalDate.of(targetYear, 12, 31);
        }

        Instant fromInstant = startDate.atStartOfDay(VN_ZONE).toInstant();
        Instant toInstant = endDate.atTime(LocalTime.MAX).atZone(VN_ZONE).toInstant();

        // 1. Tính tổng doanh thu kỳ để tính tỷ trọng %
        List<Object[]> revenueRows = reportRepository.sumRevenueAndCountOrders(fromInstant, toInstant, EXCLUDED_STATUSES);
        BigDecimal totalPeriodRevenue = BigDecimal.ZERO;
        if (!revenueRows.isEmpty() && revenueRows.get(0) != null && revenueRows.get(0)[0] != null) {
            totalPeriodRevenue = (BigDecimal) revenueRows.get(0)[0];
        }

        // 2. Query trang sản phẩm
        int pageIndex = Math.max(page, 0);
        int pageSize = size > 0 ? size : 10;
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);

        org.springframework.data.domain.Page<Object[]> pagedRows;
        if ("QUANTITY".equalsIgnoreCase(sortBy)) {
            pagedRows = reportRepository.findProductPerformanceByQuantity(fromInstant, toInstant, EXCLUDED_STATUSES, categoryId, pageRequest);
        } else {
            pagedRows = reportRepository.findProductPerformanceByRevenue(fromInstant, toInstant, EXCLUDED_STATUSES, categoryId, pageRequest);
        }

        if (pagedRows.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), pageIndex, pageSize, 0L, 0);
        }

        // 3. Gom danh sách ID sản phẩm trong trang hiện tại để query biến thể
        List<Long> productIds = new ArrayList<>();
        for (Object[] row : pagedRows.getContent()) {
            productIds.add(((Number) row[0]).longValue());
        }

        // 4. Query chi tiết biến thể theo danh sách productIds
        Map<Long, List<ProductVariantBreakdown>> variantMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Object[]> variantRows = reportRepository.findVariantBreakdownByProductIds(fromInstant, toInstant, EXCLUDED_STATUSES, productIds);
            for (Object[] vRow : variantRows) {
                Long pId = ((Number) vRow[0]).longValue();
                Long bId = ((Number) vRow[1]).longValue();
                String kichCo = (String) vRow[2];
                Long qty = ((Number) vRow[3]).longValue();
                BigDecimal amount = (BigDecimal) vRow[4];
                variantMap.computeIfAbsent(pId, k -> new ArrayList<>())
                          .add(new ProductVariantBreakdown(bId, kichCo, qty, amount));
            }
        }

        // 5. Build DTO Response
        List<ProductPerformanceResponse> content = new ArrayList<>();
        for (Object[] row : pagedRows.getContent()) {
            Long prodId = ((Number) row[0]).longValue();
            String prodName = (String) row[1];
            String catName = (String) row[2];
            String imgUrl = (String) row[3];
            Long totalCups = ((Number) row[4]).longValue();
            BigDecimal prodRevenue = (BigDecimal) row[5];

            Double share = 0.0;
            if (totalPeriodRevenue.compareTo(BigDecimal.ZERO) > 0 && prodRevenue != null) {
                share = prodRevenue.multiply(BigDecimal.valueOf(100))
                                   .divide(totalPeriodRevenue, 2, RoundingMode.HALF_UP)
                                   .doubleValue();
            }

            List<ProductVariantBreakdown> variants = variantMap.getOrDefault(prodId, Collections.emptyList());

            content.add(new ProductPerformanceResponse(
                prodId,
                prodName,
                catName,
                imgUrl,
                totalCups,
                prodRevenue,
                share,
                variants
            ));
        }

        return new PageResponse<>(
            content,
            pagedRows.getNumber(),
            pagedRows.getSize(),
            pagedRows.getTotalElements(),
            pagedRows.getTotalPages()
        );
    }
}
