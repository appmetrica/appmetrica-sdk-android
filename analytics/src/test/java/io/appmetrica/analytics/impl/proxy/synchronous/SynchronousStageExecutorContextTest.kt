package io.appmetrica.analytics.impl.proxy.synchronous

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.StartupParamsCallback
import io.appmetrica.analytics.impl.ActivityLifecycleManager
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.ContextAppearedListener
import io.appmetrica.analytics.impl.SessionsTrackingManager
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextCoverageUtils
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.UUID

class SynchronousStageExecutorContextTest : CommonTest() {

    private val context: Context = mock()
    private val appMetricaFacade: AppMetricaFacade = mock()
    private val provider: AppMetricaFacadeProvider = mock {
        on { getInitializedImpl(eq(context)) } doReturn appMetricaFacade
        on { peekInitializedImpl() } doReturn appMetricaFacade
    }
    private val webViewJsInterfaceHandler: WebViewJsInterfaceHandler = mock()
    private val sessionsTrackingManager: SessionsTrackingManager = mock()
    private val activityLifecycleManager: ActivityLifecycleManager = mock()
    private val contextAppearedListener: ContextAppearedListener = mock()
    private val firstLaunchDetector: FirstLaunchDetector = mock()

    private val synchronousStageExecutor = SynchronousStageExecutor(
        provider,
        webViewJsInterfaceHandler,
        activityLifecycleManager,
        sessionsTrackingManager,
        contextAppearedListener,
        firstLaunchDetector
    )
    private val apiKey = UUID.randomUUID().toString()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @Test
    fun activate() {
        synchronousStageExecutor.activate(context, AppMetricaConfig.newConfigBuilder(apiKey).build())
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun getReporter() {
        synchronousStageExecutor.getReporter(context, apiKey)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun activateReporter() {
        synchronousStageExecutor.activateReporter(context, ReporterConfig.newConfigBuilder(apiKey).build())
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun requestStartupParams() {
        synchronousStageExecutor.requestStartupParams(
            context,
            Mockito.mock(StartupParamsCallback::class.java),
            emptyList()
        )
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun getUuid() {
        synchronousStageExecutor.getUuid(context)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun getDeviceId() {
        synchronousStageExecutor.getDeviceId(context)
        verify(contextAppearedListener).onProbablyAppeared(context)
    }

    @Test
    fun warmUpForSelfReporter() {
        synchronousStageExecutor.warmUpForSelfReporter(context)
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
