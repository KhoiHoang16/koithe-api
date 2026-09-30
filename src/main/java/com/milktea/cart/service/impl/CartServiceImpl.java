package com.milktea.cart.service.impl;

import com.milktea.cart.dto.*;
import com.milktea.cart.entity.*;
import com.milktea.cart.mapper.CartItemMapper;
import com.milktea.cart.mapper.CartMapper;
import com.milktea.cart.repository.*;
import com.milktea.cart.service.CartService;
import com.milktea.catalog.entity.BienTheSanPham;
import com.milktea.catalog.entity.Topping;
import com.milktea.catalog.repository.BienTheSanPhamRepository;
import com.milktea.catalog.repository.ToppingRepository;
import com.milktea.common.exception.BusinessException;
import com.milktea.customer.entity.KhachHang;
import com.milktea.customer.repository.KhachHangRepository;
import com.milktea.security.UserPrincipal;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.repository.NguoiDungRepository;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private final GioHangRepository carts;
    private final ChiTietGioHangRepository items;
    private final ToppingGioHangRepository cartToppings;
    private final BienTheSanPhamRepository variants;
    private final ToppingRepository toppings;
    private final NguoiDungRepository users;
    private final KhachHangRepository customers;
    private final CartMapper cartMapper;
    private final CartItemMapper itemMapper;

    public CartServiceImpl(GioHangRepository carts, ChiTietGioHangRepository items,
            ToppingGioHangRepository cartToppings, BienTheSanPhamRepository variants,
            ToppingRepository toppings, NguoiDungRepository users, KhachHangRepository customers,
            CartMapper cartMapper, CartItemMapper itemMapper) {
        this.carts = carts; this.items = items; this.cartToppings = cartToppings;
        this.variants = variants; this.toppings = toppings; this.users = users;
        this.customers = customers; this.cartMapper = cartMapper; this.itemMapper = itemMapper;
    }

    @Override
    public CartResponse current(String token, UserPrincipal principal) { return response(resolve(token, principal)); }

    @Override
    public CartResponse addItem(String token, UserPrincipal principal, AddCartItemRequest request) {
        GioHang cart = resolve(token, principal);
        BienTheSanPham variant = variants.findByIdAndDeletedAtIsNull(request.variantId())
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể"));
        requireStock(variant.isConHang(), variant.getSoLuongTon(), request.quantity(), "Sản phẩm");
        ChiTietGioHang item = new ChiTietGioHang();
        item.setGioHang(cart); item.setBienThe(variant); item.setSoLuong(request.quantity());
        item.setMucDuong(request.sugarLevel()); item.setMucDa(request.iceLevel()); item.setGhiChuMon(request.note());
        items.save(item);
        if (request.toppings() != null) for (CartToppingRequest toppingRequest : request.toppings()) {
            addTopping(item, toppingRequest);
        }
        recalculate(item);
        cart.setThoiGianCapNhat(java.time.Instant.now());
        return response(cart);
    }

    @Override
    public CartResponse updateItem(String token, UserPrincipal principal, Long itemId, UpdateCartItemRequest request) {
        GioHang cart = resolve(token, principal);
        ChiTietGioHang item = items.findByIdAndGioHangIdAndDeletedAtIsNull(itemId, cart.getId())
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy món trong giỏ"));
        if (request.quantity() != null) {
            requireStock(item.getBienThe().isConHang(), item.getBienThe().getSoLuongTon(), request.quantity(), "Sản phẩm");
            item.setSoLuong(request.quantity());
        }
        if (request.sugarLevel() != null) item.setMucDuong(request.sugarLevel());
        if (request.iceLevel() != null) item.setMucDa(request.iceLevel());
        recalculate(item);
        return response(cart);
    }

    @Override
    public void removeItem(String token, UserPrincipal principal, Long itemId) {
        GioHang cart = resolve(token, principal);
        ChiTietGioHang item = items.findByIdAndGioHangIdAndDeletedAtIsNull(itemId, cart.getId())
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy món trong giỏ"));
        item.markDeleted();
        cartToppings.findAllByChiTietGioHangIdAndDeletedAtIsNull(item.getId()).forEach(ToppingGioHang::markDeleted);
    }

    @Override
    public void clear(String token, UserPrincipal principal) {
        GioHang cart = resolve(token, principal);
        items.findAllByGioHangIdAndDeletedAtIsNullOrderById(cart.getId()).forEach(item -> {
            item.markDeleted();
            cartToppings.findAllByChiTietGioHangIdAndDeletedAtIsNull(item.getId()).forEach(ToppingGioHang::markDeleted);
        });
    }

    private GioHang resolve(String token, UserPrincipal principal) {
        KhachHang customer = customer(principal);
        if (customer != null) {
            GioHang customerCart = carts.findFirstByKhachHangIdAndDeletedAtIsNull(customer.getId()).orElse(null);
            GioHang guestCart = validToken(token) ? carts.findFirstByTokenPhienAndDeletedAtIsNull(token).orElse(null) : null;
            if (customerCart == null) {
                customerCart = new GioHang(); customerCart.setKhachHang(customer); customerCart.setTokenPhien(UUID.randomUUID().toString());
                customerCart = carts.save(customerCart);
            }
            if (guestCart != null && !Objects.equals(guestCart.getId(), customerCart.getId())) {
                for (ChiTietGioHang item : items.findAllByGioHangIdAndDeletedAtIsNullOrderById(guestCart.getId())) {
                    item.setGioHang(customerCart);
                }
                guestCart.markDeleted();
            }
            return customerCart;
        }
        if (validToken(token)) {
            GioHang existing = carts.findFirstByTokenPhienAndDeletedAtIsNull(token).orElse(null);
            if (existing != null) return existing;
        }
        GioHang created = new GioHang(); created.setTokenPhien(UUID.randomUUID().toString());
        return carts.save(created);
    }

    private KhachHang customer(UserPrincipal principal) {
        if (principal == null || !"CUSTOMER".equals(principal.role())) return null;
        NguoiDung user = users.findWithVaiTroById(principal.id())
                .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"));
        if (user.getSoDienThoai() == null || user.getSoDienThoai().isBlank())
            throw error(HttpStatus.BAD_REQUEST, "Tài khoản khách hàng cần có số điện thoại để sử dụng giỏ hàng");
        return customers.findBySoDienThoai(user.getSoDienThoai()).orElseGet(() -> {
            KhachHang newCustomer = new KhachHang(); newCustomer.setSoDienThoai(user.getSoDienThoai()); newCustomer.setHoVaTen(user.getHoVaTen());
            return customers.save(newCustomer);
        });
    }

    private boolean validToken(String token) {
        if (token == null || token.isBlank()) return false;
        try { UUID.fromString(token); return true; }
        catch (IllegalArgumentException ex) { throw error(HttpStatus.BAD_REQUEST, "X-Cart-Token không hợp lệ"); }
    }

    private void addTopping(ChiTietGioHang item, CartToppingRequest request) {
        Topping topping = toppings.findByIdAndDeletedAtIsNull(request.toppingId())
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy topping"));
        requireStock(topping.isConHang(), topping.getSoLuongTon(), request.quantity(), "Topping");
        ToppingGioHang row = new ToppingGioHang(); row.setChiTietGioHang(item); row.setTopping(topping); row.setSoLuong(request.quantity());
        cartToppings.save(row);
    }

    private void requireStock(boolean available, BigDecimal stock, int quantity, String label) {
        if (!available || stock == null || stock.compareTo(BigDecimal.ZERO) <= 0 || stock.compareTo(BigDecimal.valueOf(quantity)) < 0)
            throw error(HttpStatus.CONFLICT, label + " đã hết hàng hoặc không đủ tồn kho");
    }

    private BigDecimal recalculate(ChiTietGioHang item) {
        BigDecimal unit = item.getBienThe().getGiaBan();
        List<ToppingGioHang> selected = cartToppings.findAllByChiTietGioHangIdAndDeletedAtIsNull(item.getId());
        for (ToppingGioHang row : selected) unit = unit.add(row.getTopping().getGiaBan().multiply(BigDecimal.valueOf(row.getSoLuong())));
        BigDecimal total = unit.multiply(BigDecimal.valueOf(item.getSoLuong()));
        item.setThanhTien(total);
        return total;
    }

    private CartResponse response(GioHang cart) {
        List<CartItemResponse> rendered = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ChiTietGioHang item : items.findAllByGioHangIdAndDeletedAtIsNullOrderById(cart.getId())) {
            List<ToppingGioHang> selected = cartToppings.findAllByChiTietGioHangIdAndDeletedAtIsNull(item.getId());
            BigDecimal unit = item.getBienThe().getGiaBan();
            for (ToppingGioHang row : selected) unit = unit.add(row.getTopping().getGiaBan().multiply(BigDecimal.valueOf(row.getSoLuong())));
            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(item.getSoLuong()));
            List<CartToppingResponse> toppingResponses = selected.stream().map(itemMapper::toResponse).toList();
            CartItemResponse mapped = itemMapper.toResponse(item);
            rendered.add(new CartItemResponse(mapped.id(), mapped.variantId(), mapped.productName(), mapped.size(), mapped.quantity(),
                    mapped.sugarLevel(), mapped.iceLevel(), mapped.note(), toppingResponses, unit, lineTotal));
            total = total.add(lineTotal);
            item.setThanhTien(lineTotal);
        }
        CartResponse mapped = cartMapper.toResponse(cart);
        return new CartResponse(mapped.id(), cart.getKhachHang() == null ? cart.getTokenPhien() : null, rendered, total);
    }

    private BusinessException error(HttpStatus status, String message) { return new BusinessException(status, message); }
}
