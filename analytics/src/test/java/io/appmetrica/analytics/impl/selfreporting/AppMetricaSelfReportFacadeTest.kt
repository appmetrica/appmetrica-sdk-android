package io.appmetrica.analytics.impl.selfreporting

import android.content.Context
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.SdkData
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy
import io.appmetrica.analytics.impl.proxy.AppMetricaProxyProvider
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AppMetricaSelfReportFacadeTest : CommonTest() {
    private val selfReporterWrapper: SelfReporterWrapper = mock()
    private var context: Context = mock()
    private val executor: IHandlerExecutor = mock()
    private val runnableCaptor = argumentCaptor<Runnable>()

    @get:Rule
    val selfReportFacadeProvider: MockedStaticRule<SelfReportFacadeProvider> = staticRule<SelfReportFacadeProvider> {
        on { SelfReportFacadeProvider.getReporterWrapper() } doReturn selfReporterWrapper
    }

    @get:Rule
    val clientServiceLocatorRule: ClientServiceLocatorRule = ClientServiceLocatorRule()

    private val appMetricaProxy: AppMetricaProxy = mock()

    @get:Rule
    val appMetricaProxyProviderRule = staticRule<AppMetricaProxyProvider> {
        on { AppMetricaProxyProvider.getProxy() } doReturn appMetricaProxy
    }

    @get:Rule
    val appMetricaRule = staticRule<AppMetrica>()

    @Before
    fun setUp() {
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor)
            .thenReturn(executor)
    }

    @Test
    fun getReporter() {
        assertThat(AppMetricaSelfReportFacade.getReporter()).isEqualTo(selfReporterWrapper)
    }

    @Test
    fun onInitializationFinished() {
        AppMetricaSelfReportFacade.onInitializationFinished(context)
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(5000L))
        runnableCaptor.firstValue.run()
        verify(selfReporterWrapper).onInitializationFinished(context)
    }

    @Test
    fun onFullyInitializationFinished() {
        AppMetricaSelfReportFacade.onFullyInitializationFinished(context)
        verify(executor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(selfReporterWrapper).onInitializationFinished(context)
    }

    @Test
    fun warmupForSelfProcess() {
        AppMetricaSelfReportFacade.warmupForSelfProcess(context)
        verify(appMetricaProxy).warmUpForSelfProcess(context)
        appMetricaRule.staticMock.verify { AppMetrica.getReporter(context, SdkData.SDK_API_KEY_UUID) }
    }
}
