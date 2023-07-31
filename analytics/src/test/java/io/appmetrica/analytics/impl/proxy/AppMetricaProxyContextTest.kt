package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.AnrListener
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
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.refEq
import java.util.UUID

class AppMetricaProxyContextTest : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @Mock
    private lateinit var mainReporter: MainReporter

    @Mock
    private lateinit var mainReporterApiConsumerProvider: MainReporterApiConsumerProvider

    @Mock
    private lateinit var deeplinkConsumer: DeeplinkConsumer

    @Mock
    private lateinit var impl: AppMetricaFacade

    @Mock
    private lateinit var provider: AppMetricaFacadeProvider

    @Mock
    private lateinit var reporterProxyStorage: ReporterProxyStorage

    @Mock
    private lateinit var barrier: MainFacadeBarrier

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var synchronousStageExecutor: SynchronousStageExecutor

    @Mock
    private lateinit var defaultOneShotMetricaConfig: DefaultOneShotMetricaConfig

    @Mock
    private lateinit var webViewJsInterfaceHandler: WebViewJsInterfaceHandler

    @Mock
    private lateinit var silentActivationValidator: SilentActivationValidator

    @Mock
    private lateinit var activationValidator: ActivationValidator

    @Mock
    private lateinit var sessionsTrackingManager: SessionsTrackingManager

    @Mock
    private lateinit var contextAppearedListener: ContextAppearedListener

    @Mock
    private lateinit var executor: ICommonExecutor

    @Mock
    private lateinit var startupParamsCallback: StartupParamsCallback

    @Mock
    private lateinit var params: List<String>
    private lateinit var proxy: AppMetricaProxy
    private val apiKey = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(provider.peekInitializedImpl()).thenReturn(impl)
        `when`(provider.getInitializedImpl(context)).thenReturn(impl)
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
        `when`(impl.mainReporterApiConsumerProvider).thenReturn(mainReporterApiConsumerProvider)
        doReturn(mainReporter).`when`(mainReporterApiConsumerProvider).mainReporter
        doReturn(deeplinkConsumer).`when`(mainReporterApiConsumerProvider).deeplinkConsumer
    }

    @Test
    fun activate() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        proxy.activate(context, config)
        verify(synchronousStageExecutor).activate(same(context), any(AppMetricaConfig::class.java))
    }

    @Test
    fun setLocationTracking() {
        proxy.setLocationTracking(context, true)
        verify(synchronousStageExecutor).setLocationTracking(context, true)
    }

    @Test
    fun setStatisticsSending() {
        proxy.setStatisticsSending(context, true)
        verify(synchronousStageExecutor).setStatisticsSending(context, true)
    }

    @Test
    fun getReporter() {
        proxy.getReporter(context, apiKey)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun activateReporter() {
        val config = ReporterConfig.newConfigBuilder(apiKey).build()
        proxy.activateReporter(context, config)
        verify(synchronousStageExecutor).activateReporter(same(context), any(ReporterConfig::class.java))
    }

    @Test
    fun requestStartupParams() {
        proxy.requestStartupParams(context, startupParamsCallback, params)
        verify(synchronousStageExecutor).requestStartupParams(context, startupParamsCallback, params)
    }

    @Test
    fun getUuid() {
        proxy.getUuid(context)
        verify(synchronousStageExecutor).getUuid(context)
    }

    @Test
    fun coverage() {
        ContextCoverageUtils.checkCoverage(AppMetricaProxy::class.java, AppMetricaProxyContextTest::class.java)
    }
}
