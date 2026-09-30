package com.milktea.cart.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.cart.dto.*;
import com.milktea.cart.entity.*;
import com.milktea.cart.mapper.CartItemMapper;
import com.milktea.cart.mapper.CartMapper;
import com.milktea.cart.repository.*;
import com.milktea.catalog.entity.BienTheSanPham;
import com.milktea.catalog.entity.Topping;
import com.milktea.catalog.repository.BienTheSanPhamRepository;
import com.milktea.catalog.repository.ToppingRepository;
import com.milktea.common.exception.BusinessException;
import com.milktea.customer.entity.KhachHang;
import com.milktea.customer.repository.KhachHangRepository;
import com.milktea.security.UserPrincipal;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.entity.VaiTro;
import com.milktea.user.repository.NguoiDungRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartServiceImplTest {
    GioHangRepository carts = mock(GioHangRepository.class);
    ChiTietGioHangRepository items = mock(ChiTietGioHangRepository.class);
    ToppingGioHangRepository cartToppings = mock(ToppingGioHangRepository.class);
    BienTheSanPhamRepository variants = mock(BienTheSanPhamRepository.class);
    ToppingRepository toppings = mock(ToppingRepository.class);
    NguoiDungRepository users = mock(NguoiDungRepository.class);
    KhachHangRepository customers = mock(KhachHangRepository.class);
    CartMapper cartMapper = mock(CartMapper.class);
    CartItemMapper itemMapper = mock(CartItemMapper.class);
    CartServiceImpl service = new CartServiceImpl(carts, items, cartToppings, variants, toppings, users, customers, cartMapper, itemMapper);

    @BeforeEach void commonStubs() {
        when(cartMapper.toResponse(any())).thenAnswer(call -> {
            GioHang cart = call.getArgument(0); return new CartResponse(cart.getId(), cart.getTokenPhien(), List.of(), BigDecimal.ZERO);
        });
        when(itemMapper.toResponse(any(ChiTietGioHang.class))).thenAnswer(call -> {
            ChiTietGioHang item = call.getArgument(0);
            return new CartItemResponse(item.getId(), item.getBienThe().getId(), "Milk Tea", item.getBienThe().getKichCo(),
                    item.getSoLuong(), item.getMucDuong(), item.getMucDa(), item.getGhiChuMon(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO);
        });
        when(itemMapper.toResponse(any(ToppingGioHang.class))).thenAnswer(call -> {
            ToppingGioHang row = call.getArgument(0);
            return new CartToppingResponse(row.getId(), row.getTopping().getId(), row.getTopping().getTenTopping(), row.getSoLuong(), row.getTopping().getGiaBan());
        });
    }

    @Test void computesCurrentLineTotalIncludingToppings() {
        String token = "f4713f1f-9ec8-4ba3-a8c5-f86d69e33a92";
        GioHang cart = new GioHang(); cart.setId(1L); cart.setTokenPhien(token);
        when(carts.findFirstByTokenPhienAndDeletedAtIsNull(token)).thenReturn(Optional.of(cart));
        BienTheSanPham variant = variant(10L, "M", "40000", "5");
        when(variants.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(variant));
        List<ChiTietGioHang> savedItems = new ArrayList<>();
        List<ToppingGioHang> savedToppings = new ArrayList<>();
        when(items.save(any())).thenAnswer(call -> { ChiTietGioHang item = call.getArgument(0); item.setId(20L); savedItems.add(item); return item; });
        Topping topping = new Topping(); topping.setId(30L); topping.setTenTopping("Pearls"); topping.setGiaBan(new BigDecimal("8000")); topping.setSoLuongTon(new BigDecimal("5")); topping.setConHang(true);
        when(toppings.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(topping));
        when(cartToppings.save(any())).thenAnswer(call -> { ToppingGioHang row = call.getArgument(0); row.setId(31L); savedToppings.add(row); return row; });
        when(cartToppings.findAllByChiTietGioHangIdAndDeletedAtIsNull(20L)).thenReturn(savedToppings);
        when(items.findAllByGioHangIdAndDeletedAtIsNullOrderById(1L)).thenReturn(savedItems);

        CartResponse response = service.addItem(token, null, new AddCartItemRequest(10L, 2, "50%", "Less", null,
                List.of(new CartToppingRequest(30L, 1))));

        assertEquals(new BigDecimal("96000"), response.total());
        assertEquals(new BigDecimal("48000"), response.items().getFirst().unitPrice());
    }

    @Test void mergesGuestItemsIntoCustomerCartOnCustomerRequest() {
        String token = "f4713f1f-9ec8-4ba3-a8c5-f86d69e33a92";
        KhachHang customer = new KhachHang(); customer.setId(7L); customer.setSoDienThoai("0900000000");
        GioHang customerCart = new GioHang(); customerCart.setId(2L); customerCart.setKhachHang(customer); customerCart.setTokenPhien("new-token");
        GioHang guestCart = new GioHang(); guestCart.setId(1L); guestCart.setTokenPhien(token);
        ChiTietGioHang guestItem = new ChiTietGioHang(); guestItem.setId(11L); guestItem.setGioHang(guestCart);
        VaiTro role = new VaiTro(); role.setTenVaiTro("CUSTOMER");
        NguoiDung user = new NguoiDung(); user.setId(9L); user.setVaiTro(role); user.setSoDienThoai(customer.getSoDienThoai());
        when(users.findWithVaiTroById(9L)).thenReturn(Optional.of(user));
        when(customers.findBySoDienThoai(customer.getSoDienThoai())).thenReturn(Optional.of(customer));
        when(carts.findFirstByKhachHangIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(customerCart));
        when(carts.findFirstByTokenPhienAndDeletedAtIsNull(token)).thenReturn(Optional.of(guestCart));
        when(items.findAllByGioHangIdAndDeletedAtIsNullOrderById(1L)).thenReturn(List.of(guestItem));
        when(items.findAllByGioHangIdAndDeletedAtIsNullOrderById(2L)).thenReturn(List.of());

        service.current(token, new UserPrincipal(9L, "customer", "", true, "CUSTOMER"));

        assertEquals(customerCart, guestItem.getGioHang());
        assertEquals(false, guestCart.getDeletedAt() == null);
    }

    @Test void rejectsAddingOutOfStockVariant() {
        String token = "f4713f1f-9ec8-4ba3-a8c5-f86d69e33a92";
        GioHang cart = new GioHang(); cart.setId(1L); cart.setTokenPhien(token);
        when(carts.findFirstByTokenPhienAndDeletedAtIsNull(token)).thenReturn(Optional.of(cart));
        when(variants.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(variant(10L, "M", "40000", "0")));

        assertThrows(BusinessException.class, () -> service.addItem(token, null,
                new AddCartItemRequest(10L, 1, null, null, null, List.of())));
        verify(items, never()).save(any());
    }

    private BienTheSanPham variant(Long id, String size, String price, String stock) {
        BienTheSanPham variant = new BienTheSanPham(); variant.setId(id); variant.setKichCo(size);
        variant.setGiaBan(new BigDecimal(price)); variant.setSoLuongTon(new BigDecimal(stock)); variant.setConHang(new BigDecimal(stock).signum() > 0);
        return variant;
    }
}
