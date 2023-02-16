package com.example.forexservice

import com.example.forexservice.dto.internal.FeeDto
import com.example.forexservice.entity.FeeEntity
import java.math.BigDecimal

fun prepareFeeEntity(fromCurrency: String, toCurrency: String, value: Double): FeeEntity {
    val now = System.currentTimeMillis()
    return FeeEntity(fromCurrency, toCurrency, BigDecimal.valueOf(value), now, now)
}

fun prepareFeeDto(fromCurrency: String, toCurrency: String, value: Double): FeeDto {
    return FeeDto(fromCurrency = fromCurrency, toCurrency = toCurrency, value = BigDecimal.valueOf(value))
}