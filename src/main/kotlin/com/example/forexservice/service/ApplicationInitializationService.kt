package com.example.forexservice.service

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ApplicationInitializationService(
    private val ecbRateService: ECBRateService,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun setUp() {
        ecbRateService.refreshCurrencyRates()
    }

}