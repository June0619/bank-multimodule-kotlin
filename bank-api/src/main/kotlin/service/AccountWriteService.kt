package me.jwjung.bank.service

import dto.AccountView
import entity.Account
import entity.Transaction
import entity.TransactionType
import event.AccountCreatedEvent
import event.TransactionCreatedEvent
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import me.jwjung.bank.common.ApiResponse
import me.jwjung.bank.core.common.CircuitBreakerUtils.execute
import me.jwjung.bank.core.common.TxAdvice
import me.jwjung.bank.core.lock.DistributedLockService
import me.jwjung.bank.domain.repository.AccountRepository
import me.jwjung.bank.domain.repository.TransactionRepository
import me.jwjung.bank.monitoring.metrics.BankMetrics
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import publisher.EventPublisher
import java.math.BigDecimal

@Service
class AccountWriteService (
    private val txAdvice: TxAdvice,
    private val circuitBreaker: CircuitBreakerRegistry,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val lockService: DistributedLockService,
    private val eventPublisher: EventPublisher,
    private val metrics: BankMetrics
) {
    private val logger = LoggerFactory.getLogger(AccountWriteService::class.java)
    private val breaker = circuitBreaker.circuitBreaker("accountWrite")

    private fun randomAccountNumber(): String {
        return System.currentTimeMillis().toString()
    }

    fun createAccount(name: String, balance: BigDecimal): ResponseEntity<ApiResponse<AccountView>> {
        return breaker.execute(
            operation = {
                val account = txAdvice.run {
                    val accountNumber = randomAccountNumber()
                    val account = Account(
                        accountNumber = accountNumber,
                        balance = balance,
                        accountHolderName = name
                    )
                    accountRepository.save(account)
                }!!

                metrics.incrementAccountCreated()
                metrics.updateAccountCount(accountRepository.count())

                eventPublisher.publishAsync(
                    AccountCreatedEvent(
                        accountId = account.id,
                        accountNumber = account.accountNumber,
                        accountHolderName = account.accountHolderName,
                        initialBalance = account.balance
                    )
                )

                return@execute ApiResponse.success(
                    data = AccountView(
                        id = account.id,
                        accountNumber = account.accountNumber,
                        balance = account.balance,
                        accountHolderName = account.accountHolderName,
                        createdAt = account.createdAt
                    ),
                    msg = "Account Created"
                )
            },
            fallback = {exception ->
                logger.warn("Create Account Failed", exception)
                ApiResponse.error<AccountView>(
                    msg = "Create Account Failed",
                )
            }
        )
    }

    fun transfer(fromAccount: String, toAccount: String, amount: BigDecimal): ResponseEntity<ApiResponse<String>> {
        return breaker.execute(
            operation = {
                lockService.executeWithTransactionLock(fromAccount, toAccount) {
                    transferInternal(fromAccount, toAccount, amount)
                }
            },
            fallback = {exception ->
                logger.warn("Transfer Failed", exception)
                ApiResponse.error<String>(
                    msg = "Transfer Failed",
                )
            }
        )!!
    }

    private fun transferInternal(fromAccount: String, toAccount: String, amount: BigDecimal): ResponseEntity<ApiResponse<String>>? {
        return txAdvice.run {
            val fromAcct = accountRepository.findByAccountNumber(fromAccount)
                ?: return@run ApiResponse.error("From Account Not Found")

            if (fromAcct.balance < amount) {
                return@run ApiResponse.error("From Account Balance Limit")
            }

            val toAcct = accountRepository.findByAccountNumber(toAccount)
                ?: return@run ApiResponse.error("To Account Not Found")

            fromAcct.balance = fromAcct.balance.subtract(amount)
            toAcct.balance = toAcct.balance.add(amount)

            val fromTransaction = Transaction(
                account = fromAcct,
                amount = amount,
                type = TransactionType.TRANSFER,
                description = "Transfer From"
            )

            transactionRepository.save(fromTransaction)

            metrics.incrementTransaction("TRANSFER")

            eventPublisher.publishAsync(
                TransactionCreatedEvent(
                    transactionId = fromTransaction.id,
                    fromAcct.id,
                    type = TransactionType.TRANSFER,
                    description = "Transaction Created",
                    amount = amount,
                    balanceAfter = fromAcct.balance
                )
            )

            val toTransaction = Transaction(
                account = toAcct,
                amount = amount,
                type = TransactionType.TRANSFER,
                description = "Transfer To"
            )

            transactionRepository.save(toTransaction)

            eventPublisher.publishAsync(
                TransactionCreatedEvent(
                    transactionId = toTransaction.id,
                    toAcct.id,
                    type = TransactionType.TRANSFER,
                    description = "Transaction Created",
                    amount = amount,
                    balanceAfter = fromAcct.balance
                )
            )

            return@run ApiResponse.success<String>(
                data = "Transfer Completed",
                msg = "Transfer Completed"
            )
        }
    }
}