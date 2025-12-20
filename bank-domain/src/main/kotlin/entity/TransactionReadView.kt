package entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transaction_read_views")
data class TransactionReadView(
    @Id
    val id: Long = 0,

    @Column(nullable = false)
    val accountId: Long = 0,

    @Column(nullable = false)
    val accountNumber: String = "",

    @Column(nullable = false)
    val accountHolderName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType = TransactionType.DEPOSIT,

    @Column(nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val description: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, precision = 19, scale = 2)
    val balanceAfter: BigDecimal = BigDecimal.ZERO
)