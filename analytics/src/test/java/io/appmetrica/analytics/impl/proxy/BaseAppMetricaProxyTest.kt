package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class BaseAppMetricaProxyTest : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val reporter: SelfReporterWrapper = mock()

    @get:Rule
    val selfReportFacadeMockedStaticRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() }.thenReturn(reporter)
    }

    @Test
    fun dispatchUsesCachedExecutorAndRunsTaskWithoutReportingError() {
        val initialExecutor: IHandlerExecutor = mock()
        val replacementExecutor: IHandlerExecutor = mock()
        val executorProvider = ClientServiceLocator.getInstance().clientExecutorProvider
        whenever(executorProvider.defaultExecutor).thenReturn(initialExecutor)
        val proxy = createProxy()
        whenever(executorProvider.defaultExecutor).thenReturn(replacementExecutor)
        var taskExecutionCount = 0

        proxy.dispatchSafelyForTest { taskExecutionCount++ }

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(initialExecutor).execute(runnableCaptor.capture())
        verify(replacementExecutor, never()).execute(any())
        assertThat(runnableCaptor.firstValue).isInstanceOf(SafeRunnable::class.java)
        runnableCaptor.firstValue.run()

        assertThat(taskExecutionCount).isEqualTo(1)
        verifyNoInteractions(reporter)
    }

    @Test
    fun dispatchReportsAndContainsTaskFailure() {
        val executor: IHandlerExecutor = mock()
        val executorProvider = ClientServiceLocator.getInstance().clientExecutorProvider
        whenever(executorProvider.defaultExecutor).thenReturn(executor)
        val proxy = createProxy()
        val error = RuntimeException("expected")

        proxy.dispatchSafelyForTest { throw error }

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(runnableCaptor.capture())
        assertThat(runnableCaptor.firstValue).isInstanceOf(SafeRunnable::class.java)
        assertThatCode { runnableCaptor.firstValue.run() }.doesNotThrowAnyException()
        verify(reporter).reportError("Exception during asynchronous AppMetrica proxy task", error)
    }

    @Test
    fun dispatchPreservesTaskFailureWhenReportingFails() {
        val executor: IHandlerExecutor = mock()
        val executorProvider = ClientServiceLocator.getInstance().clientExecutorProvider
        whenever(executorProvider.defaultExecutor).thenReturn(executor)
        val proxy = createProxy()
        val taskError = RuntimeException("task")
        val reportingError = RuntimeException("reporting")
        doThrow(reportingError).whenever(reporter).reportError(
            "Exception during asynchronous AppMetrica proxy task",
            taskError
        )
        var taskExecutionCount = 0

        proxy.dispatchSafelyForTest {
            taskExecutionCount++
            throw taskError
        }

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(runnableCaptor.capture())
        val safeRunnable = runnableCaptor.firstValue as SafeRunnable

        assertThatThrownBy { safeRunnable.runSafety() }.isSameAs(taskError)
        assertThat(taskExecutionCount).isEqualTo(1)
        verify(reporter).reportError("Exception during asynchronous AppMetrica proxy task", taskError)
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
        fun dispatchSafelyForTest(task: Runnable) = dispatchSafely(task)
    }
}
