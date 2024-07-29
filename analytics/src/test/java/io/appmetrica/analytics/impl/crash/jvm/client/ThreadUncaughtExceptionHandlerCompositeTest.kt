package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class ThreadUncaughtExceptionHandlerCompositeTest : CommonTest() {

    private val thread: Thread = mock()
    private val throwable: Throwable = mock()
    private val firstHandler: Thread.UncaughtExceptionHandler = mock()
    private val secondHandler: Thread.UncaughtExceptionHandler = mock()

    private val composite: ThreadUncaughtExceptionHandlerComposite by setUp {
        ThreadUncaughtExceptionHandlerComposite()
    }

    @Test
    fun registerAndUnregister() {
        composite.register(firstHandler)
        composite.register(null)
        composite.register(secondHandler)
        composite.unregister(secondHandler)
        composite.uncaughtException(thread, throwable)
        verify(firstHandler).uncaughtException(thread, throwable)
        verifyNoInteractions(secondHandler)
    }
}
