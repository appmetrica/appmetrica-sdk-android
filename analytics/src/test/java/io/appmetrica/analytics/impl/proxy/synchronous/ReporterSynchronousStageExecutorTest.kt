package io.appmetrica.analytics.impl.proxy.synchronous

import io.appmetrica.analytics.impl.crash.AppMetricaThrowable
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReporterSynchronousStageExecutorTest: CommonTest() {

    private val synchronousStageExecutor = ReporterSynchronousStageExecutor()

    @Test
    fun reportError() {
        val message = "original message"
        val throwable = Throwable(message)
        throwable.fillInStackTrace()
        val resultThrowable = synchronousStageExecutor.reportError("EventName", throwable)
        assertThat(resultThrowable).usingRecursiveComparison().isEqualTo(throwable)
    }

    @Test
    fun reportErrorIfThrowableIsNull() {
        val resultThrowable = synchronousStageExecutor.reportError("EventName", null)
        assertThat(resultThrowable).isExactlyInstanceOf(AppMetricaThrowable::class.java)
        val stacktrace = resultThrowable.stackTrace
        assertThat(stacktrace[0].className)
            .isEqualTo("io.appmetrica.analytics.impl.proxy.synchronous.ReporterSynchronousStageExecutor")
        assertThat(stacktrace[0].methodName).isEqualTo("reportError")
    }
}
