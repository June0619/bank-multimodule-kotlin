package me.jwjung.bank.core.lock

import me.jwjung.bank.core.exception.LockAcquisitionException
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@ConfigurationProperties(prefix = "bank.lock")
data class LockProperties(
    val timeout : Long = 5000,
    val leaseTime : Long = 10000,
    val retryInterval : Long = 100 ,
    val maxRetryAttempts : Long = 50
)

@Service
@EnableConfigurationProperties(LockProperties::class)
class DistributedLockService(
    private val redissonClient: RedissonClient,
    private val lockProperties: LockProperties,
) {
    private val logger = LoggerFactory.getLogger(DistributedLockService::class.java)

    private fun <T> executeWithLock(
        lockKey: String,
        action: () -> T
    ): T {
        val lock = redissonClient.getLock(lockKey)
        return try {
            val acquired = lock.tryLock(
                lockProperties.timeout,
                lockProperties.leaseTime,
                TimeUnit.MICROSECONDS
            )

            if (!acquired) {
                logger.error("Acquiring lock for $lockKey")
                throw LockAcquisitionException("Acquiring lock for $lockKey")
            }

            try {
                action()
            } finally {
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                }
            }
        } catch (e: Exception) {
            logger.error("Lock [$lockKey] failed to acquire lock", e)
            throw e
        }
    }

    fun <T> executeWithAccountLock(
        accountNumber: String,
        action: () -> T
    ): T {
        val lockKey = "account:lock:$accountNumber"
        return executeWithAccountLock(lockKey, action)
    }

    fun <T> executeWithTransactionLock(
        from : String,
        to : String,
        action : () -> T
    ) : T {
        val sorted = listOf(from, to).sorted()
        val lockKey = "transaction:lock:${sorted[0]}:${sorted[1]}"
        return executeWithLock(lockKey, action)
    }
}