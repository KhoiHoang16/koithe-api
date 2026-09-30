package com.milktea.cart.service;

import com.milktea.cart.dto.AddCartItemRequest;
import com.milktea.cart.dto.CartResponse;
import com.milktea.cart.dto.UpdateCartItemRequest;
import com.milktea.security.UserPrincipal;

public interface CartService {
    CartResponse current(String token, UserPrincipal principal);
    CartResponse addItem(String token, UserPrincipal principal, AddCartItemRequest request);
    CartResponse updateItem(String token, UserPrincipal principal, Long itemId, UpdateCartItemRequest request);
    void removeItem(String token, UserPrincipal principal, Long itemId);
    void clear(String token, UserPrincipal principal);
}
