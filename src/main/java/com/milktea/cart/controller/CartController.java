package com.milktea.cart.controller;

import com.milktea.cart.dto.AddCartItemRequest;
import com.milktea.cart.dto.CartResponse;
import com.milktea.cart.dto.UpdateCartItemRequest;
import com.milktea.cart.service.CartService;
import com.milktea.common.response.ApiResponse;
import com.milktea.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService service;
    public CartController(CartService service) { this.service = service; }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> add(@RequestHeader(value = "X-Cart-Token", required = false) String token,
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = service.addItem(token, principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).header("X-Cart-Token", cart.cartToken() == null ? "" : cart.cartToken())
                .body(ApiResponse.success(cart));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<CartResponse>> current(@RequestHeader(value = "X-Cart-Token", required = false) String token,
            @AuthenticationPrincipal UserPrincipal principal) {
        CartResponse cart = service.current(token, principal);
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (cart.cartToken() != null) response.header("X-Cart-Token", cart.cartToken());
        return response.body(ApiResponse.success(cart));
    }

    @PatchMapping("/items/{id}")
    public ApiResponse<CartResponse> update(@RequestHeader(value = "X-Cart-Token", required = false) String token,
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.success(service.updateItem(token, principal, id, request));
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@RequestHeader(value = "X-Cart-Token", required = false) String token,
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) { service.removeItem(token, principal, id); }

    @DeleteMapping("/current")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@RequestHeader(value = "X-Cart-Token", required = false) String token,
            @AuthenticationPrincipal UserPrincipal principal) { service.clear(token, principal); }
}
