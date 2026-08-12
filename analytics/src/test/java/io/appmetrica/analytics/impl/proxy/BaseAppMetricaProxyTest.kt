package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BaseAppMetricaProxyTest : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @Test
    fun dispatchUsesCachedExecutorWithoutWrappingTask() {
        val initialExecutor: IHandlerExecutor = mock()
        val replacementExecutor: IHandlerExecutor = mock()
        val executorProvider = ClientServiceLocator.getInstance().clientExecutorProvider
        whenever(executorProvider.defaultExecutor).thenReturn(initialExecutor)
        val proxy = createProxy()
        whenever(executorProvider.defaultExecutor).thenReturn(replacementExecutor)
        val task: Runnable = mock()

        proxy.dispatchForTest(task)

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(initialExecutor).execute(runnableCaptor.capture())
        verify(replacementExecutor, never()).execute(any())
        assertThat(runnableCaptor.firstValue).isSameAs(task)

        runnableCaptor.firstValue.run()
        verify(task).run()
    }

    @Test
    fun dispatchSafelyContainsTaskFailure() {
        val executor: IHandlerExecutor = mock()
        val executorProvider = ClientServiceLocator.getInstance().clientExecutorProvider
        whenever(executorProvider.defaultExecutor).thenReturn(executor)
        val proxy = createProxy()
        val task = Runnable { error("expected") }

        proxy.dispatchSafelyForTest(task)

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(runnableCaptor.capture())
        assertThat(runnableCaptor.firstValue).isInstanceOf(SafeRunnable::class.java)
        assertThatCode { runnableCaptor.firstValue.run() }.doesNotThrowAnyException()
    }

    private fun createProxy() = TestBaseAppMetricaProxy(
        mock(),
        mock(),
        mock(),
        mock()
    )

    private class TestBaseAppMetricaProxy(
        provider: AppMetricaFacadeProvider,
        webViewJsInterfaceHandler: WebViewJsInterfaceHandler,
        reporterProxyStorage: ReporterProxyStorage,
        defaultOneShotMetricaConfig: DefaultOneShotMetricaConfig
    ) : BaseAppMetricaProxy(
        provider,
        webViewJsInterfaceHandler,
        reporterProxyStorage,
        defaultOneShotMetricaConfig
    ) {
        fun dispatchForTest(task: Runnable) = dispatch(task)

        fun dispatchSafelyForTest(task: Runnable) = dispatchSafely(task)
    }
}
