package com.example.forexservice.service

import com.example.forexservice.config.CurrencyProperties
import com.example.forexservice.dto.external.Envelope
import com.example.forexservice.dto.internal.RateDto
import com.example.forexservice.exception.EntityNotFoundException
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class ECBRateService(
    private val ecbClient: RestTemplate,
    private val cacheManager: CacheManager,
    private val applicationContext: ApplicationContext,
    private val currencyProperties: CurrencyProperties
) {

    @Cacheable("rates")
    fun getECBRates(): List<RateDto> =
        runCatching { fetchRates() }
            .onFailure {
                clearRatesCache()
            }
            .getOrThrow()

    fun getECBRate(fromCurrency: String, toCurrency: String): BigDecimal =
        self().getECBRates()
            .find { it.fromCurrency == fromCurrency && it.toCurrency == toCurrency }
            ?.rate
            ?: throw EntityNotFoundException("No rate from $fromCurrency to $toCurrency found")

    fun refreshCurrencyRates() {
        clearRatesCache()
        self().getECBRates()
    }

    fun self(): ECBRateService = applicationContext.getBean(ECBRateService::class.java)

    fun fetchRates(): List<RateDto> =
        ecbClient.getForObject<Envelope>("/stats/eurofxref/eurofxref-daily.xml")
            .cube!!
            .innerCubes
            .flatMap {
                listOf(
                    RateDto(
                        currencyProperties.main,
                        it.currency!!,
                        it.rate!!.setScale(currencyProperties.rateScale, RoundingMode.FLOOR)
                    ),
                    RateDto(
                        it.currency!!,
                        currencyProperties.main,
                        BigDecimal.ONE.divide(it.rate!!, currencyProperties.rateScale, RoundingMode.FLOOR)
                    ),
                )
            }

    fun clearRatesCache() {
        cacheManager.getCache("rates")
            ?.clear()
    }

}
