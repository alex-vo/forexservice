package com.example.forexservice.service.validation

import com.example.forexservice.dto.internal.FeeDto
import com.example.forexservice.exception.ForexServiceValidationException
import com.example.forexservice.service.ECBRateService
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class FeeValidator(
    private val ecbRateService: ECBRateService
) {

    fun validateFee(feeDto: FeeDto) {
        validateValue(feeDto.value)
        validateCurrencies(feeDto)
    }

    fun validateCurrencies(feeDto: FeeDto) {
        ecbRateService.getECBRates()
            .find {
                it.fromCurrency == feeDto.fromCurrency && it.toCurrency == feeDto.toCurrency
            }
            ?: throw ForexServiceValidationException("Fee from ${feeDto.fromCurrency} to ${feeDto.toCurrency} not found")
    }

    fun validateValue(value: BigDecimal) {
        if (value < BigDecimal.ZERO) {
            throw ForexServiceValidationException("Fee cannot be negative")
        }
    }

}