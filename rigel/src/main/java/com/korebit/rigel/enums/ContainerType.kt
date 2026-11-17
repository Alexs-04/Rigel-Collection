package com.korebit.rigel.enums

import java.math.BigDecimal

enum class ContainerType(
    val label: String,
    val suggestedSalePrice: BigDecimal,
    val suggestedBuyoutPrice: BigDecimal,
) {
    LARGE_GLASS_CONTAINER("Vidrio grande", BigDecimal("15.00"), BigDecimal("5.00")),
    SMALL_GLASS_CONTAINER("Vidrio chico", BigDecimal("10.00"), BigDecimal("5.00")),
    LARGE_PLASTIC_CONTAINER("Plastico grande", BigDecimal("10.00"), BigDecimal("5.00")),
    SMALL_PLASTIC_CONTAINER("Plastico chico", BigDecimal("5.00"), BigDecimal("3.00")),
    BEER_CONTAINER("Caguama", BigDecimal("100.00"), BigDecimal("15.00")),
}
