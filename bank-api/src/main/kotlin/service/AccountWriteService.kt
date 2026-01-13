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

    private fun transferInternal(
        fromAccount: String,
        toAccount: String,
        amount: BigDecimal
    ): ResponseEntity<ApiResponse<String>> { // 리턴 타입의 '?' 제거 (성공/실패는 ApiResponse로 처리)

        return txAdvice.run {
            // 1. 조회 (Entity는 영속 상태가 됨)
            val fromAcct = accountRepository.findByAccountNumber(fromAccount)
                ?: return@run ApiResponse.error("From Account Not Found")

            val toAcct = accountRepository.findByAccountNumber(toAccount)
                ?: return@run ApiResponse.error("To Account Not Found")

            // 2. 밸리데이션
            if (fromAcct.balance < amount) {
                return@run ApiResponse.error("From Account Balance Limit")
            }

            // 3. 비즈니스 로직 (Dirty Checking으로 자동 Update)
            fromAcct.balance -= amount // 코틀린 연산자 오버로딩 활용 가능 (BigDecimal도 -, + 가능)
            toAcct.balance += amount

            // 4. 히스토리 저장 (신규 생성이므로 save 필요)
            // apply를 써서 코드 응집도를 높임
            val fromTransaction = Transaction(
                account = fromAcct,
                amount = amount,
                type = TransactionType.TRANSFER,
                description = "Transfer From"
            ).also { transactionRepository.save(it) } // save 즉시 ID 주입됨

            val toTransaction = Transaction(
                account = toAcct,
                amount = amount,
                type = TransactionType.TRANSFER,
                description = "Transfer To"
            ).also { transactionRepository.save(it) }

            // 5. 메트릭 처리
            metrics.incrementTransaction("TRANSFER")
            metrics.incrementTransaction("TRANSFER")

            // 6. 이벤트 발행
            // 팁: 여기서 바로 publishAsync 하지 않고,
            // ApplicationEventPublisher로 던지고 리스너에서 @TransactionalEventListener(phase = AFTER_COMMIT) 처리하는게 베스트.
            // 하지만 비동기(Async) 메서드라면 트랜잭션과 무관하게 돌테니 여기서 호출해도 큰 문제는 없음 (단, 데이터 일관성 주의)
            publishTransactionEvent(fromTransaction, fromAcct.balance)
            publishTransactionEvent(toTransaction, toAcct.balance)

            return@run ApiResponse.success<String>(
                data = "Transfer Completed",
                msg = "Transfer Completed"
            )
        }!! // null이 나올 수 없는 구조이므로 !! 사용
    }

    // 중복 코드 제거용 헬퍼 함수
    private fun publishTransactionEvent(tx: Transaction, balance: BigDecimal) {
        eventPublisher.publishAsync(
            TransactionCreatedEvent(
                transactionId = tx.id, // save() 이후라 ID 존재함
                accountId = tx.account.id,
                type = tx.type,
                description = tx.description,
                amount = tx.amount,
                balanceAfter = balance
            )
        )
    }
}