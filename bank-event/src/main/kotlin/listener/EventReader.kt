package me.jwjung.bank.event.listener

import entity.AccountReadView
import entity.TransactionReadView
import event.AccountCreatedEvent
import event.TransactionCreatedEvent
import me.jwjung.bank.core.common.TxAdvice
import me.jwjung.bank.domain.repository.AccountReadViewRepository
import me.jwjung.bank.domain.repository.AccountRepository
import me.jwjung.bank.domain.repository.TransactionReadViewRepository
import me.jwjung.bank.domain.repository.TransactionRepository
import me.jwjung.bank.monitoring.metrics.BankMetrics
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

@Component
class EventReader(
    private val accountReadRepository: AccountReadViewRepository,
    private val transactionReadViewRepository: TransactionReadViewRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val metrics: BankMetrics,
    private val txAdvice: TxAdvice,
    private val accountReadViewRepository: AccountReadViewRepository
) {
    private val logger = LoggerFactory.getLogger(EventReader::class.java)

    @EventListener
    @Async("taskExecutor")
    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000))
    fun handleAccountCreated(event : AccountCreatedEvent) {
        val startTime = Instant.now()
        val eventType = "AccountCreatedEvent"

        logger.info("event received $eventType")

        try {
            txAdvice.runNew {
                val account = accountRepository.findById(event.accountId).orElseThrow {
                    IllegalStateException("Account with id ${event.accountId} not found")
                }

                val accountReadView = AccountReadView(
                    id = account.id,
                    accountNumber = account.accountNumber,
                    accountHolderName = account.accountHolderName,
                    balance = account.balance,
                    createdAt = account.createdAt,
                    lastUpdatedAt = LocalDateTime.now(),
                    transactionCount = 0,
                    totalDeposits = BigDecimal.ZERO,
                    totalWithdrawals = BigDecimal.ZERO
                )

                accountReadRepository.save(accountReadView)
                logger.info("Account ${account.id} created")
            }

            val duration = Duration.between(startTime, Instant.now())
            metrics.recordEventProcessingTime(duration, eventType)
            metrics.incrementEventProcessed(eventType)
        } catch (e: Exception) {
            logger.error("Error occurred while proccessing $eventType", e)
            metrics.incrementEventFailed(eventType)
        }
    }

    @EventListener
    @Async("taskExecutor")
    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000))
    fun handleTransactionCreated(event : TransactionCreatedEvent) {
        val startTime = Instant.now()
        val eventType = "TransactionCreatedEvent"

        logger.info("event received $eventType")

        try {
            txAdvice.runNew {
                val transaction = transactionRepository.findById(event.transactionId).orElseThrow {
                    IllegalStateException("Transaction with id ${event.transactionId} not found")
                }

                val account = accountRepository.findById(event.accountId).orElseThrow {
                    IllegalStateException("Account with id ${event.accountId} not found")
                }

                val transactionReadView = TransactionReadView(
                    id = transaction.id,
                    accountId = event.accountId,
                    accountNumber = account.accountNumber,
                    accountHolderName = account.accountHolderName,
                    type = transaction.type,
                    amount = transaction.amount,
                    description = transaction.description,
                    createdAt = transaction.createdAt,
                    balanceAfter = account.balance
                )

                transactionReadViewRepository.save(transactionReadView)
                logger.info("transaction read view updated ${transaction.id}")

                val accountReadView = accountReadViewRepository.findById(account.id).orElseThrow {
                    IllegalStateException("Account with id ${account.id} not found")
                }

                val updatedAccountReadView = accountReadView.copy(
                    balance = account.balance,
                    lastUpdatedAt = LocalDateTime.now(),
                    transactionCount = accountReadView.transactionCount + 1,
                    totalDeposits = when {
                        transaction.type.name.contains("DEPOSIT") ->
                            accountReadView.totalDeposits + transaction.amount
                        transaction.type.name.contains("TRANSFER") ->
                            accountReadView.totalDeposits + transaction.amount
                        else -> accountReadView.totalDeposits
                    },
                    totalWithdrawals = when {
                        transaction.type.name.contains("WITHDRAWAL") ->
                            accountReadView.totalWithdrawals + transaction.amount
                        transaction.type.name.contains("TRANSFER")->
                            accountReadView.totalDeposits + transaction.amount
                        else -> accountReadView.totalWithdrawals
                    }
                )

                accountReadViewRepository.save(updatedAccountReadView)
                logger.info("Account ${account.id} updated")
            }

            val duration = Duration.between(startTime, Instant.now())
            metrics.recordEventProcessingTime(duration, eventType)
            metrics.incrementEventProcessed(eventType)
        } catch (e : Exception) {
            logger.error("Error occurred while processing $eventType", e)
            metrics.incrementEventFailed(eventType)
            throw e
        }
    }
}
