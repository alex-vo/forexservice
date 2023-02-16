package com.example.forexservice.service.validation

import com.example.forexservice.exception.ForexServiceValidationException
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ConversionAmountValidator {

    fun validateConversionAmount(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            throw ForexServiceValidationException("Conversion amount must be positive")
        }
    }

}