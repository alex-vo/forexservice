package com.example.forexservice.service.validation

import com.example.forexservice.dto.internal.RateDto
import com.example.forexservice.exception.ForexServiceValidationException
import com.example.forexservice.prepareFeeDto
import com.example.forexservice.service.ECBRateService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class FeeValidatorTest {

    @Mock
    lateinit var ecbRateService: ECBRateService

    @InjectMocks
    @Spy
    lateinit var feeValidator: FeeValidator

    @Test
    fun `should validate fee`() {
        val feeDto = prepareFeeDto("EUR", "USD", 0.01)
        doNothing().whenever(feeValidator).validateValue(BigDecimal.valueOf(0.01))
        doNothing().whenever(feeValidator).validateCurrencies(feeDto)

        feeValidator.validateFee(feeDto)

        verify(feeValidator).validateValue(BigDecimal.valueOf(0.01))
        verify(feeValidator).validateCurrencies(feeDto)
    }

    @Test
    fun `should validate currencies - pair exists`() {
        whenever(ecbRateService.getECBRates()).thenReturn(listOf(RateDto("EUR", "USD", BigDecimal.valueOf(1.01))))

        feeValidator.validateCurrencies(prepareFeeDto("EUR", "USD", 0.01))
    }

    @Test
    fun `should validate currencies - pair does not exist`() {
        whenever(ecbRateService.getECBRates()).thenReturn(listOf())

        assertThrows<ForexServiceValidationException> {
            feeValidator.validateCurrencies(prepareFeeDto("EUR", "USD", 0.01))
        }.let {
            assertEquals(it.message, "Fee from EUR to USD not found")
        }
    }

    @Test
    fun `should validate value - negative`() {
        assertThrows<ForexServiceValidationException> { feeValidator.validateValue(BigDecimal.valueOf(-1)) }
            .let { assertEquals(it.message, "Fee cannot be negative") }
    }

    @Test
    fun `should validate value - positive`() {
        feeValidator.validateValue(BigDecimal.ONE)
    }

}