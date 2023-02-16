package com.example.forexservice.dto.internal

import java.math.BigDecimal
import java.util.UUID

data class FeeDto(
    val id: UUID? = null,
    val fromCurrency: String,
    val toCurrency: String,
    val value: BigDecimal,
    val createdAt: Long? = null,
    val modifiedAt: Long? = null,
)

data class FeePatchDto(
    val value: BigDecimal,
)

data class RateDto(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: BigDecimal
)

data class ConversionDto(
    val amount: BigDecimal,
    val totalFee: BigDecimal
)