package com.example.forexservice.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "fee")
class FeeEntity(
    val fromCurrency: String,
    val toCurrency: String,
    var value: BigDecimal,
    val createdAt: Long,
    var modifiedAt: Long,
) {
    @Id
    val id: UUID = UUID.randomUUID()
}
