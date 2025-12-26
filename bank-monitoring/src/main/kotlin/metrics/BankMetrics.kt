package metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicLong
import java.time.Duration

@Component
class BankMetrics (private val meterRegistry: MeterRegistry) {
    private val accountGauge = AtomicLong(0)

    init {
        meterRegistry.gauge("bank.account.total", accountGauge) {
            it.get().toDouble()
        }
    }

    fun incrementAccountCreated() {
        Counter.builder("bank.account.created").description("Number of accounts created").register(meterRegistry).increment()
    }

    fun updateAccountCount(count: Long) {
        accountGauge.set(count)
    }

    fun incrementTransaction(type: String) {
        Counter.builder("bank.transaction.count")
            .description("Number of transactions")
            .tag("type", type)
            .register(meterRegistry)
            .increment()
    }

    fun recordTransactionAmount(amount: BigDecimal, type: String) {
        DistributionSummary.builder("bank.transaction.amount")
            .description("Transaction amounts distribution")
            .tag("type", type)
            .register(meterRegistry)
            .record(amount.toDouble())
    }

    fun incrementEventPublished(eventType: String) {
        Counter.builder("bank.event.published")
            .description("Number of events published")
            .tag("type", eventType)
            .register(meterRegistry)
            .increment()
    }

    fun incrementEventProcessed(eventType: String) {
        Counter.builder("bank.event.processed")
            .description("Number of events processed")
            .tag("type", eventType)
            .tag("status", "success")
            .register(meterRegistry)
            .increment()
    }

    fun incrementEventFailed(eventType: String) {
        Counter.builder("bank.event.failed")
            .description("Number of events failed")
            .tag("type", eventType)
            .tag("status", "failed")
            .register(meterRegistry)
            .increment()
    }

    fun recordEventProcessingTime(duration: Duration, eventType: String) {
        Timer.builder("bank.event.processing.time")
            .description("Event processing time")
            .tag("type", eventType)
            .register(meterRegistry)
            .record(duration)
    }

    fun incrementLockAcquisitionFailure(lockKey: String) {
        Counter.builder("bank.lock.acquisition.failed")
            .description("Number of failed lock acquisitions")
            .tag("lock_key", lockKey)
            .register(meterRegistry)
            .increment()
    }

    fun incrementLockAcquisitionSuccess(lockKey: String) {
        Counter.builder("bank.lock.acquisition.success")
            .description("Number of successful lock acquisitions")
            .tag("lock_key", lockKey)
            .register(meterRegistry)
            .increment()
    }

    fun recordApiResponseTime(duration: Duration, endpoint: String, method: String) {
        Timer.builder("bank.api.response.time")
            .description("API response time")
            .tag("endpoint", endpoint)
            .tag("method", method)
            .register(meterRegistry)
            .record(duration)
    }
}