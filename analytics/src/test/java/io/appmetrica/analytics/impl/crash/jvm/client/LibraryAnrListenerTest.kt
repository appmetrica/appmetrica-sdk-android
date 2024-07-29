package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.impl.IUnhandledSituationReporter
import io.appmetrica.analytics.impl.MainReporterComponents
import io.appmetrica.analytics.impl.UnhandledSituationReporterProvider
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class LibraryAnrListenerTest : CommonTest() {

    private val stacktrace: List<StackTraceElement> = listOf(mock(), mock())

    private val allThreads = AllThreads(
        ThreadState("some", 3, 12, "Group", 0, stacktrace),
        emptyList(),
        "process name"
    )

    private val libraryAnrDetector: LibraryAnrDetector = mock {
        on { isAppmetricaAnr(stacktrace) } doReturn true
        on { isPushAnr(stacktrace) } doReturn true
    }

    private val selfSdkReporter: IUnhandledSituationReporter = mock()

    private val selfSdkReporterProvider: UnhandledSituationReporterProvider = mock {
        on { reporter } doReturn selfSdkReporter
    }

    private val pushSdkReporter: IUnhandledSituationReporter = mock()

    private val pushSdkReporterProvider: UnhandledSituationReporterProvider = mock {
        on { reporter } doReturn pushSdkReporter
    }

    private val mainReporterComponents: MainReporterComponents = mock {
        on { libraryAnrDetector } doReturn libraryAnrDetector
        on { selfSdkCrashReporterProvider } doReturn selfSdkReporterProvider
        on { pushSdkCrashReporterProvider } doReturn pushSdkReporterProvider
    }

    private val mainReporterConsumer: IUnhandledSituationReporter = mock()

    @get:Rule
    val threadsStateDumperMockedConstructionRule = constructionRule<ThreadsStateDumper> {
        on { threadsDumpForAnr } doReturn allThreads
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val listener: LibraryAnrListener by setUp {
        LibraryAnrListener(mainReporterComponents, mainReporterConsumer)
    }

    @Test
    fun `onAppNotResponding for self and push anr`() {
        listener.onAppNotResponding()
        verify(mainReporterConsumer).reportAnr(allThreads)
        verify(selfSdkReporter).reportAnr(allThreads)
        verify(pushSdkReporter).reportAnr(allThreads)
    }

    @Test
    fun `onAppNotResponding for push only anr`() {
        whenever(libraryAnrDetector.isAppmetricaAnr(stacktrace)).thenReturn(false)
        listener.onAppNotResponding()
        verify(mainReporterConsumer).reportAnr(allThreads)
        verify(pushSdkReporter).reportAnr(allThreads)
        verifyNoInteractions(selfSdkReporter)
    }

    @Test
    fun `onAppNotResponding for self only anr`() {
        whenever(libraryAnrDetector.isPushAnr(stacktrace)).thenReturn(false)
        listener.onAppNotResponding()
        verify(mainReporterConsumer).reportAnr(allThreads)
        verify(selfSdkReporter).reportAnr(allThreads)
        verifyNoInteractions(pushSdkReporter)
    }

    @Test
    fun `onAppNotResponding for non library anr`() {
        whenever(libraryAnrDetector.isPushAnr(stacktrace)).thenReturn(false)
        whenever(libraryAnrDetector.isAppmetricaAnr(stacktrace)).thenReturn(false)
        listener.onAppNotResponding()
        verify(mainReporterConsumer).reportAnr(allThreads)
        verifyNoInteractions(selfSdkReporter, pushSdkReporter)
    }

    @Test
    fun `onAppNotResponding without affected thread`() {
        val allThread = AllThreads("")
        whenever(threadsStateDumperMockedConstructionRule.constructionMock.constructed().first().threadsDumpForAnr)
            .thenReturn(allThread)
        listener.onAppNotResponding()
        verify(libraryAnrDetector).isAppmetricaAnr(emptyList())
        verify(libraryAnrDetector).isPushAnr(emptyList())
    }
}
