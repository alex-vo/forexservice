package com.example.forexservice.controller

import org.junit.jupiter.api.Test

class AccessControlTest : BaseComponentTest() {

    @Test
    fun `should fail to call admin API with user creds`() {
        performUserGet("/v1/rates")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `should fail to call admin API without creds`() {
        performGet("/v1/fees")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `should successfully call admin API`() {
        performAdminGet("/v1/rates")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `should fail to call user API without creds`() {
        performGet("/v1/conversion")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `should successfully call user API with admin creds`() {
        performAdminGet("/v1/conversion?fromCurrency=EUR&toCurrency=USD&amount=100")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `should successfully call user API with user creds`() {
        performUserGet("/v1/conversion?fromCurrency=EUR&toCurrency=USD&amount=100")
            .andExpect {
                status { isOk() }
            }
    }

}