package com.example.forexservice.repository

import com.example.forexservice.entity.FeeEntity
import com.example.forexservice.exception.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.UUID

class FeeRepositoryTest : BaseIntegrationTest() {

    @Autowired
    lateinit var feeRepository: FeeRepository

    @Test
    fun `should get fee by id`() {
        val feeId = UUID.fromString("ded41d82-9339-4e01-9b9e-c005fb117ebc")
        val result = feeRepository.getFeeById(feeId)

        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(FeeEntity("EUR", "USD", BigDecimal.valueOf(0.02), 1676548345085, 1676548345085))
        assertEquals(result.id, feeId)
    }

    @Test
    fun `should throw EntityNotFoundException - fee not found`() {
        assertThrows<EntityNotFoundException> {
            feeRepository.getFeeById(UUID.fromString("3ee46768-9769-4409-96e0-f6fefdfdc0a2"))
        }.let {
            assertEquals(it.message, "Fee 3ee46768-9769-4409-96e0-f6fefdfdc0a2 not found")
        }
    }

    @Test
    fun `should list fees by currencies`() {
        val result = feeRepository.findFeesByCurrencies("EUR", "USD")

        assertThat(result)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
            .containsExactly(
                FeeEntity("EUR", "USD", BigDecimal.valueOf(0.01), 1676548345084, 1676548345084),
                FeeEntity("EUR", "USD", BigDecimal.valueOf(0.02), 1676548345085, 1676548345085),
            )
        assertEquals(result[0].id, UUID.fromString("dc114750-496b-4072-967d-d5108679778e"))
        assertEquals(result[1].id, UUID.fromString("ded41d82-9339-4e01-9b9e-c005fb117ebc"))
    }

}
