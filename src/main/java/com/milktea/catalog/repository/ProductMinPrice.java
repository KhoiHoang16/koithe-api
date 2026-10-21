package com.milktea.catalog.repository;

import java.math.BigDecimal;

public interface ProductMinPrice {
    Long getProductId();

    BigDecimal getMinPrice();
}
