package me.jwjung.bank.domain.repository

import entity.TransactionReadView
import entity.TransactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface TransactionReadViewRepository : JpaRepository<TransactionReadView, Long> {

    fun findByAccountIdOrderByCreatedAtDesc(accountId: Long): List<TransactionReadView>

    fun findByAccountNumberOrderByCreatedAtDesc(accountNumber: String): List<TransactionReadView>

    fun findByAccountIdAndTypeOrderByCreatedAtDesc(accountId: Long, type: TransactionType): List<TransactionReadView>

    @Query("SELECT t FROM TransactionReadView t WHERE t.accountId = :accountId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    fun findByAccountIdAndDateRange(
        @Param("accountId") accountId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<TransactionReadView>

    @Query("SELECT SUM(t.amount) FROM TransactionReadView t WHERE t.accountId = :accountId AND t.type = :type")
    fun sumAmountByAccountIdAndType(
        @Param("accountId") accountId: Long,
        @Param("type") type: TransactionType
    ): BigDecimal?
}