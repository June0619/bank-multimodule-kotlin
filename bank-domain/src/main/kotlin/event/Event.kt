package event

import entity.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

// 기본 도메인 이벤트 인터페이스
interface DomainEvent {
    val occurredOn: LocalDateTime
    val eventId: String
}

// 계좌 생성 이벤트
data class AccountCreatedEvent(
    val accountId: Long,
    val accountNumber: String,
    val accountHolderName: String,
    val initialBalance: BigDecimal,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
    override val eventId: String = UUID.randomUUID().toString()
) : DomainEvent

// 거래 발생 이벤트
data class TransactionCreatedEvent(
    val transactionId: Long,
    val accountId: Long,
    val type: TransactionType,
    val amount: BigDecimal,
    val description: String,
    val balanceAfter: BigDecimal,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
    override val eventId: String = UUID.randomUUID().toString()
) : DomainEvent