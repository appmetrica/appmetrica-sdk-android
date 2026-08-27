package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.validation.ValidationResult
import io.appmetrica.analytics.coreutils.internal.validation.Validator
import io.appmetrica.analytics.impl.ActivityLifecycleManager
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig
import io.appmetrica.analytics.impl.SessionsTrackingManager
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.Barrier
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.MockProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
internal class AppMetricaProxyBarrierTests(
    name: String,
    ifNoArgs: Boolean,
    args: Array<Class<*>>
) : BaseAppMetricaProxyBarrierTests(name, ifNoArgs, args) {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val reporterProxyStorage: ReporterProxyStorage = mock {
        on { getOrCreate(any<Context>(), any<String>()) } doReturn mock()
    }
    private val silentActivationValidator: SilentActivationValidator = mock {
        on { validate() } doReturn ValidationResult.successful(mock<Validator<Any>>())
    }
    protected override val barrier by setUp { mock<Barrier>() }
    private val synchronousStageExecutor: SynchronousStageExecutor = mock {
        on { reportError(any(), any()) } doReturn mock()
    }
    private val sessionsTrackingManager: SessionsTrackingManager = mock {
        on { startWatchingIfNotYet() } doReturn ActivityLifecycleManager.WatchingStatus.WATCHING
    }
    protected override val proxy by setUp {
        AppMetricaProxy(
            provider,
            barrier,
            silentActivationValidator,
            mock<WebViewJsInterfaceHandler>(),
            synchronousStageExecutor,
            reporterProxyStorage,
            mock<DefaultOneShotMetricaConfig>(),
            sessionsTrackingManager
        )
    }

    @Before
    fun setUpClientServiceLocator() {
        val executor: IHandlerExecutor = MockProvider.mockedBlockingExecutorMock()
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor).thenReturn(executor)
        whenever(ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(any<Context>()).readUuid())
            .thenReturn(mock<IdentifiersResult>())
    }

    @Test
    fun callsBarrier() {
        verifyBarrierCall()
    }

    companion object {

        private val methodsNotToCheck = listOf(
            "getDeviceId",
            "reportExternalAdRevenue"
        )
        private val methodsWithNoArguments = listOf("pauseSession", "resumeSession")

        @JvmStatic
        @Parameterized.Parameters(name = "Test if {0} is called")
        fun data(): Collection<Array<Any>> = BaseAppMetricaProxyBarrierTests.data(
            methodsNotToCheck,
            methodsWithNoArguments,
            AppMetricaProxy::class.java
        )
    }
}
