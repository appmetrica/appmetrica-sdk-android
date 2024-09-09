package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.impl.IUnhandledSituationReporter
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LibraryAnrListenerTest : CommonTest() {

    private val stacktrace: List<StackTraceElement> = listOf(mock(), mock())

    private val allThreads = AllThreads(
        ThreadState("some", 3, 12, "Group", 0, stacktrace),
        emptyList(),
        "process name"
    )

    private val mainReporterConsumer: IUnhandledSituationReporter = mock()

    @get:Rule
    val threadsStateDumperMockedConstructionRule = constructionRule<ThreadsStateDumper> {
        on { threadsDumpForAnr } doReturn allThreads
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val listener: LibraryAnrListener by setUp {
        LibraryAnrListener(mainReporterConsumer)
    }

    @Test
    fun onAppNotResponding() {
        listener.onAppNotResponding()
        verify(mainReporterConsumer).reportAnr(allThreads)
    }
}
