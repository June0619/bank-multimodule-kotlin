package entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "account")
class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0,

    @Column(nullable = false, unique = true)
    val accountNumber: String,

    @Column(nullable = false)
    var balance: BigDecimal,

    @Column(nullable = false)
    val accountHolderName: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
