package com.milktea.order.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.cart.entity.*;
import com.milktea.cart.repository.*;
import com.milktea.catalog.entity.*;
import com.milktea.catalog.repository.*;
import com.milktea.common.exception.BusinessException;
import com.milktea.customer.repository.KhachHangRepository;
import com.milktea.order.dto.*;
import com.milktea.order.entity.*;
import com.milktea.order.mapper.*;
import com.milktea.order.repository.*;
import com.milktea.order.service.OrderNumberGenerator;
import com.milktea.security.UserPrincipal;
import com.milktea.shift.entity.CaLamViec;
import com.milktea.shift.repository.CaLamViecRepository;
import com.milktea.table.entity.Ban;
import com.milktea.table.repository.BanRepository;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.repository.NguoiDungRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceImplTest {
    DonHangRepository orders = mock(DonHangRepository.class);
    ChiTietDonHangRepository orderItems = mock(ChiTietDonHangRepository.class);
    ToppingDonHangRepository orderToppings = mock(ToppingDonHangRepository.class);
    GioHangRepository carts = mock(GioHangRepository.class);
    ChiTietGioHangRepository cartItems = mock(ChiTietGioHangRepository.class);
    ToppingGioHangRepository cartToppings = mock(ToppingGioHangRepository.class);
    BienTheSanPhamRepository variants = mock(BienTheSanPhamRepository.class);
    ToppingRepository toppings = mock(ToppingRepository.class);
    NguoiDungRepository users = mock(NguoiDungRepository.class);
    KhachHangRepository customers = mock(KhachHangRepository.class);
    CaLamViecRepository shifts = mock(CaLamViecRepository.class);
    BanRepository tables = mock(BanRepository.class);
    OrderNumberGenerator numbers = mock(OrderNumberGenerator.class);
    OrderMapper orderMapper = mock(OrderMapper.class);
    OrderItemMapper itemMapper = mock(OrderItemMapper.class);
    OrderToppingMapper toppingMapper = mock(OrderToppingMapper.class);
    OrderServiceImpl service = new OrderServiceImpl(orders, orderItems, orderToppings, carts, cartItems, cartToppings,
            variants, toppings, users, customers, shifts, tables, numbers, orderMapper, itemMapper, toppingMapper);
    List<ChiTietDonHang> savedItems = new ArrayList<>();
    List<ToppingDonHang> savedToppings = new ArrayList<>();
    AtomicLong nextId = new AtomicLong(100);

    @BeforeEach void setUp() {
        savedItems.clear(); savedToppings.clear();
        when(numbers.next()).thenReturn("DH-2026-001", "DH-2026-002", "DH-2026-003");
        when(orders.save(any())).thenAnswer(call -> { DonHang order = call.getArgument(0); if (order.getId() == null) order.setId(nextId.incrementAndGet()); return order; });
        when(orderItems.save(any())).thenAnswer(call -> { ChiTietDonHang item = call.getArgument(0); if (item.getId() == null) item.setId(nextId.incrementAndGet()); savedItems.add(item); return item; });
        when(orderToppings.save(any())).thenAnswer(call -> { ToppingDonHang topping = call.getArgument(0); if (topping.getId() == null) topping.setId(nextId.incrementAndGet()); savedToppings.add(topping); return topping; });
        when(orderItems.findAllByDonHangIdAndDeletedAtIsNullOrderById(anyLong())).thenAnswer(call -> savedItems.stream()
                .filter(item -> item.getDonHang().getId().equals(call.getArgument(0))).toList());
        when(orderToppings.findAllByChiTietDonHangIdAndDeletedAtIsNull(anyLong())).thenAnswer(call -> savedToppings.stream()
                .filter(t -> t.getChiTietDonHang().getId().equals(call.getArgument(0))).toList());
        when(orderMapper.toResponse(any())).thenAnswer(call -> { DonHang order = call.getArgument(0);
            return new OrderResponse(order.getId(), order.getMaHienThiDon(), order.getTrangThai(), order.getKenhDatHang(), order.getLoaiPhucVu(),
                    null, order.getThuNgan() == null ? null : order.getThuNgan().getId(), order.getCaLamViec() == null ? null : order.getCaLamViec().getId(),
                    order.getBan() == null ? null : order.getBan().getId(), order.getTongTienHang(), order.getSoTienGiam(), order.getTongTienThanhToan(),
                    order.getNgayTao(), List.of()); });
        when(itemMapper.toResponse(any())).thenAnswer(call -> { ChiTietDonHang line = call.getArgument(0);
            return new OrderItemResponse(line.getId(), line.getBienThe().getId(), "Milk Tea", line.getBienThe().getKichCo(), line.getSoLuong(),
                    line.getDonGia(), line.getThanhTien(), line.getMucDuong(), line.getMucDa(), line.getGhiChuMon(), List.of()); });
        when(toppingMapper.toResponse(any())).thenAnswer(call -> { ToppingDonHang line = call.getArgument(0);
            return new OrderToppingResponse(line.getId(), line.getTopping().getId(), line.getTopping().getTenTopping(), line.getSoLuong(), line.getDonGia(),
                    line.getDonGia().multiply(BigDecimal.valueOf(line.getSoLuong()))); });
    }

    @Test void createsFromCartAndFreezesCurrentVariantAndToppingPrices() {
        String token = UUID.randomUUID().toString();
        GioHang cart = new GioHang(); cart.setId(1L); cart.setTokenPhien(token);
        BienTheSanPham variant = variant(5L, "M", "40000", "10");
        Topping topping = topping(8L, "Pearls", "8000", "10");
        ChiTietGioHang cartLine = new ChiTietGioHang(); cartLine.setId(10L); cartLine.setGioHang(cart); cartLine.setBienThe(variant); cartLine.setSoLuong(2);
        ToppingGioHang cartTopping = new ToppingGioHang(); cartTopping.setId(11L); cartTopping.setChiTietGioHang(cartLine); cartTopping.setTopping(topping); cartTopping.setSoLuong(1);
        when(carts.findFirstByTokenPhienAndDeletedAtIsNull(token)).thenReturn(Optional.of(cart));
        when(cartItems.findAllByGioHangIdAndDeletedAtIsNullOrderById(1L)).thenReturn(List.of(cartLine));
        when(cartToppings.findAllByChiTietGioHangIdAndDeletedAtIsNull(10L)).thenReturn(List.of(cartTopping));
        when(variants.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(variant));
        when(toppings.findByIdAndDeletedAtIsNull(8L)).thenReturn(Optional.of(topping));

        OrderResponse response = service.create(new CreateOrderRequest(token, null, "MANG_VE", "WEBSITE", null), null);

        assertEquals("DH-2026-001", response.displayCode());
        assertEquals(new BigDecimal("96000"), response.payableTotal());
        assertEquals(new BigDecimal("40000"), savedItems.getFirst().getDonGia());
        assertEquals(new BigDecimal("8000"), savedToppings.getFirst().getDonGia());
        variant.setGiaBan(new BigDecimal("50000")); topping.setGiaBan(new BigDecimal("10000"));
        assertEquals(new BigDecimal("40000"), savedItems.getFirst().getDonGia());
        assertNotNull(cart.getDeletedAt());
    }

    @Test void createsDirectPosOrderWithRequiredCashierAndOpenShift() {
        NguoiDung cashier = new NguoiDung(); cashier.setId(3L); cashier.setDangHoatDong(true);
        CaLamViec shift = new CaLamViec(); shift.setId(4L); shift.setThuNgan(cashier); shift.setTrangThai("DANG_MO");
        BienTheSanPham variant = variant(5L, "M", "40000", "10");
        when(users.findById(3L)).thenReturn(Optional.of(cashier)); when(shifts.findById(4L)).thenReturn(Optional.of(shift));
        when(variants.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(variant));
        CreatePosOrderRequest request = new CreatePosOrderRequest(3L, 4L,
                List.of(new PosOrderItemRequest(5L, 1, null, null, null, List.of())), "MANG_VE", null, null);

        OrderResponse response = service.createPos(request);

        assertEquals("TAI_QUAY_POS", response.channel()); assertEquals(3L, response.cashierId()); assertEquals(4L, response.shiftId());
    }

    @Test void paymentDeductsInventoryAndCancelRestoresIt() {
        DonHang order = new DonHang(); order.setId(9L); order.setMaHienThiDon("DH-2026-010"); order.setKenhDatHang("WEBSITE");
        order.setLoaiPhucVu("MANG_VE"); order.setTrangThai("CHO_XAC_NHAN"); order.setTongTienHang(new BigDecimal("96000"));
        order.setSoTienGiam(BigDecimal.ZERO); order.setTongTienThanhToan(new BigDecimal("96000"));
        BienTheSanPham variant = variant(5L, "M", "40000", "8"); Topping topping = topping(8L, "Pearls", "8000", "10");
        ChiTietDonHang line = new ChiTietDonHang(); line.setId(20L); line.setDonHang(order); line.setBienThe(variant); line.setSoLuong(2); line.setDonGia(new BigDecimal("40000")); line.setThanhTien(new BigDecimal("96000"));
        ToppingDonHang topLine = new ToppingDonHang(); topLine.setId(21L); topLine.setChiTietDonHang(line); topLine.setTopping(topping); topLine.setSoLuong(1); topLine.setDonGia(new BigDecimal("8000"));
        when(orders.findByIdAndDeletedAtIsNull(9L)).thenReturn(Optional.of(order));
        savedItems.add(line); savedToppings.add(topLine);
        when(variants.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(variant));
        when(toppings.findByIdAndDeletedAtIsNull(8L)).thenReturn(Optional.of(topping));

        service.updateStatus(9L, "DA_THANH_TOAN");
        assertEquals(new BigDecimal("6"), variant.getSoLuongTon()); assertEquals(new BigDecimal("8"), topping.getSoLuongTon());
        service.cancel(9L, new UserPrincipal(1L, "admin", "", true, "ADMIN"));
        assertEquals(new BigDecimal("8"), variant.getSoLuongTon()); assertEquals(new BigDecimal("10"), topping.getSoLuongTon());
    }

    @Test void rejectsIllegalStateTransition() {
        DonHang order = new DonHang(); order.setId(77L); order.setTrangThai("CHO_XAC_NHAN");
        when(orders.findByIdAndDeletedAtIsNull(77L)).thenReturn(Optional.of(order));
        BusinessException failure = assertThrows(BusinessException.class, () -> service.updateStatus(77L, "HOAN_THANH"));
        assertEquals(400, failure.getStatus().value());
    }

    private BienTheSanPham variant(Long id, String size, String price, String stock) {
        BienTheSanPham value = new BienTheSanPham(); value.setId(id); value.setKichCo(size); value.setGiaBan(new BigDecimal(price));
        value.setSoLuongTon(new BigDecimal(stock)); value.setConHang(new BigDecimal(stock).signum() > 0); return value;
    }
    private Topping topping(Long id, String name, String price, String stock) {
        Topping value = new Topping(); value.setId(id); value.setTenTopping(name); value.setGiaBan(new BigDecimal(price));
        value.setSoLuongTon(new BigDecimal(stock)); value.setConHang(new BigDecimal(stock).signum() > 0); return value;
    }
}
