package me.jwjung.bank.core.common

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class CircuitBreakerConfiguration {

    @Bean
    fun circuitBreakerRegistry(): CircuitBreakerRegistry {
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50f) // 실패율 임계값 == 50%
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Open -> Half Open 상태의 대기 시간
            .permittedNumberOfCallsInHalfOpenState(3) // Half Open 상태에서의 상태 기준 값
            .slidingWindowSize(5) // 최근 5개 항목만 검사
            .minimumNumberOfCalls(3) //
            .build()

        /**
         * 1. 최근 5번 호출 중 3번 이상 실패한다 -> Open
         * 2. Open 에서 30초 대기 후 half Open
         * 3. Half Open 상태에서 3번 호출을 허용하면서 테스트 후 다음 상태 결정
         */

        return CircuitBreakerRegistry.of(config)
    }

}

object CircuitBreakerUtils {

    fun <T> CircuitBreaker.execute(
        operation: () -> T,
        fallback: (Exception) -> T
    ): T {
        return try {
            val supplier = CircuitBreaker.decorateSupplier(this) { operation() }
            supplier.get()
        } catch (e: Exception) {
            fallback(e)
        }
    }
}

