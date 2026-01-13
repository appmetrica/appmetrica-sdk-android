package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.StartupParamsCallback
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.DeeplinkConsumer
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.SessionsTrackingManager
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.Barrier
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextCoverageUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class AppMetricaProxyContextTest : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val mainReporter: MainReporter = mock()
    private val mainReporterApiConsumerProvider: MainReporterApiConsumerProvider = mock()
    private val deeplinkConsumer: DeeplinkConsumer = mock()
    private val impl: AppMetricaFacade = mock()
    private val provider: AppMetricaFacadeProvider = mock()
    private val reporterProxyStorage: ReporterProxyStorage = mock()
    private val barrier: Barrier = mock()
    private val applicationContext: Context = mock()
    private val context: Context = mock {
        on { applicationContext } doReturn applicationContext
    }
    private val synchronousStageExecutor: SynchronousStageExecutor = mock()
    private val defaultOneShotMetricaConfig: DefaultOneShotMetricaConfig = mock()
    private val webViewJsInterfaceHandler: WebViewJsInterfaceHandler = mock()
    private val silentActivationValidator: SilentActivationValidator = mock()
    private val sessionsTrackingManager: SessionsTrackingManager = mock()
    private val startupParamsCallback: StartupParamsCallback = mock()
    private val params: List<String> = mock()
    private val executor: IHandlerExecutor = mock()

    private lateinit var proxy: AppMetricaProxy
    private val apiKey = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        whenever(provider.peekInitializedImpl()).thenReturn(impl)
        whenever(provider.getInitializedImpl(context)).thenReturn(impl)
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor).thenReturn(executor)
        proxy = AppMetricaProxy(
            provider,
            barrier,
            silentActivationValidator,
            webViewJsInterfaceHandler,
            synchronousStageExecutor,
            reporterProxyStorage,
            defaultOneShotMetricaConfig,
            sessionsTrackingManager
        )
        whenever(impl.mainReporterApiConsumerProvider).thenReturn(mainReporterApiConsumerProvider)
        doReturn(mainReporter).whenever(mainReporterApiConsumerProvider).mainReporter
        doReturn(deeplinkConsumer).whenever(mainReporterApiConsumerProvider).deeplinkConsumer
    }

    @Test
    fun activate() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        proxy.activate(context, config)
        verify(barrier).activate(context, config)
        verify(synchronousStageExecutor).activate(applicationContext, config)
    }

    @Test
    fun getReporter() {
        proxy.getReporter(context, apiKey)
        verify(barrier).getReporter(context, apiKey)
        verify(synchronousStageExecutor).getReporter(applicationContext, apiKey)
    }

    @Test
    fun activateReporter() {
        val config = ReporterConfig.newConfigBuilder(apiKey).build()
        proxy.activateReporter(context, config)
        verify(barrier).activateReporter(context, config)
        verify(synchronousStageExecutor).activateReporter(applicationContext, config)
    }

    @Test
    fun requestStartupParams() {
        proxy.requestStartupParams(context, startupParamsCallback, params)
        verify(barrier).requestStartupParams(context, startupParamsCallback, params)
        verify(synchronousStageExecutor).requestStartupParams(applicationContext, startupParamsCallback, params)
    }

    @Test
    fun getUuid() {
        proxy.getUuid(context)
        verify(barrier).getUuid(context)
        verify(synchronousStageExecutor).getUuid(applicationContext)
    }

    @Test
    fun getDeviceId() {
        proxy.getDeviceId(context)
        verify(barrier).getDeviceId(context)
        verify(synchronousStageExecutor).getDeviceId(applicationContext)
    }

    @Test
    fun warmUpForSelfProcess() {
        proxy.warmUpForSelfProcess(context)
        verify(barrier).warmUpForSelfProcess(context)
        verify(synchronousStageExecutor).warmUpForSelfReporter(context)
    }

    @Test
    fun coverage() {
        ContextCoverageUtils.checkCoverage(AppMetricaProxy::class.java, AppMetricaProxyContextTest::class.java)
    }
}
