package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.ArrayDeque
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class ClientReportsQueueTest : CommonTest() {

    private val executor: ICommonExecutor = mock()
    private val postedDrains = ArrayDeque<Runnable>()
    private lateinit var budget: ClientReportsQueueBudget
    private lateinit var queue: ClientReportsQueue

    @Before
    fun setUp() {
        postedDrains.clear()
        whenever(executor.post(any())).thenAnswer { invocation ->
            postedDrains.addLast(invocation.getArgument(0))
            null
        }
        budget = ClientReportsQueueBudget(maxBytesLimit = 1_000)
        queue = ClientReportsQueue(budget, executor)
    }

    @Test
    fun enqueueDataPostsDrainOnceUntilEmpty() {
        val first = queue.enqueue(ClientReportsQueueCategory.DATA, 100, noopCallable())
        val second = queue.enqueue(ClientReportsQueueCategory.DATA, 100, noopCallable())
        assertThat(first).isNotNull
        assertThat(second).isNotNull
        assertThat(queue.pendingSizeForTests()).isEqualTo(2)
        verify(executor, times(1)).post(any())

        drainAll()
        assertThat(queue.pendingSizeForTests()).isEqualTo(0)
        assertThat(budget.currentBytes).isEqualTo(0)

        clearInvocations(executor)
        postedDrains.clear()
        queue.enqueue(ClientReportsQueueCategory.DATA, 100, noopCallable())
        verify(executor, times(1)).post(any())
        assertThat(postedDrains).hasSize(1)
    }

    @Test
    fun controlAndFatalAlwaysEnqueueWithoutBudget() {
        budget.tryReserve(1_000)
        assertThat(budget.currentBytes).isEqualTo(1_000)

        val control = queue.enqueue(ClientReportsQueueCategory.CONTROL, 100, noopCallable())
        val fatal = queue.enqueue(ClientReportsQueueCategory.FATAL, 100, noopCallable())
        assertThat(control).isNotNull
        assertThat(fatal).isNotNull
        assertThat(queue.pendingSizeForTests()).isEqualTo(2)
        assertThat(budget.currentBytes).isEqualTo(1_000)
        assertThat(budget.droppedCount).isEqualTo(0)
    }

    @Test
    fun dataExceedingLimitIsDroppedWithoutEnqueue() {
        val dropped = queue.enqueue(ClientReportsQueueCategory.DATA, 1_001, noopCallable())
        assertThat(dropped).isNull()
        assertThat(queue.pendingSizeForTests()).isEqualTo(0)
        assertThat(budget.droppedCount).isEqualTo(1)
        assertThat(budget.droppedBytes).isEqualTo(1_001)
        verify(executor, never()).post(any())
    }

    @Test
    fun dataEvictsOldestPendingDataThenEnqueues() {
        val oldest = queue.enqueue(ClientReportsQueueCategory.DATA, 800, noopCallable())
        assertThat(oldest).isNotNull
        assertThat(budget.currentBytes).isEqualTo(800)

        val newer = queue.enqueue(ClientReportsQueueCategory.DATA, 700, noopCallable())
        assertThat(newer).isNotNull
        assertThat(queue.pendingSizeForTests()).isEqualTo(1)
        assertThat(budget.currentBytes).isEqualTo(700)
        assertThat(budget.droppedCount).isEqualTo(1)
        assertThat(budget.droppedBytes).isEqualTo(800)
    }

    @Test
    fun dataDoesNotEvictControlOrFatal() {
        queue.enqueue(ClientReportsQueueCategory.CONTROL, 0, noopCallable())
        queue.enqueue(ClientReportsQueueCategory.FATAL, 0, noopCallable())
        budget.tryReserve(1_000)

        val dropped = queue.enqueue(ClientReportsQueueCategory.DATA, 100, noopCallable())
        assertThat(dropped).isNull()
        assertThat(queue.pendingSizeForTests()).isEqualTo(2)
        assertThat(budget.droppedCount).isEqualTo(1)
    }

    @Test
    fun disabledBudgetSkipsEvictionAndAcceptsLargeData() {
        budget = ClientReportsQueueBudget(maxBytesLimit = null)
        queue = ClientReportsQueue(budget, executor)

        val item = queue.enqueue(ClientReportsQueueCategory.DATA, 10_000_000, noopCallable())
        assertThat(item).isNotNull
        assertThat(budget.currentBytes).isEqualTo(10_000_000)
        assertThat(budget.droppedCount).isEqualTo(0)
    }

    @Test
    fun tryCancelIfNotStartedReleasesReservation() {
        val item = checkNotNull(queue.enqueue(ClientReportsQueueCategory.DATA, 400, noopCallable()))
        assertThat(budget.currentBytes).isEqualTo(400)

        assertThat(queue.tryCancelIfNotStarted(item)).isTrue
        assertThat(queue.pendingSizeForTests()).isEqualTo(0)
        assertThat(budget.currentBytes).isEqualTo(0)
        assertThat(item.callable).isNull()
        assertThat(item.reservation).isNull()
    }

    @Test
    fun tryCancelIfNotStartedReturnsFalseWhenAlreadyStarted() {
        val item = checkNotNull(queue.enqueue(ClientReportsQueueCategory.DATA, 100, noopCallable()))
        val startedLatch = CountDownLatch(1)
        val allowFinish = CountDownLatch(1)
        item.callable = Callable {
            startedLatch.countDown()
            assertThat(allowFinish.await(5, TimeUnit.SECONDS)).isTrue
            null
        }

        val drainThread = Thread { postedDrains.removeFirst().run() }.also { it.start() }
        assertThat(startedLatch.await(5, TimeUnit.SECONDS)).isTrue
        assertThat(queue.tryCancelIfNotStarted(item)).isFalse

        allowFinish.countDown()
        drainThread.join(5_000)
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun tryCancelIfNotStartedReturnsFalseWhenItemNotInQueue() {
        val orphan = ClientReportsQueuePendingItem(
            category = ClientReportsQueueCategory.DATA,
            weight = 100,
            callable = noopCallable(),
        )
        assertThat(queue.tryCancelIfNotStarted(orphan)).isFalse
    }

    @Test
    fun drainCountsDownCompletionLatchAndContinuesAfterThrow() {
        val latch = CountDownLatch(1)
        val order = mutableListOf<String>()
        queue.enqueue(
            category = ClientReportsQueueCategory.FATAL,
            weight = 0,
            callable = Callable {
                order.add("fatal-throw")
                throw RuntimeException("boom")
            },
            completionLatch = latch,
        )
        queue.enqueue(
            category = ClientReportsQueueCategory.CONTROL,
            weight = 0,
            callable = Callable {
                order.add("control")
                null
            },
        )

        drainAll()
        assertThat(latch.await(0, TimeUnit.MILLISECONDS)).isTrue
        assertThat(order).containsExactly("fatal-throw", "control")
        assertThat(queue.pendingSizeForTests()).isEqualTo(0)
    }

    @Test
    fun drainWithNullCallableStillCompletes() {
        val item = checkNotNull(queue.enqueue(ClientReportsQueueCategory.DATA, 100, noopCallable()))
        item.callable = null
        drainAll()
        assertThat(queue.pendingSizeForTests()).isEqualTo(0)
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    private fun drainAll() {
        while (postedDrains.isNotEmpty()) {
            postedDrains.removeFirst().run()
        }
    }

    private fun noopCallable(): Callable<*> = Callable { null }
}
