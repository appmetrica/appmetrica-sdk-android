package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.StartupParamsCallback
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextCoverageUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.UUID

class SynchronousStageExecutorContextTest : CommonTest() {

    @Mock
    private lateinit var mProvider: AppMetricaFacadeProvider
    @Mock
    private lateinit var appMetricaFacade: AppMetricaFacade
    @Mock
    private lateinit var webViewJsInterfaceHandler: WebViewJsInterfaceHandler
    @Mock
    private lateinit var sessionsTrackingManager: SessionsTrackingManager
    @Mock
    private lateinit var activityLifecycleManager: ActivityLifecycleManager
    @Mock
    private lateinit var contextAppearedListener: ContextAppearedListener
    @Mock
    private lateinit var context: Context
    private lateinit var synchronousStageExecutor: SynchronousStageExecutor
    private val apiKey = UUID.randomUUID().toString()

    @Rule
    @JvmField
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mProvider.getInitializedImpl(context)).thenReturn(appMetricaFacade)
        `when`(mProvider.peekInitializedImpl()).thenReturn(appMetricaFacade)
        synchronousStageExecutor = SynchronousStageExecutor(
            mProvider,
            webViewJsInterfaceHandler,
            activityLifecycleManager,
            sessionsTrackingManager,
            contextAppearedListener
        )
    }

    @Test
    fun activate() {
        synchronousStageExecutor.activate(context, AppMetricaConfig.newConfigBuilder(apiKey).build())
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun activateReporter() {
        synchronousStageExecutor.activateReporter(context, ReporterConfig.newConfigBuilder(apiKey).build())
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun initializeDeprecated() {
        synchronousStageExecutor.initialize(context)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun initialize() {
        synchronousStageExecutor.initialize(context, AppMetricaConfig.newConfigBuilder(apiKey).build())
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun requestStartupParamsWithStartupParamsCallback() {
        synchronousStageExecutor.requestStartupParams(context, mock(StartupParamsCallback::class.java), emptyList())
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun setStatisticsSending() {
        synchronousStageExecutor.setStatisticsSending(context, true)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun getFeatures() {
        synchronousStageExecutor.getFeatures(context)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun getUuid() {
        synchronousStageExecutor.getUuid(context)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun coverage() {
        ContextCoverageUtils.checkCoverage(
            SynchronousStageExecutor::class.java,
            SynchronousStageExecutorContextTest::class.java
        )
    }
}
