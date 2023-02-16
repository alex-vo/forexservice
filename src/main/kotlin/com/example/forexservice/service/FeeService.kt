package com.example.forexservice.service

import com.example.forexservice.dto.internal.FeeDto
import com.example.forexservice.dto.internal.FeePatchDto
import com.example.forexservice.exception.EntityNotFoundException
import com.example.forexservice.mapping.toDto
import com.example.forexservice.mapping.toEntity
import com.example.forexservice.mapping.update
import com.example.forexservice.repository.FeeRepository
import com.example.forexservice.repository.getFeeById
import com.example.forexservice.service.validation.FeeValidator
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class FeeService(
    private val feeRepository: FeeRepository,
    private val feeValidator: FeeValidator,
) {

    fun list(fromCurrency: String, toCurrency: String): List<FeeDto> =
        feeRepository.findFeesByCurrencies(fromCurrency, toCurrency)
            .map { it.toDto() }

    fun get(id: UUID): FeeDto =
        feeRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("Fee $id not found") }

    fun add(feeDto: FeeDto) {
        feeValidator.validateFee(feeDto)
        feeRepository.save(feeDto.toEntity())
    }

    @Transactional
    fun update(id: UUID, feePatchDto: FeePatchDto) {
        val fee = feeRepository.getFeeById(id)
            .update(feePatchDto)
        feeRepository.save(fee)
    }

    @Transactional
    fun delete(id: UUID) {
        val fee = feeRepository.getFeeById(id)
        feeRepository.delete(fee)
    }

}