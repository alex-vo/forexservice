package com.example.forexservice.service

import com.example.forexservice.config.FeeProperties
import com.example.forexservice.dto.internal.ConversionDto
import com.example.forexservice.prepareFeeEntity
import com.example.forexservice.repository.FeeRepository
import com.example.forexservice.service.validation.ConversionAmountValidator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class ConversionServiceTest {

    @Mock
    lateinit var feeRepository: FeeRepository

    @Mock
    lateinit var ecbRateService: ECBRateService

    @Mock
    lateinit var conversionAmountValidator: ConversionAmountValidator

    @Mock
    lateinit var feeProperties: FeeProperties

    @InjectMocks
    @Spy
    lateinit var conversionService: ConversionService

    @Test
    fun `should preview conversion`() {
        val conversionDto = ConversionDto(BigDecimal.ONE, BigDecimal.ZERO)
        doReturn(conversionDto).whenever(conversionService).calculateConversion("EUR", "USD", BigDecimal.ONE)

        val result = conversionService.previewConversion("EUR", "USD", BigDecimal.ONE)

        assertEquals(result, conversionDto)
        verify(conversionAmountValidator).validateConversionAmount(BigDecimal.ONE)
    }

    @Test
    fun `should calculate conversion`() {
        val amount = BigDecimal.valueOf(100.3)
        val totalFee = BigDecimal.valueOf(5.5)
        val rate = BigDecimal.valueOf(1.01)
        whenever(ecbRateService.getECBRate("EUR", "USD")).thenReturn(rate)
        doReturn(totalFee).whenever(conversionService)
            .calculateTotalFee("EUR", "USD", amount)

        val result = conversionService.calculateConversion("EUR", "USD", amount)

        assertEquals(ConversionDto((amount - totalFee) * rate, totalFee), result)
    }

    @Test
    fun `should calculate total fee`() {
        val fee1 = prepareFeeEntity("EUR", "USD", 0.01)
        val fee2 = prepareFeeEntity("EUR", "USD", 0.02)
        whenever(feeRepository.findFeesByCurrencies("EUR", "USD"))
            .thenReturn(listOf(fee1, fee2))

        val result = conversionService.calculateTotalFee("EUR", "USD", BigDecimal.valueOf(100))

        assertThat(result).isCloseTo(BigDecimal.valueOf(3.00), Offset.offset(BigDecimal.valueOf(0.000001)))
    }

    @Test
    fun `should apply default fee`() {
        whenever(feeRepository.findFeesByCurrencies("EUR", "USD"))
            .thenReturn(listOf())
        whenever(feeProperties.default).thenReturn(BigDecimal.valueOf(0.01))

        val result = conversionService.calculateTotalFee("EUR", "USD", BigDecimal.valueOf(100))

        assertThat(result).isCloseTo(BigDecimal.valueOf(1.00), Offset.offset(BigDecimal.valueOf(0.000001)))
    }

}