package com.example.forexservice.service

import com.example.forexservice.config.FeeProperties
import com.example.forexservice.dto.internal.ConversionDto
import com.example.forexservice.repository.FeeRepository
import com.example.forexservice.service.validation.ConversionAmountValidator
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ConversionService(
    private val feeRepository: FeeRepository,
    private val ecbRateService: ECBRateService,
    private val conversionAmountValidator: ConversionAmountValidator,
    private val feeProperties: FeeProperties
) {

    fun previewConversion(fromCurrency: String, toCurrency: String, amount: BigDecimal): ConversionDto {
        conversionAmountValidator.validateConversionAmount(amount)

        return calculateConversion(fromCurrency, toCurrency, amount)
    }

    fun calculateConversion(fromCurrency: String, toCurrency: String, amount: BigDecimal): ConversionDto {
        val rate = ecbRateService.getECBRate(fromCurrency, toCurrency)
        val totalFee = calculateTotalFee(fromCurrency, toCurrency, amount)

        return ConversionDto((amount - totalFee) * rate, totalFee)
    }

    fun calculateTotalFee(fromCurrency: String, toCurrency: String, amount: BigDecimal): BigDecimal =
        feeRepository.findFeesByCurrencies(fromCurrency, toCurrency)
            .map { it.value }
            .ifEmpty { listOf(feeProperties.default) }
            .sumOf { amount * it }

}