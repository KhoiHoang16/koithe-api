package com.milktea.order.service.impl;

import com.milktea.cart.entity.*;
import com.milktea.cart.repository.*;
import com.milktea.catalog.entity.*;
import com.milktea.catalog.repository.*;
import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.customer.entity.KhachHang;
import com.milktea.customer.repository.KhachHangRepository;
import com.milktea.order.dto.*;
import com.milktea.order.entity.*;
import com.milktea.order.mapper.*;
import com.milktea.order.repository.*;
import com.milktea.order.service.*;
import com.milktea.security.UserPrincipal;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.repository.NguoiDungRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Set<String> SERVICE_TYPES = Set.of("TAI_CHO", "MANG_VE", "GIAO_HANG");
    private static final Set<String> CHANNELS = Set.of("TAI_QUAY_POS", "QUET_QR_BAN", "UNG_DUNG_APP", "WEBSITE");
    private final DonHangRepository orders;
    private final ChiTietDonHangRepository orderItems;
    private final ToppingDonHangRepository orderToppings;
    private final GioHangRepository carts;
    private final ChiTietGioHangRepository cartItems;
    private final ToppingGioHangRepository cartToppings;
    private final BienTheSanPhamRepository variants;
    private final ToppingRepository toppings;
    private final NguoiDungRepository users;
    private final KhachHangRepository customers;
    private final CaLamViecRepository shifts;
    private final BanRepository tables;
    private final OrderNumberGenerator numbers;
    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final OrderToppingMapper toppingMapper;

    public OrderServiceImpl(DonHangRepository orders, ChiTietDonHangRepository orderItems,
            ToppingDonHangRepository orderToppings, GioHangRepository carts, ChiTietGioHangRepository cartItems,
            ToppingGioHangRepository cartToppings, BienTheSanPhamRepository variants, ToppingRepository toppings,
            NguoiDungRepository users, KhachHangRepository customers, CaLamViecRepository shifts, BanRepository tables,
            OrderNumberGenerator numbers, OrderMapper orderMapper, OrderItemMapper itemMapper, OrderToppingMapper toppingMapper) {
        this.orders=orders; this.orderItems=orderItems; this.orderToppings=orderToppings; this.carts=carts;
        this.cartItems=cartItems; this.cartToppings=cartToppings; this.variants=variants; this.toppings=toppings;
        this.users=users; this.customers=customers; this.shifts=shifts; this.tables=tables;
        this.numbers=numbers; this.orderMapper=orderMapper; this.itemMapper=itemMapper; this.toppingMapper=toppingMapper;
    }

    @Override @Transactional
    public OrderResponse create(CreateOrderRequest request, UserPrincipal principal) {
        validateOrderType(request.serviceType(), request.channel(), request.tableId());
        KhachHang customer = resolveCustomer(principal, request.customerId(), true);
        GioHang cart = null;
        if (request.cartToken() != null && !request.cartToken().isBlank()) {
            cart = carts.findFirstByTokenPhienAndDeletedAtIsNull(request.cartToken())
                    .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy giỏ hàng"));
        } else if (customer != null) {
            cart = carts.findFirstByKhachHangIdAndDeletedAtIsNull(customer.getId())
                    .orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "Giỏ hàng đang trống"));
        }
        if (cart == null) throw error(HttpStatus.BAD_REQUEST, "Cần token giỏ hàng hoặc khách hàng hợp lệ");
        List<ChiTietGioHang> entries = cartItems.findAllByGioHangIdAndDeletedAtIsNullOrderById(cart.getId());
        if (entries.isEmpty()) throw error(HttpStatus.BAD_REQUEST, "Không thể tạo đơn từ giỏ hàng rỗng");
        DonHang order = orders.save(newOrder(request.channel(), request.serviceType(), customer, null, null,
                request.tableId() == null ? null : table(request.tableId())));
        BigDecimal total = BigDecimal.ZERO;
        for (ChiTietGioHang entry : entries) {
            List<ToppingGioHang> chosen = cartToppings.findAllByChiTietGioHangIdAndDeletedAtIsNull(entry.getId());
            total = total.add(addSnapshotLine(order, entry.getBienThe().getId(), entry.getSoLuong(), entry.getMucDuong(), entry.getMucDa(), entry.getGhiChuMon(),
                    chosen.stream().map(t -> new OrderToppingRequest(t.getTopping().getId(), t.getSoLuong())).toList()));
        }
        applyTotals(order, total);
        DonHang saved = orders.save(order);
        for (ChiTietGioHang entry : entries) {
            entry.markDeleted();
            cartToppings.findAllByChiTietGioHangIdAndDeletedAtIsNull(entry.getId()).forEach(ToppingGioHang::markDeleted);
        }
        cart.markDeleted();
        return response(saved);
    }

    @Override @Transactional
    public OrderResponse createPos(CreatePosOrderRequest request) {
        validateOrderType(request.serviceType(), "TAI_QUAY_POS", request.tableId());
        NguoiDung cashier = users.findById(request.cashierId()).filter(NguoiDung::isDangHoatDong)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy thu ngân"));
        CaLamViec shift = shifts.findById(request.shiftId()).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy ca làm việc"));
        if (!Objects.equals(shift.getThuNgan().getId(), cashier.getId()) || !"DANG_MO".equals(shift.getTrangThai()))
            throw error(HttpStatus.BAD_REQUEST, "Ca làm việc không mở hoặc không thuộc thu ngân");
        KhachHang customer = request.customerId() == null ? null : customers.findById(request.customerId())
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
        DonHang order = orders.save(newOrder("TAI_QUAY_POS", request.serviceType(), customer, cashier, shift,
                request.tableId() == null ? null : table(request.tableId())));
        BigDecimal total = BigDecimal.ZERO;
        for (PosOrderItemRequest item : request.items()) total = total.add(addSnapshotLine(order, item.variantId(), item.quantity(),
                item.sugarLevel(), item.iceLevel(), item.note(), item.toppings()));
        applyTotals(order, total);
        return response(orders.save(order));
    }

    @Override @Transactional(readOnly = true)
    public OrderResponse get(Long id, UserPrincipal principal) {
        if (principal == null) throw error(HttpStatus.UNAUTHORIZED, "Cần đăng nhập");
        DonHang order = order(id); authorizeOwnerOrStaff(order, principal, false);
        return response(order);
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<OrderResponse> list(String status, String channel, Pageable pageable) {
        if (status != null && !Set.of("CHO_XAC_NHAN", "DA_THANH_TOAN", "DANG_PHA_CHE", "SAN_SANG", "HOAN_THANH", "DA_HUY").contains(status))
            throw error(HttpStatus.BAD_REQUEST, "Trạng thái đơn hàng không hợp lệ");
        if (channel != null && !CHANNELS.contains(channel)) throw error(HttpStatus.BAD_REQUEST, "Kênh đặt hàng không hợp lệ");
        return PageResponse.from(orders.search(status, channel, pageable).map(this::response));
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(UserPrincipal principal, Pageable pageable) {
        if (principal == null || !"CUSTOMER".equals(principal.role())) throw error(HttpStatus.FORBIDDEN, "Chỉ khách hàng được xem lịch sử đơn");
        KhachHang customer = resolveCustomer(principal, null, false);
        return PageResponse.from(orders.findAllByKhachHangIdAndDeletedAtIsNull(customer.getId(), pageable).map(this::response));
    }

    @Override @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        DonHang order = order(id);
        transition(order, status);
        return response(order);
    }

    @Override @Transactional
    public OrderResponse cancel(Long id, UserPrincipal principal) {
        if (principal == null) throw error(HttpStatus.UNAUTHORIZED, "Cần đăng nhập");
        DonHang order = order(id);
        authorizeOwnerOrStaff(order, principal, true);
        transition(order, "DA_HUY");
        return response(order);
    }

    private DonHang newOrder(String channel, String serviceType, KhachHang customer, NguoiDung cashier, CaLamViec shift, Ban table) {
        DonHang order = new DonHang(); order.setMaHienThiDon(numbers.next()); order.setKenhDatHang(channel);
        order.setLoaiPhucVu(serviceType); order.setKhachHang(customer); order.setThuNgan(cashier);
        order.setCaLamViec(shift); order.setBan(table); order.setTrangThai("CHO_XAC_NHAN"); order.setNgayTao(Instant.now());
        order.setTongTienHang(BigDecimal.ZERO); order.setSoTienGiam(BigDecimal.ZERO); order.setTongTienThanhToan(BigDecimal.ZERO); return order;
    }

    private BigDecimal addSnapshotLine(DonHang order, Long variantId, Integer quantity, String sugar, String ice, String note,
            List<OrderToppingRequest> selectedToppings) {
        BienTheSanPham variant = variants.findByIdAndDeletedAtIsNull(variantId)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể"));
        requireStock(variant.isConHang(), variant.getSoLuongTon(), quantity, "Sản phẩm");
        ChiTietDonHang line = new ChiTietDonHang(); line.setDonHang(order); line.setBienThe(variant); line.setSoLuong(quantity);
        line.setDonGia(variant.getGiaBan()); line.setMucDuong(sugar); line.setMucDa(ice); line.setGhiChuMon(note);
        BigDecimal unit = variant.getGiaBan();
        for (OrderToppingRequest selected : selectedToppings == null ? List.<OrderToppingRequest>of() : selectedToppings) {
            Topping topping = toppings.findByIdAndDeletedAtIsNull(selected.toppingId())
                    .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy topping"));
            int required = Math.multiplyExact(quantity, selected.quantity());
            requireStock(topping.isConHang(), topping.getSoLuongTon(), required, "Topping");
            ToppingDonHang snapshot = new ToppingDonHang(); snapshot.setChiTietDonHang(line); snapshot.setTopping(topping);
            snapshot.setSoLuong(selected.quantity()); snapshot.setDonGia(topping.getGiaBan()); line.getToppings().add(snapshot);
            unit = unit.add(topping.getGiaBan().multiply(BigDecimal.valueOf(selected.quantity())));
        }
        line.setThanhTien(unit.multiply(BigDecimal.valueOf(quantity)));
        orderItems.save(line); order.getChiTiet().add(line);
        line.getToppings().forEach(orderToppings::save);
        return line.getThanhTien();
    }

    private void applyTotals(DonHang order, BigDecimal total) {
        order.setTongTienHang(total); order.setSoTienGiam(BigDecimal.ZERO); order.setTongTienThanhToan(total);
    }

    private void transition(DonHang order, String to) {
        Set<String> allowed = switch (order.getTrangThai()) {
            case "CHO_XAC_NHAN" -> Set.of("DA_THANH_TOAN", "DA_HUY");
            case "DA_THANH_TOAN" -> Set.of("DANG_PHA_CHE", "DA_HUY");
            case "DANG_PHA_CHE" -> Set.of("SAN_SANG", "DA_HUY");
            case "SAN_SANG" -> Set.of("HOAN_THANH", "DA_HUY");
            default -> Set.of();
        };
        if (!allowed.contains(to)) throw error(HttpStatus.BAD_REQUEST, "Không thể chuyển trạng thái từ " + order.getTrangThai() + " sang " + to);
        if ("DA_THANH_TOAN".equals(to)) changeInventory(order, false);
        if ("DA_HUY".equals(to) && order.isTonKhoDaTru()) changeInventory(order, true);
        order.setTrangThai(to);
    }

    private void changeInventory(DonHang order, boolean restore) {
        for (ChiTietDonHang line : orderItems.findAllByDonHangIdAndDeletedAtIsNullOrderById(order.getId())) {
            BienTheSanPham variant = variants.findByIdAndDeletedAtIsNull(line.getBienThe().getId())
                    .orElseThrow(() -> error(HttpStatus.CONFLICT, "Biến thể không còn tồn tại"));
            BigDecimal amount = BigDecimal.valueOf(line.getSoLuong());
            if (!restore && (variant.getSoLuongTon().compareTo(amount) < 0 || !variant.isConHang()))
                throw error(HttpStatus.CONFLICT, "Không đủ tồn kho biến thể " + variant.getKichCo());
            variant.setSoLuongTon(restore ? variant.getSoLuongTon().add(amount) : variant.getSoLuongTon().subtract(amount));
            variant.setConHang(variant.getSoLuongTon().signum() > 0);
            for (ToppingDonHang lineTopping : orderToppings.findAllByChiTietDonHangIdAndDeletedAtIsNull(line.getId())) {
                Topping topping = toppings.findByIdAndDeletedAtIsNull(lineTopping.getTopping().getId())
                        .orElseThrow(() -> error(HttpStatus.CONFLICT, "Topping không còn tồn tại"));
                BigDecimal toppingAmount = BigDecimal.valueOf((long) lineTopping.getSoLuong() * line.getSoLuong());
                if (!restore && (topping.getSoLuongTon().compareTo(toppingAmount) < 0 || !topping.isConHang()))
                    throw error(HttpStatus.CONFLICT, "Không đủ tồn kho topping " + topping.getTenTopping());
                topping.setSoLuongTon(restore ? topping.getSoLuongTon().add(toppingAmount) : topping.getSoLuongTon().subtract(toppingAmount));
                topping.setConHang(topping.getSoLuongTon().signum() > 0);
            }
        }
        order.setTonKhoDaTru(!restore);
    }

    private OrderResponse response(DonHang order) {
        List<OrderItemResponse> rendered = new ArrayList<>();
        for (ChiTietDonHang line : orderItems.findAllByDonHangIdAndDeletedAtIsNullOrderById(order.getId())) {
            List<OrderToppingResponse> selected = orderToppings.findAllByChiTietDonHangIdAndDeletedAtIsNull(line.getId())
                    .stream().map(toppingMapper::toResponse).toList();
            OrderItemResponse mapped = itemMapper.toResponse(line);
            rendered.add(new OrderItemResponse(mapped.id(), mapped.variantId(), mapped.productName(), mapped.size(), mapped.quantity(),
                    mapped.unitPrice(), mapped.lineTotal(), mapped.sugarLevel(), mapped.iceLevel(), mapped.note(), selected));
        }
        OrderResponse mapped = orderMapper.toResponse(order);
        return new OrderResponse(mapped.id(), mapped.displayCode(), mapped.status(), mapped.channel(), mapped.serviceType(),
                mapped.customerId(), mapped.cashierId(), mapped.shiftId(), mapped.tableId(), mapped.merchandiseTotal(),
                mapped.discount(), mapped.payableTotal(), mapped.createdAt(), rendered);
    }

    private DonHang order(Long id) { return orders.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng")); }
    private Ban table(Long id) { return tables.findById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy bàn")); }
    private void validateOrderType(String serviceType, String channel, Long tableId) {
        if (serviceType == null || !SERVICE_TYPES.contains(serviceType)) throw error(HttpStatus.BAD_REQUEST, "Loại phục vụ không hợp lệ");
        if (channel == null || !CHANNELS.contains(channel)) throw error(HttpStatus.BAD_REQUEST, "Kênh đặt hàng không hợp lệ");
        if ("TAI_CHO".equals(serviceType) && tableId == null) throw error(HttpStatus.BAD_REQUEST, "Phục vụ tại chỗ bắt buộc chọn bàn");
    }
    private void requireStock(boolean available, BigDecimal stock, int quantity, String label) {
        if (!available || stock == null || stock.signum() <= 0 || stock.compareTo(BigDecimal.valueOf(quantity)) < 0)
            throw error(HttpStatus.CONFLICT, label + " đã hết hàng hoặc không đủ tồn kho");
    }

    private KhachHang resolveCustomer(UserPrincipal principal, Long requestedId, boolean staffMayChoose) {
        if (principal != null && "CUSTOMER".equals(principal.role())) {
            NguoiDung user = users.findWithVaiTroById(principal.id()).orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"));
            if (user.getSoDienThoai() == null || user.getSoDienThoai().isBlank()) throw error(HttpStatus.BAD_REQUEST, "Tài khoản cần có số điện thoại");
            KhachHang customer = customers.findBySoDienThoai(user.getSoDienThoai()).orElseGet(() -> {
                KhachHang created = new KhachHang(); created.setSoDienThoai(user.getSoDienThoai()); created.setHoVaTen(user.getHoVaTen()); return customers.save(created);
            });
            if (requestedId != null && !requestedId.equals(customer.getId())) throw error(HttpStatus.FORBIDDEN, "Không thể tạo đơn cho khách hàng khác");
            return customer;
        }
        if (requestedId == null) return null;
        boolean allowedStaff = principal != null && Set.of("ADMIN", "MANAGER", "CASHIER").contains(principal.role());
        if (!staffMayChoose || !allowedStaff) throw error(HttpStatus.FORBIDDEN, "Không được chỉ định khách hàng này");
        return customers.findById(requestedId).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
    }

    private void authorizeOwnerOrStaff(DonHang order, UserPrincipal principal, boolean cancel) {
        if ((cancel && Set.of("ADMIN", "MANAGER").contains(principal.role()))
                || (!cancel && Set.of("ADMIN", "MANAGER", "CASHIER", "BARISTA").contains(principal.role()))) return;
        if (!"CUSTOMER".equals(principal.role())) throw error(HttpStatus.FORBIDDEN, "Không có quyền truy cập đơn hàng");
        KhachHang customer = resolveCustomer(principal, null, false);
        if (order.getKhachHang() == null || !Objects.equals(order.getKhachHang().getId(), customer.getId()))
            throw error(HttpStatus.FORBIDDEN, "Đơn hàng không thuộc khách hàng hiện tại");
    }
    private BusinessException error(HttpStatus status, String message) { return new BusinessException(status, message); }
}
