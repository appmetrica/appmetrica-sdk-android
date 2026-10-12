package io.appmetrica.analytics.impl.selfreporting

import android.content.Context
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.AppMetricaPluginsImpl
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.SdkData
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy
import io.appmetrica.analytics.impl.proxy.AppMetricaProxyProvider
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
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

    private val appMetricaProxyRule = constructionRule<AppMetricaProxy>()
    private val appMetricaPluginsImplRule = constructionRule<AppMetricaPluginsImpl>()
    private val clientServiceLocatorRule: ClientServiceLocatorRule = ClientServiceLocatorRule()

    private val selfReportFacadeProvider: MockedStaticRule<SelfReportFacadeProvider> =
        staticRule<SelfReportFacadeProvider> {
            on { SelfReportFacadeProvider.getReporterWrapper() } doReturn selfReporterWrapper
        }

    private val appMetricaProxy: AppMetricaProxy = mock()

    private val appMetricaProxyProviderRule = staticRule<AppMetricaProxyProvider> {
        on { AppMetricaProxyProvider.getProxy() } doReturn appMetricaProxy
    }

    private val appMetricaRule = staticRule<AppMetrica>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(clientServiceLocatorRule)
        .around(appMetricaProxyRule)
        .around(appMetricaPluginsImplRule)
        .around(selfReportFacadeProvider)
        .around(appMetricaProxyProviderRule)
        .around(appMetricaRule)

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
        verify(executor).postDelayed(runnableCaptor.capture(), eq(5000L))
        runnableCaptor.firstValue.run()
        verify(selfReporterWrapper).onInitializationFinished(context)
    }

    @Test
    fun onFullyInitializationFinished() {
        AppMetricaSelfReportFacade.onFullyInitializationFinished(context)
        verify(executor).post(runnableCaptor.capture())
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
