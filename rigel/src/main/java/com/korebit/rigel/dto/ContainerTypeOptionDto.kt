package com.korebit.rigel.dto

import com.korebit.rigel.enums.ContainerType

data class ContainerTypeOptionDto(
    val value: ContainerType,
    val label: String,
    val suggestedSalePrice: Double,
    val suggestedBuyoutPrice: Double,
) {
    companion object {
        fun fromType(type: ContainerType): ContainerTypeOptionDto {
            return ContainerTypeOptionDto(
                value = type,
                label = type.label,
                suggestedSalePrice = type.suggestedSalePrice.toDouble(),
                suggestedBuyoutPrice = type.suggestedBuyoutPrice.toDouble(),
            )
        }
    }
}

