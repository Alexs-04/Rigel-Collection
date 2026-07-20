package com.korebit.rigel.enums;

import java.math.BigDecimal;

public enum ContainerType {
    LARGE_GLASS_CONTAINER("Vidrio grande", new BigDecimal("15.00"), new BigDecimal("5.00")),
    SMALL_GLASS_CONTAINER("Vidrio chico", new BigDecimal("10.00"), new BigDecimal("5.00")),
    LARGE_PLASTIC_CONTAINER("Plastico grande", new BigDecimal("10.00"), new BigDecimal("5.00")),
    SMALL_PLASTIC_CONTAINER("Plastico chico", new BigDecimal("5.00"), new BigDecimal("3.00")),
    BEER_CONTAINER("Caguama", new BigDecimal("100.00"), new BigDecimal("15.00"));

    private final String label;
    private final BigDecimal suggestedSalePrice;
    private final BigDecimal suggestedBuyoutPrice;

    ContainerType(String label, BigDecimal suggestedSalePrice, BigDecimal suggestedBuyoutPrice) {
        this.label = label;
        this.suggestedSalePrice = suggestedSalePrice;
        this.suggestedBuyoutPrice = suggestedBuyoutPrice;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getSuggestedSalePrice() {
        return suggestedSalePrice;
    }

    public BigDecimal getSuggestedBuyoutPrice() {
        return suggestedBuyoutPrice;
    }
}
