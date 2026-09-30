package com.milktea.catalog.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.milktea.catalog.dto.StockRequest;
import com.milktea.catalog.dto.VariantRequest;
import com.milktea.catalog.entity.BienTheSanPham;
import com.milktea.catalog.entity.SanPham;
import com.milktea.catalog.mapper.VariantMapper;
import com.milktea.catalog.repository.BienTheSanPhamRepository;
import com.milktea.catalog.repository.SanPhamRepository;
import com.milktea.common.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VariantServiceImplTest {
    @Mock BienTheSanPhamRepository variants;
    @Mock SanPhamRepository products;
    @Mock VariantMapper mapper;
    VariantServiceImpl service;

    @BeforeEach void setUp() { service = new VariantServiceImpl(variants, products, mapper); }

    @Test void createRejectsDuplicateSizeWithinProduct() {
        when(products.findById(7L)).thenReturn(Optional.of(new SanPham()));
        when(variants.existsBySanPhamIdAndKichCoIgnoreCaseAndDeletedAtIsNull(7L, "m")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(7L,
                new VariantRequest("m", new BigDecimal("45000"), BigDecimal.TEN)));
    }

    @Test void updateStockSynchronizesAvailabilityWithQuantity() {
        BienTheSanPham variant = new BienTheSanPham();
        when(variants.findById(3L)).thenReturn(Optional.of(variant));

        service.updateStock(3L, new StockRequest(BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, variant.getSoLuongTon());
        assertEquals(false, variant.isConHang());
        verify(mapper).toResponse(variant);
    }

    @Test void createPersistsNormalizedSizeAndAvailability() {
        SanPham product = new SanPham();
        when(products.findById(7L)).thenReturn(Optional.of(product));
        when(variants.existsBySanPhamIdAndKichCoIgnoreCaseAndDeletedAtIsNull(7L, "S")).thenReturn(false);
        when(variants.save(any(BienTheSanPham.class))).thenAnswer(call -> call.getArgument(0));

        service.create(7L, new VariantRequest(" S ", new BigDecimal("42000"), BigDecimal.ONE));

        verify(variants).save(any(BienTheSanPham.class));
    }
}
