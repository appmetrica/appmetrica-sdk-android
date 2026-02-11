package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.crash.utils.CrashedThreadConverter
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AppMetricaUncaughtExceptionHandlerTest : CommonTest() {

    private val throwable: Throwable = mock()

    private val processName = ":SomeProcess"

    private val thread: Thread = mock()
    private val threadState: ThreadState = mock()
    private val threadStateDump: List<ThreadState> = listOf(mock(), mock())

    @get:Rule
    val crashedThreadConverterMockedConstructionRule = constructionRule<CrashedThreadConverter> {
        on { apply(thread) } doReturn threadState
    }

    @get:Rule
    val threadsStateDumperMockedConstructionRule = constructionRule<ThreadsStateDumper> {
        on { getThreadsDumpForCrash(thread) } doReturn threadStateDump
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val allThreadsArgumentCaptor = argumentCaptor<AllThreads>()

    private val crashProcessor: ICrashProcessor = mock()

    private val handler: AppMetricaUncaughtExceptionHandler by setUp {
        AppMetricaUncaughtExceptionHandler(crashProcessor)
    }

    @Before
    fun setUp() {
        whenever(ClientServiceLocator.getInstance().processNameProvider.getProcessName()).thenReturn(processName)
        AppMetricaUncaughtExceptionHandler.reset()
    }

    @After
    fun tearDown() {
        AppMetricaUncaughtExceptionHandler.reset()
    }

    @Test
    fun `uncaughtException mark process as dying`() {
        handler.uncaughtException(thread, throwable)
        assertThat(AppMetricaUncaughtExceptionHandler.isProcessDying()).isTrue()
    }

    @Test
    fun uncaughtException() {
        handler.uncaughtException(thread, throwable)
        verify(crashProcessor).processCrash(eq(throwable), allThreadsArgumentCaptor.capture())
        ObjectPropertyAssertions(allThreadsArgumentCaptor.firstValue)
            .checkField("processName", processName)
            .checkField("threads", threadStateDump)
            .checkField("affectedThread", threadState)
            .checkAll()
    }
}
