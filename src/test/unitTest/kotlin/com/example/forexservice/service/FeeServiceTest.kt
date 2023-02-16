package com.example.forexservice.service

import com.example.forexservice.dto.internal.FeePatchDto
import com.example.forexservice.entity.FeeEntity
import com.example.forexservice.mapping.toDto
import com.example.forexservice.mapping.toEntity
import com.example.forexservice.mapping.update
import com.example.forexservice.prepareFeeDto
import com.example.forexservice.prepareFeeEntity
import com.example.forexservice.repository.FeeRepository
import com.example.forexservice.service.validation.FeeValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FeeServiceTest {

    @Mock
    lateinit var feeRepository: FeeRepository

    @Mock
    lateinit var feeValidator: FeeValidator

    @InjectMocks
    lateinit var feeService: FeeService

    @Test
    fun `should list fees`() {
        val fee1 = prepareFeeEntity("EUR", "USD", 0.01)
        val fee2 = prepareFeeEntity("EUR", "USD", 0.02)
        whenever(feeRepository.findFeesByCurrencies("EUR", "USD"))
            .thenReturn(listOf(fee1, fee2))

        val result = feeService.list("EUR", "USD")

        assertThat(result).containsExactly(fee1.toDto(), fee2.toDto())
    }

    @Test
    fun `should validate and save fee`() {
        val start = System.currentTimeMillis()
        val dto = prepareFeeDto("EUR", "USD", 0.1)

        feeService.add(dto)

        verify(feeValidator).validateFee(dto)
        val feeEntityCaptor = argumentCaptor<FeeEntity>()
        verify(feeRepository).save(feeEntityCaptor.capture())
        assertThat(feeEntityCaptor.firstValue)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "modifiedAt")
            .isEqualTo(dto.toEntity())
        with(feeEntityCaptor.firstValue) {
            assertNotNull(id)
            assertTrue(createdAt > start)
            assertTrue(modifiedAt > start)
        }
    }

    @Test
    fun `should update fee`() {
        val feeId = UUID.randomUUID()
        val feeEntity = prepareFeeEntity("EUR", "USD", 0.01)
        whenever(feeRepository.findFeeById(feeId)).thenReturn(feeEntity)
        val feePatchDto = FeePatchDto(BigDecimal.valueOf(0.02))

        feeService.update(feeId, feePatchDto)

        verify(feeRepository).save(feeEntity.update(feePatchDto))
    }

    @Test
    fun `should delete fee`() {
        val feeId = UUID.randomUUID()
        val feeEntity = prepareFeeEntity("EUR", "USD", 0.01)
        whenever(feeRepository.findFeeById(feeId)).thenReturn(feeEntity)

        feeService.delete(feeId)

        verify(feeRepository).delete(feeEntity)
    }

}