package io.appmetrica.analytics.impl.crash.jvm.client

import android.os.Looper
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.crash.utils.FullStateConverter
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AnrFromApiReportingTaskTest : CommonTest() {

    private val anrReporter: AnrReporter = mock()
    private val thread: Thread = mock()
    private val stacktrace = arrayOf(mock<StackTraceElement>())
    private val allThreads = mapOf(thread to stacktrace)
    private val allThreadsDump: AllThreads = mock()

    private val mainThread: Thread = mock()
    private val mainLooper: Looper = mock {
        on { thread } doReturn mainThread
    }

    @get:Rule
    val looperMockedStaticRule = staticRule<Looper> {
        on { Looper.getMainLooper() } doReturn mainLooper
    }

    @get:Rule
    val threadsStateDumperMockedConstructionRule = constructionRule<ThreadsStateDumper> {
        on { threadsDumpForAnr } doReturn allThreadsDump
    }

    @get:Rule
    val fullStateConverterConstructionRule = constructionRule<FullStateConverter>()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val anrFromApiReportingTask by setUp { AnrFromApiReportingTask(anrReporter, allThreads) }

    @Test
    fun execute() {
        anrFromApiReportingTask.execute()
        verify(anrReporter).reportAnr(allThreadsDump)
    }

    @Test
    fun `execute check thread state dumper`() {
        anrFromApiReportingTask.execute()

        assertThat(threadsStateDumperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        val arguments = threadsStateDumperMockedConstructionRule.argumentInterceptor.flatArguments()
        val threadProvider = arguments[0] as ThreadsStateDumper.ThreadProvider
        assertThat(threadProvider.mainThread).isEqualTo(mainThread)
        assertThat(threadProvider.allThreadsStacktraces).isEqualTo(allThreads)
        assertThat(arguments[1]).isEqualTo(fullStateConverterConstructionRule.constructionMock.constructed().first())
        assertThat(arguments[2]).isEqualTo(ClientServiceLocator.getInstance().processDetector)

        assertThat(fullStateConverterConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(fullStateConverterConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
