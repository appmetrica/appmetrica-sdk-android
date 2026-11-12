package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.startup.ClientReportsQueueBudgetConfig
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class ClientReportsQueueBudgetTest : CommonTest() {

    @Test
    fun reserveAndReleaseUpdatesCurrentBytes() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 10_000)
        assertThat(budget.tryReserve(1_000)).isTrue
        assertThat(budget.currentBytes).isEqualTo(1_000)

        assertThat(budget.tryReserve(2_000)).isTrue
        assertThat(budget.currentBytes).isEqualTo(3_000)

        budget.release(1_000)
        assertThat(budget.currentBytes).isEqualTo(2_000)
    }

    @Test
    fun tryAcquireReturnsReservationOrNull() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 1_000)
        val first = budget.tryAcquire(800)
        assertThat(first).isNotNull
        assertThat(budget.currentBytes).isEqualTo(800)

        assertThat(budget.tryAcquire(300)).isNull()
        assertThat(budget.currentBytes).isEqualTo(800)

        val second = budget.tryAcquire(200)
        assertThat(second).isNotNull
        assertThat(budget.currentBytes).isEqualTo(1_000)

        first!!.releaseOnce()
        assertThat(budget.currentBytes).isEqualTo(200)
        first.releaseOnce()
        assertThat(budget.currentBytes).isEqualTo(200)

        second!!.releaseOnce()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun reservationWrapReleasesAfterCall() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 1_000)
        val reservation = checkNotNull(budget.tryAcquire(400))
        assertThat(budget.currentBytes).isEqualTo(400)

        reservation.wrap(java.util.concurrent.Callable { Unit }).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun reservationWrapReleasesEvenWhenCallableThrows() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 1_000)
        val reservation = checkNotNull(budget.tryAcquire(400))
        try {
            reservation.wrap(java.util.concurrent.Callable<Unit> { error("boom") }).call()
        } catch (_: IllegalStateException) {
            // expected
        }
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun defaultConstructorUsesStartupDefaultByteLimit() {
        val expected = ClientReportsQueueBudgetConfig.DEFAULT.toByteLimit()
        val budget = ClientReportsQueueBudget()
        assertThat(budget.enabled).isEqualTo(expected != null)
        assertThat(budget.limitBytes).isEqualTo(expected)
    }

    @Test
    fun dropWhenQueueBudgetExceeded() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 5_000)
        assertThat(budget.tryReserve(4_000)).isTrue
        assertThat(budget.tryReserve(2_000)).isFalse
        assertThat(budget.currentBytes).isEqualTo(4_000)
    }

    @Test
    fun dropWhenEventExceedsBudget() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 1_000)
        assertThat(budget.tryReserve(1_001)).isFalse
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun wouldFitReflectsRemainingCapacity() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 1_000)
        assertThat(budget.wouldFit(1_000)).isTrue
        budget.tryReserve(800)
        assertThat(budget.wouldFit(200)).isTrue
        assertThat(budget.wouldFit(201)).isFalse
    }

    @Test
    fun disabledBudgetAlwaysFitsAndAccepts() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = null)
        assertThat(budget.enabled).isFalse
        assertThat(budget.wouldFit(Long.MAX_VALUE)).isTrue
        assertThat(budget.tryReserve(1_000_000)).isTrue
        assertThat(budget.currentBytes).isEqualTo(1_000_000)
    }

    @Test
    fun recordDropUpdatesCounters() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 100)
        budget.recordDrop(ClientReportsQueueDropReason.EVICTED_OLDEST, 40)
        budget.recordDrop(ClientReportsQueueDropReason.QUEUE_BUDGET_EXCEEDED, 60)
        assertThat(budget.droppedCount).isEqualTo(2)
        assertThat(budget.droppedBytes).isEqualTo(100)
    }

    @Test
    fun releaseDoesNotGoNegative() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 1_000)
        budget.tryReserve(100)
        budget.release(500)
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun concurrentReserveRespectsBudget() {
        val budget = ClientReportsQueueBudget(maxBytesLimit = 10_000)
        val threads = 20
        val weight = 1_000L
        val executor = Executors.newFixedThreadPool(threads)
        val barrier = CyclicBarrier(threads)
        val accepted = AtomicInteger(0)
        val done = CountDownLatch(threads)
        repeat(threads) {
            executor.execute {
                try {
                    barrier.await(5, TimeUnit.SECONDS)
                    if (budget.tryReserve(weight)) {
                        accepted.incrementAndGet()
                    }
                } finally {
                    done.countDown()
                }
            }
        }
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue
        executor.shutdownNow()
        assertThat(accepted.get()).isEqualTo(10)
        assertThat(budget.currentBytes).isEqualTo(10_000)
    }
}
