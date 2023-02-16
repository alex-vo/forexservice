package com.example.forexservice.controller

import com.example.forexservice.dto.internal.FeeDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class FeesControllerTest : BaseComponentTest() {

    @Test
    fun `should list fees`() {
        val result: List<FeeDto> = performSuccessfulAdminGet("/v1/fees?fromCurrency=EUR&toCurrency=USD")

        assertThat(result)
            .containsExactly(
                FeeDto(
                    UUID.fromString("dc114750-496b-4072-967d-d5108679778e"),
                    "EUR",
                    "USD",
                    BigDecimal.valueOf(0.01),
                    1676548345084,
                    1676548345084
                ),
                FeeDto(
                    UUID.fromString("ded41d82-9339-4e01-9b9e-c005fb117ebc"),
                    "EUR",
                    "USD",
                    BigDecimal.valueOf(0.02),
                    1676548345085,
                    1676548345085
                )
            )
    }

    @Test
    fun `should get fee by id`() {
        val expectedResult = FeeDto(
            UUID.fromString("dc114750-496b-4072-967d-d5108679778e"),
            "EUR",
            "USD",
            BigDecimal.valueOf(0.01),
            1676548345084,
            1676548345084
        )

        val result: FeeDto = performSuccessfulAdminGet("/v1/fees/dc114750-496b-4072-967d-d5108679778e")

        assertEquals(result, expectedResult)
    }

    @Test
    fun `should add fee`() {
        val feeDto = FeeDto(fromCurrency = "EUR", toCurrency = "USD", value = BigDecimal.valueOf(0.03))

        performSuccessfulAdminPost("/v1/fees", feeDto)

        val newResult: List<FeeDto> = performSuccessfulAdminGet("/v1/fees?fromCurrency=EUR&toCurrency=USD")
        assertThat(newResult)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "modifiedAt")
            .contains(feeDto)
    }

    @Test
    fun `should update fee`() {
        val start = System.currentTimeMillis()
        val feeDto = FeeDto(
            UUID.fromString("ded41d82-9339-4e01-9b9e-c005fb117ebc"),
            "EUR",
            "USD",
            BigDecimal.valueOf(0.03),
            1676548345085,
            1676548345085
        )

        performSuccessfulAdminPut("/v1/fees/ded41d82-9339-4e01-9b9e-c005fb117ebc", feeDto)

        val newResult: FeeDto = performSuccessfulAdminGet("/v1/fees/ded41d82-9339-4e01-9b9e-c005fb117ebc")
        assertThat(newResult)
            .usingRecursiveComparison()
            .ignoringFields("modifiedAt")
            .isEqualTo(feeDto)
        assertTrue(newResult.modifiedAt!! > start)
    }

    @Test
    fun `should delete fee`() {
        performSuccessfulAdminDelete("/v1/fees/dc114750-496b-4072-967d-d5108679778e")

        performAdminGet("/v1/fees/dc114750-496b-4072-967d-d5108679778e")
            .andExpect { status { isNotFound() } }
    }

}