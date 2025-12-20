package entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "account_read_views")
data class AccountReadView(
    @Id
    val id: Long = 0,

    @Column(nullable = false)
    val accountNumber: String = "",

    @Column(nullable = false)
    val accountHolderName: String = "",

    @Column(nullable = false, precision = 19, scale = 2)
    val balance: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val lastUpdatedAt: LocalDateTime = LocalDateTime.now(),

    // 읽기 최적화를 위한 추가 필드들
    @Column(nullable = false)
    val transactionCount: Int = 0,

    @Column(precision = 19, scale = 2)
    val totalDeposits: BigDecimal = BigDecimal.ZERO,

    @Column(precision = 19, scale = 2)
    val totalWithdrawals: BigDecimal = BigDecimal.ZERO
)

