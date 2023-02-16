package com.example.forexservice.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValidationTest : BaseComponentTest() {

    @Test
    fun `should fail to preview conversion of negative amount`() {
        val result: Map<String, String> = performUserGet("/v1/conversion?fromCurrency=EUR&toCurrency=USD&amount=-100")
            .andExpect {
                status { isBadRequest() }
            }
            .getResponseDTO()

        assertEquals(result, mapOf("message" to "Conversion amount must be positive"))
    }

}