package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

internal class CrashProcessorCompositeTest : CommonTest() {

    private val firstCrashProcessor: ICrashProcessor = mock()
    private val secondCrashProcessor: CrashProcessor = mock()

    private val throwable: Throwable = mock()
    private val allThreads: AllThreads = mock()

    private val composite: CrashProcessorComposite by setUp {
        CrashProcessorComposite()
    }

    @Test
    fun `register as varargs`() {
        composite.register(firstCrashProcessor, secondCrashProcessor)
        composite.processCrash(throwable, allThreads)
        verify(firstCrashProcessor).processCrash(throwable, allThreads)
        verify(secondCrashProcessor).processCrash(throwable, allThreads)
    }

    @Test
    fun `register as list`() {
        composite.register(listOf(firstCrashProcessor, secondCrashProcessor))
        composite.processCrash(throwable, allThreads)
        verify(firstCrashProcessor).processCrash(throwable, allThreads)
        verify(secondCrashProcessor).processCrash(throwable, allThreads)
    }

    @Test
    fun clearAllCrashProcessors() {
        composite.register(firstCrashProcessor, secondCrashProcessor)
        composite.clearAllCrashProcessors()
        composite.processCrash(throwable, allThreads)
        verifyNoInteractions(firstCrashProcessor, secondCrashProcessor)
        composite.register(firstCrashProcessor, secondCrashProcessor)
        composite.processCrash(throwable, allThreads)
        verify(firstCrashProcessor).processCrash(throwable, allThreads)
        verify(secondCrashProcessor).processCrash(throwable, allThreads)
    }
}
