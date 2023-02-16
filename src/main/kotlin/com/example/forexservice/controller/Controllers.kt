package com.example.forexservice.controller

import com.example.forexservice.dto.internal.ConversionDto
import com.example.forexservice.dto.internal.FeeDto
import com.example.forexservice.dto.internal.FeePatchDto
import com.example.forexservice.dto.internal.RateDto
import com.example.forexservice.service.ConversionService
import com.example.forexservice.service.ECBRateService
import com.example.forexservice.service.FeeService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/v1/rates")
class RatesController(
    private val ecbRateService: ECBRateService
) {

    @GetMapping
    fun list(): List<RateDto> =
        ecbRateService.getECBRates()

    @PutMapping("refresh")
    fun refresh(): Unit =
        ecbRateService.refreshCurrencyRates()

}

@RestController
@RequestMapping("/v1/fees")
class FeesController(
    private val feeService: FeeService
) {

    @GetMapping
    fun list(@RequestParam fromCurrency: String, @RequestParam toCurrency: String): List<FeeDto> =
        feeService.list(fromCurrency, toCurrency)

    @GetMapping("{id}")
    fun get(@PathVariable id: UUID): FeeDto =
        feeService.get(id)

    @PostMapping
    fun add(@RequestBody feeDto: FeeDto): Unit =
        feeService.add(feeDto)

    @PutMapping("{id}")
    fun update(@PathVariable id: UUID, @RequestBody feePatchDto: FeePatchDto): Unit =
        feeService.update(id, feePatchDto)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: UUID): Unit =
        feeService.delete(id)

}

@RestController
@RequestMapping("/v1/conversion")
class ConversionController(
    private val conversionService: ConversionService
) {

    @GetMapping
    fun previewConversion(
        @RequestParam fromCurrency: String,
        @RequestParam toCurrency: String,
        @RequestParam amount: BigDecimal
    ): ConversionDto =
        conversionService.previewConversion(fromCurrency, toCurrency, amount)

}