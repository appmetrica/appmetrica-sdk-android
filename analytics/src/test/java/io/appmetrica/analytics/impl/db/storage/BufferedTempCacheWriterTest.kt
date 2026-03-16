package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BufferedTempCacheWriterTest : CommonTest() {

    private val scope = "test-scope"
    private val timestamp = 123456L
    private val data = ByteArray(5) { it.toByte() }

    private val secondScope = "second-scope"
    private val secondTimestamp = 234567L
    private val secondData = ByteArray(7) { it.toByte() }

    private val batchWriterCaptor = argumentCaptor<List<TempCachePutTask>>()
    private val batchWriter: (List<TempCachePutTask>) -> Unit = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()
    private val serviceExecutorProvider by setUp {
        GlobalServiceLocator.getInstance().serviceExecutorProvider
    }

    private val executor: IHandlerExecutor = mock {
        on { execute(any()) } doAnswer { invocation ->
            (invocation.arguments[0] as Runnable).run()
            null
        }
        on { executeDelayed(any(), any()) } doAnswer { invocation ->
            (invocation.arguments[0] as Runnable).run()
            null
        }
    }

    private lateinit var writer: BufferedTempCacheWriter

    @Before
    fun setUp() {
        whenever(serviceExecutorProvider.getPersistenceExecutor()).thenReturn(executor)
        writer = BufferedTempCacheWriter(1000, batchWriter)
    }

    @Test
    fun `put single task writes batch`() {
        writer.put(scope, timestamp, data)

        verify(batchWriter).invoke(batchWriterCaptor.capture())

        val capturedTasks = batchWriterCaptor.firstValue
        assertThat(capturedTasks).hasSize(1)
        assertThat(capturedTasks[0]).isEqualTo(TempCachePutTask(scope, timestamp, data))
    }

    @Test
    fun `put multiple tasks accumulates and writes batch`() {
        val delayedRunnableCaptor = argumentCaptor<Runnable>()
        val accumulatingExecutor: IHandlerExecutor = mock {
            on { execute(any()) } doAnswer { invocation ->
                (invocation.arguments[0] as Runnable).run()
                null
            }
            on { executeDelayed(any(), any()) } doAnswer { invocation ->
                null
            }
        }

        whenever(serviceExecutorProvider.getPersistenceExecutor()).thenReturn(accumulatingExecutor)
        val testWriter = BufferedTempCacheWriter(1000, batchWriter)

        testWriter.put(scope, timestamp, data)
        testWriter.put(secondScope, secondTimestamp, secondData)

        verify(accumulatingExecutor).executeDelayed(delayedRunnableCaptor.capture(), eq(1000L))
        delayedRunnableCaptor.firstValue.run()

        verify(batchWriter).invoke(batchWriterCaptor.capture())

        val capturedTasks = batchWriterCaptor.firstValue
        assertThat(capturedTasks).hasSize(2)
        assertThat(capturedTasks).containsExactly(
            TempCachePutTask(scope, timestamp, data),
            TempCachePutTask(secondScope, secondTimestamp, secondData)
        )
    }

    @Test
    fun `flush writes pending tasks immediately`() {
        val delayedExecutor: IHandlerExecutor = mock {
            on { execute(any()) } doAnswer { invocation ->
                (invocation.arguments[0] as Runnable).run()
                null
            }
            on { executeDelayed(any(), any()) } doAnswer {
                null
            }
        }

        whenever(serviceExecutorProvider.getPersistenceExecutor()).thenReturn(delayedExecutor)
        val testWriter = BufferedTempCacheWriter(1000, batchWriter)

        testWriter.put(scope, timestamp, data)
        verify(batchWriter, never()).invoke(any())

        testWriter.flush()
        verify(batchWriter).invoke(batchWriterCaptor.capture())

        val capturedTasks = batchWriterCaptor.firstValue
        assertThat(capturedTasks).hasSize(1)
        assertThat(capturedTasks[0]).isEqualTo(TempCachePutTask(scope, timestamp, data))
    }

    @Test
    fun `flushAsync writes pending tasks asynchronously`() {
        val delayedExecutor: IHandlerExecutor = mock {
            on { execute(any()) } doAnswer { invocation ->
                (invocation.arguments[0] as Runnable).run()
                null
            }
            on { executeDelayed(any(), any()) } doAnswer {
                null
            }
        }

        whenever(serviceExecutorProvider.getPersistenceExecutor()).thenReturn(delayedExecutor)
        val testWriter = BufferedTempCacheWriter(1000, batchWriter)

        testWriter.put(scope, timestamp, data)
        verify(batchWriter, never()).invoke(any())

        testWriter.flushAsync()

        verify(delayedExecutor, times(1)).execute(any())
        verify(batchWriter).invoke(batchWriterCaptor.capture())

        val capturedTasks = batchWriterCaptor.firstValue
        assertThat(capturedTasks).hasSize(1)
    }

    @Test
    fun `exception in batch writer is caught and logged`() {
        val failingBatchWriter: (List<TempCachePutTask>) -> Unit = mock {
            on { invoke(any()) } doAnswer { throw RuntimeException("Test exception") }
        }

        val writer = BufferedTempCacheWriter(1000, failingBatchWriter)

        writer.put(scope, timestamp, data)

        verify(failingBatchWriter).invoke(any())
    }

    @Test
    fun `custom delay is respected`() {
        val customDelay = 5000L
        val writer = BufferedTempCacheWriter(customDelay, batchWriter)

        writer.put(scope, timestamp, data)

        verify(executor).executeDelayed(any(), eq(customDelay))
    }

    @Test
    fun `lambda receives correct task data`() {
        val capturedTasks = mutableListOf<List<TempCachePutTask>>()
        val capturingBatchWriter: (List<TempCachePutTask>) -> Unit = { tasks ->
            capturedTasks.add(tasks)
        }

        val writer = BufferedTempCacheWriter(1000, capturingBatchWriter)

        writer.put(scope, timestamp, data)

        assertThat(capturedTasks).hasSize(1)
        val tasks = capturedTasks[0]
        assertThat(tasks).hasSize(1)

        val task = tasks[0]
        assertThat(task.scope).isEqualTo(scope)
        assertThat(task.timestamp).isEqualTo(timestamp)
        assertThat(task.data).isEqualTo(data)
    }

    @Test
    fun `lambda receives tasks in correct order`() {
        val capturedTasks = mutableListOf<List<TempCachePutTask>>()
        val capturingBatchWriter: (List<TempCachePutTask>) -> Unit = { tasks ->
            capturedTasks.add(tasks)
        }

        val delayedRunnableCaptor = argumentCaptor<Runnable>()
        val accumulatingExecutor: IHandlerExecutor = mock {
            on { execute(any()) } doAnswer { invocation ->
                (invocation.arguments[0] as Runnable).run()
                null
            }
            on { executeDelayed(any(), any()) } doAnswer { invocation ->
                null
            }
        }

        whenever(serviceExecutorProvider.getPersistenceExecutor()).thenReturn(accumulatingExecutor)
        val testWriter = BufferedTempCacheWriter(1000, capturingBatchWriter)

        val thirdScope = "third-scope"
        val thirdTimestamp = 345678L
        val thirdData = ByteArray(9) { it.toByte() }

        testWriter.put(scope, timestamp, data)
        testWriter.put(secondScope, secondTimestamp, secondData)
        testWriter.put(thirdScope, thirdTimestamp, thirdData)

        verify(accumulatingExecutor).executeDelayed(delayedRunnableCaptor.capture(), eq(1000L))

        delayedRunnableCaptor.firstValue.run()

        assertThat(capturedTasks).hasSize(1)
        val tasks = capturedTasks[0]
        assertThat(tasks).hasSize(3)

        assertThat(tasks[0]).isEqualTo(TempCachePutTask(scope, timestamp, data))
        assertThat(tasks[1]).isEqualTo(TempCachePutTask(secondScope, secondTimestamp, secondData))
        assertThat(tasks[2]).isEqualTo(TempCachePutTask(thirdScope, thirdTimestamp, thirdData))
    }

    @Test
    fun `lambda receives empty list if no tasks were added`() {
        val capturedTasks = mutableListOf<List<TempCachePutTask>>()
        val capturingBatchWriter: (List<TempCachePutTask>) -> Unit = { tasks ->
            capturedTasks.add(tasks)
        }

        val writer = BufferedTempCacheWriter(1000, capturingBatchWriter)

        writer.flush()

        assertThat(capturedTasks).isEmpty()
    }

    @Test
    fun `lambda called once for multiple sequential puts`() {
        var callCount = 0
        val countingBatchWriter: (List<TempCachePutTask>) -> Unit = { tasks ->
            callCount++
        }

        val delayedRunnableCaptor = argumentCaptor<Runnable>()
        val accumulatingExecutor: IHandlerExecutor = mock {
            on { execute(any()) } doAnswer { invocation ->
                (invocation.arguments[0] as Runnable).run()
                null
            }
            on { executeDelayed(any(), any()) } doAnswer { invocation ->
                null
            }
        }

        whenever(serviceExecutorProvider.getPersistenceExecutor()).thenReturn(accumulatingExecutor)
        val testWriter = BufferedTempCacheWriter(1000, countingBatchWriter)

        testWriter.put(scope, timestamp, data)
        testWriter.put(secondScope, secondTimestamp, secondData)

        verify(accumulatingExecutor).executeDelayed(delayedRunnableCaptor.capture(), eq(1000L))

        delayedRunnableCaptor.firstValue.run()

        assertThat(callCount).isEqualTo(1)
    }

    @Test
    fun `lambda can be called multiple times for separate batches`() {
        val capturedBatches = mutableListOf<List<TempCachePutTask>>()
        val capturingBatchWriter: (List<TempCachePutTask>) -> Unit = { tasks ->
            capturedBatches.add(tasks)
        }

        val writer = BufferedTempCacheWriter(1000, capturingBatchWriter)

        writer.put(scope, timestamp, data)
        writer.flush()

        writer.put(secondScope, secondTimestamp, secondData)
        writer.flush()

        assertThat(capturedBatches).hasSize(2)
        assertThat(capturedBatches[0]).hasSize(1)
        assertThat(capturedBatches[1]).hasSize(1)
    }
}
