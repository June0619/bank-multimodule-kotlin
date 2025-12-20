package publisher

import com.sun.org.slf4j.internal.LoggerFactory
import event.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

interface EventPublisher {
    fun publish(event: DomainEvent)
    fun publishAsync(event: DomainEvent)
    fun publishAll(events: List<DomainEvent>)
    fun publishAllAsync(events: List<DomainEvent>)
}

@Component
class EventPublisherImpl(
    private val eventPublisher: ApplicationEventPublisher
    // TODO metrics
) : EventPublisher {
    private val logger = LoggerFactory.getLogger(EventPublisherImpl::class.java)

    override fun publish(event: DomainEvent) {
        eventPublisher.publishEvent(event)
    }

    override fun publishAsync(event: DomainEvent) {
        eventPublisher.publishEvent(event)
    }

    @Async("taskExecutor")
    override fun publishAll(events: List<DomainEvent>) {
        events.forEach { event ->
            eventPublisher.publishEvent(event)
        }
    }

    @Async("taskExecutor")
    override fun publishAllAsync(events: List<DomainEvent>) {
        events.forEach { event ->
            eventPublisher.publishEvent(event)
        }
    }
}