package publisher

import event.DomainEvent
import org.slf4j.LoggerFactory
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
        logger.info("Publishing event: $event")
        try {
            eventPublisher.publishEvent(event)
        } catch (e: Exception) {
            logger.error("Error publishing event!", e)
        }
    }

    override fun publishAsync(event: DomainEvent) {
        logger.info("Publishing event: $event")
        try {
            eventPublisher.publishEvent(event)
        } catch (e: Exception) {
            logger.error("Error publishing event!", e)
        }
    }

    @Async("taskExecutor")
    override fun publishAll(events: List<DomainEvent>) {
        events.forEach { event ->
            logger.info("Publishing event: $event")
            try {
                eventPublisher.publishEvent(event)
            } catch (e: Exception) {
                logger.error("Error publishing event!", e)
            }
        }
    }

    @Async("taskExecutor")
    override fun publishAllAsync(events: List<DomainEvent>) {
        events.forEach { event ->
            logger.info("Publishing event: $event")
            try {
                eventPublisher.publishEvent(event)
            } catch (e: Exception) {
                logger.error("Error publishing event!", e)
            }
        }
    }
}