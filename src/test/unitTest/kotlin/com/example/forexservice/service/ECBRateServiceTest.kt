package com.example.forexservice.service

import com.example.forexservice.config.CurrencyProperties
import com.example.forexservice.dto.internal.RateDto
import com.example.forexservice.exception.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.context.ApplicationContext
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class ECBRateServiceTest {
    @Mock
    lateinit var ecbClient: RestTemplate

    @Mock
    lateinit var cacheManager: CacheManager

    @Mock
    lateinit var applicationContext: ApplicationContext

    @Mock
    lateinit var currencyProperties: CurrencyProperties

    @InjectMocks
    @Spy
    lateinit var ecbRateService: ECBRateService

    @Test
    fun `should get ECB rates`() {
        val rateDto = RateDto("EUR", "USD", BigDecimal.valueOf(1.01))
        val rates = listOf(rateDto)
        doReturn(rates).whenever(ecbRateService).fetchRates()

        val result = ecbRateService.getECBRates()

        assertEquals(result, rates)
    }

    @Test
    fun `should clear cache on ECB rate fetch failure and rethrow exception`() {
        val e = RuntimeException()
        doThrow(e).whenever(ecbRateService).fetchRates()

        assertThrows<RuntimeException> { ecbRateService.getECBRates() }
            .let { assertEquals(it, e) }

        verify(ecbRateService).clearRatesCache()
    }

    @Test
    fun `should throw EntityNotFoundException if no rate present`() {
        doReturn(listOf<RateDto>()).whenever(ecbRateService).getECBRates()
        doReturn(ecbRateService).whenever(ecbRateService).self()

        assertThrows<EntityNotFoundException> { ecbRateService.getECBRate("EUR", "USD") }
            .let { assertEquals(it.message, "No rate from EUR to USD found") }
    }

    @Test
    fun `should return currency conversion rate`() {
        doReturn(ecbRateService).whenever(ecbRateService).self()
        doReturn(listOf(RateDto("EUR", "USD", BigDecimal.valueOf(1.01))))
            .whenever(ecbRateService).getECBRates()

        val result = ecbRateService.getECBRate("EUR", "USD")

        assertEquals(BigDecimal.valueOf(1.01), result)
    }

    @Test
    fun `should refresh currency rates`() {
        doReturn(ecbRateService).whenever(ecbRateService).self()
        doReturn(listOf<RateDto>()).whenever(ecbRateService).getECBRates()

        ecbRateService.refreshCurrencyRates()

        verify(ecbRateService).clearRatesCache()
    }

    @Test
    fun `should clear rates cache`() {
        val cache = mock(Cache::class.java)
        whenever(cacheManager.getCache("rates")).thenReturn(cache)

        ecbRateService.clearRatesCache()

        verify(cache).clear()
    }
}