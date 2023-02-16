package com.example.forexservice.service.validation

import com.example.forexservice.exception.ForexServiceValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ConversionAmountValidatorTest {

    private val conversionAmountValidator = ConversionAmountValidator()

    @Test
    fun `should validate conversion amount - negative`() {
        assertThrows<ForexServiceValidationException> {
            conversionAmountValidator.validateConversionAmount(BigDecimal.valueOf(-1))
        }.let {
            assertEquals(it.message, "Conversion amount must be positive")
        }
    }

    @Test
    fun `should validate conversion amount - positive`() {
        conversionAmountValidator.validateConversionAmount(BigDecimal.ONE)
    }

}