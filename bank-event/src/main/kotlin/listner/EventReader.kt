package listner

import event.AccountCreatedEvent
import event.TransactionCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import repository.AccountReadViewRepository
import repository.AccountRepository
import repository.TransactionReadViewRepository
import repository.TransactionRepository

@Component
class EventReader(
    private val accountReadRepository: AccountReadViewRepository,
    private val transactionReadViewRepository: TransactionReadViewRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    private val logger = LoggerFactory.getLogger(EventReader::class.java)

    @EventListener
    @Async("taskExecutor")
    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000))
    fun handleAccountCreated(event : AccountCreatedEvent) {

    }

    @EventListener
    @Async("taskExecutor")
    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000))
    fun handleTransactionCreated(event : TransactionCreatedEvent) {

    }
}
