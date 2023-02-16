package com.example.forexservice.controller

import com.example.forexservice.dto.internal.RateDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RatesControllerTest : BaseComponentTest() {

    @Test
    fun `should list rates`() {
        val result: List<RateDto> = performSuccessfulAdminGet("/v1/rates")

        val expectedResponse: List<RateDto> = "src/test/componentTest/resources/integration/rates-response.json"
            .asResourceContent()
            .asDTO()
        assertEquals(result, expectedResponse)
    }

    @Test
    fun `should refresh rates`() {
        val newUSDRate = BigDecimal.valueOf(1.01)
        getMockServerClient().setupECBResponse("src/test/componentTest/resources/integration/ecb-api-second-response.xml".asResourceContent())
        performSuccessfulAdminPut("/v1/rates/refresh")

        val result: RateDto = performSuccessfulAdminGet<List<RateDto>>("/v1/rates")
            .find { it.fromCurrency == "EUR" && it.toCurrency == "USD" }!!

        assertThat(result.rate).isCloseTo(newUSDRate, Offset.offset(BigDecimal.valueOf(0.000001)))
    }

}