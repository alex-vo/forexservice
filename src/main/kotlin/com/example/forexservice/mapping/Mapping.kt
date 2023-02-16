package com.example.forexservice.mapping

import com.example.forexservice.dto.internal.FeeDto
import com.example.forexservice.dto.internal.FeePatchDto
import com.example.forexservice.entity.FeeEntity

fun FeeEntity.toDto(): FeeDto =
    FeeDto(id, fromCurrency, toCurrency, feeValue, createdAt, modifiedAt)

fun FeeEntity.update(feePatchDto: FeePatchDto) = apply {
    feeValue = feePatchDto.value
    modifiedAt = System.currentTimeMillis()
}

fun FeeDto.toEntity(): FeeEntity {
    val now = System.currentTimeMillis()
    return FeeEntity(
        fromCurrency,
        toCurrency,
        value,
        now,
        now,
    )
}