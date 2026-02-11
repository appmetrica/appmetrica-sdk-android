package io.appmetrica.analytics.testutils

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.stubbing.Answer
import java.util.concurrent.Callable
import java.util.concurrent.Future

object MockProvider {

    val blockingRunnableAnswer = Answer {
        (it.arguments.first() as Runnable).run()
    }

    val callableAnswer = Answer<Future<*>> {
        val callable = it.arguments.first() as Callable<*>
        val result = callable.call()
        mock<Future<Any?>> {
            on { get() } doReturn result
            on { isDone } doReturn true
        }
    }

    val mockedLooper: Looper = mock()
    val mockedBlockingHandler = mockedBlockingHandler()

    @JvmStatic
    fun mockedBlockingExecutorMock() = mock<IHandlerExecutor> {
        on { handler } doReturn mockedBlockingHandler
        on { looper } doReturn mockedLooper
        on { execute(any()) } doAnswer blockingRunnableAnswer
        on { executeDelayed(any(), any()) } doAnswer blockingRunnableAnswer
        on { submit(any<Callable<*>>()) } doAnswer callableAnswer
    }

    @JvmStatic
    fun mockedBlockingHandler() = mock<Handler> {
        on { post(any()) } doAnswer blockingRunnableAnswer
        on { postDelayed(any(), any()) } doAnswer blockingRunnableAnswer
    }

    @JvmStatic
    fun mockBundle(initialData: Map<String, Any?> = emptyMap()): Bundle {
        val data = mutableMapOf<String, Any?>()
        data.putAll(initialData)

        return mock {
            on { putBundle(any(), any()) } doAnswer { invocation ->
                data[invocation.getArgument(0)] = invocation.getArgument(1)
                null
            }
            on { getBundle(any<String>()) } doAnswer { invocation ->
                data[invocation.getArgument(0)] as? Bundle
            }
            on { keySet() } doAnswer {
                data.keys
            }
        }
    }
}
