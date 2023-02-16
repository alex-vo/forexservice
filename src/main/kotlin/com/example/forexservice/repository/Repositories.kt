package com.example.forexservice.repository

import com.example.forexservice.entity.FeeEntity
import com.example.forexservice.exception.EntityNotFoundException
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface FeeRepository : JpaRepository<FeeEntity, UUID> {

    @Query(
        """
        from FeeEntity f        
        where f.fromCurrency=?1 and f.toCurrency=?2
    """
    )
    fun findFeesByCurrencies(fromCurrency: String, toCurrency: String): List<FeeEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findFeeById(id: UUID): FeeEntity?

}

fun FeeRepository.getFeeById(id: UUID): FeeEntity {
    return findFeeById(id)
        ?: throw EntityNotFoundException("Fee $id not found")
}