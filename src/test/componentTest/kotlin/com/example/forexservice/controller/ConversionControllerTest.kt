package com.example.forexservice.controller

import com.example.forexservice.dto.internal.ConversionDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ConversionControllerTest : BaseComponentTest() {

    @Test
    fun `should preview conversion`() {
        val result: ConversionDto =
            performSuccessfulUserGet("/v1/conversion?fromCurrency=EUR&toCurrency=USD&amount=100")

        with(result) {
            assertThat(amount).isCloseTo(BigDecimal("103.79"), Offset.offset(BigDecimal.valueOf(0.000001)))
            assertThat(totalFee).isCloseTo(BigDecimal("3.00"), Offset.offset(BigDecimal.valueOf(0.000001)))
        }
    }

}