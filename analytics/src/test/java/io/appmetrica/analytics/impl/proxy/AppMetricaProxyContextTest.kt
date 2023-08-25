package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.StartupParamsCallback
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.ContextAppearedListener
import io.appmetrica.analytics.impl.DeeplinkConsumer
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.SessionsTrackingManager
import io.appmetrica.analytics.impl.SynchronousStageExecutor
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.impl.proxy.validation.MainFacadeBarrier
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextCoverageUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.same
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
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
    private val barrier: MainFacadeBarrier = mock()
    private val applicationContext: Context = mock()
    private val context: Context = mock {
        on { applicationContext } doReturn applicationContext
    }
    private val synchronousStageExecutor: SynchronousStageExecutor = mock()
    private val defaultOneShotMetricaConfig: DefaultOneShotMetricaConfig = mock()
    private val webViewJsInterfaceHandler: WebViewJsInterfaceHandler = mock()
    private val silentActivationValidator: SilentActivationValidator = mock()
    private val activationValidator: ActivationValidator = mock()
    private val sessionsTrackingManager: SessionsTrackingManager = mock()
    private val contextAppearedListener: ContextAppearedListener = mock()
    private val executor: ICommonExecutor = mock()
    private val startupParamsCallback: StartupParamsCallback = mock()
    private val params: List<String> = mock()

    private lateinit var proxy: AppMetricaProxy
    private val apiKey = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(provider.peekInitializedImpl()).thenReturn(impl)
        whenever(provider.getInitializedImpl(context)).thenReturn(impl)
        proxy = AppMetricaProxy(
            provider,
            executor,
            barrier,
            activationValidator,
            silentActivationValidator,
            webViewJsInterfaceHandler,
            synchronousStageExecutor,
            reporterProxyStorage,
            defaultOneShotMetricaConfig,
            sessionsTrackingManager,
            contextAppearedListener
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
    fun setLocationTracking() {
        proxy.setLocationTracking(context, true)
        verify(barrier).setLocationTracking(context, true)
        verify(synchronousStageExecutor).setLocationTracking(applicationContext, true)
    }

    @Test
    fun setStatisticsSending() {
        proxy.setStatisticsSending(context, true)
        verify(barrier).setStatisticsSending(context, true)
        verify(synchronousStageExecutor).setStatisticsSending(applicationContext, true)
    }

    @Test
    fun getReporter() {
        proxy.getReporter(context, apiKey)
        verify(barrier).getReporter(context, apiKey)
        verify(contextAppearedListener).onProbablyAppeared(applicationContext)
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
    fun coverage() {
        ContextCoverageUtils.checkCoverage(AppMetricaProxy::class.java, AppMetricaProxyContextTest::class.java)
    }
}
