package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Handler
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.impl.modules.ModuleStatusReporter
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaImplFieldsProviderTest : CommonTest() {

    private val handler: Handler = mock()
    private val appMetrica: AppMetricaImpl = mock()
    private val receiver: DataResultReceiver = mock()
    private val processConfiguration: ProcessConfiguration = mock()
    private val reportsHandler: ReportsHandler = mock()
    private val commonExecutor: ICommonExecutor = mock()
    private val startupHelper: StartupHelper = mock()
    private val preferences: PreferencesClientDbStorage = mock()
    private var context: Context = mock()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val dataResultReceiverRule = constructionRule<DataResultReceiver>()

    @get:Rule
    val processConfigurationRule = constructionRule<ProcessConfiguration>()

    @get:Rule
    val reportsHandlerRule = constructionRule<ReportsHandler>()

    @get:Rule
    val startupHelperRule = constructionRule<StartupHelper>()

    @get:Rule
    val referrerHelperRule = constructionRule<ReferrerHelper>()

    @get:Rule
    val reporterFactoryRule = constructionRule<ReporterFactory>()

    @get:Rule
    val moduleStatusReporterRule = constructionRule<ModuleStatusReporter>()

    private val fieldsProvider by setUp { AppMetricaImplFieldsProvider() }

    @Test
    fun createDataResultReceiver() {
        assertThat(fieldsProvider.createDataResultReceiver(handler, appMetrica))
            .isEqualTo(dataResultReceiverRule.constructionMock.constructed().first())
        assertThat(dataResultReceiverRule.constructionMock.constructed()).hasSize(1)
        assertThat(dataResultReceiverRule.argumentInterceptor.flatArguments())
            .containsExactly(handler, appMetrica)
    }

    @Test
    fun createProcessConfiguration() {
        assertThat(fieldsProvider.createProcessConfiguration(context, receiver))
            .isEqualTo(processConfigurationRule.constructionMock.constructed().first())
        assertThat(processConfigurationRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(processConfigurationRule.argumentInterceptor.flatArguments())
            .containsExactly(context, receiver)
    }

    @Test
    fun createReportsHandler() {
        assertThat(fieldsProvider.createReportsHandler(processConfiguration, context, commonExecutor))
            .isEqualTo(reportsHandlerRule.constructionMock.constructed().first())
        assertThat(reportsHandlerRule.constructionMock.constructed()).hasSize(1)
        assertThat(reportsHandlerRule.argumentInterceptor.flatArguments())
            .containsExactly(processConfiguration, context, commonExecutor)
    }

    @Test
    fun createStartupHelper() {
        assertThat(fieldsProvider.createStartupHelper(context, reportsHandler, handler))
            .isEqualTo(startupHelperRule.constructionMock.constructed().first())
        assertThat(startupHelperRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupHelperRule.argumentInterceptor.flatArguments())
            .containsExactly(reportsHandler, ClientServiceLocator.getInstance().getStartupParams(context), handler)
    }

    @Test
    fun createReferrerHelper() {
        assertThat(fieldsProvider.createReferrerHelper(reportsHandler, preferences, handler))
            .isEqualTo(referrerHelperRule.constructionMock.constructed().first())
        assertThat(referrerHelperRule.constructionMock.constructed()).hasSize(1)
        assertThat(referrerHelperRule.argumentInterceptor.flatArguments())
            .containsExactly(reportsHandler, preferences, handler)
    }

    @Test
    fun createReporterFactory() {
        val reporterFactory = fieldsProvider.createReporterFactory(
            context,
            processConfiguration,
            reportsHandler,
            handler,
            startupHelper
        )
        assertThat(reporterFactory).isEqualTo(reporterFactoryRule.constructionMock.constructed().first())
        assertThat(reporterFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterFactoryRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                processConfiguration,
                reportsHandler,
                handler,
                startupHelper
            )
    }

    @Test
    fun createModuleStatusReporter() {
        val moduleStatusReporter = fieldsProvider.createModuleStatusReporter(context)
        assertThat(moduleStatusReporter).isEqualTo(moduleStatusReporterRule.constructionMock.constructed().first())
        assertThat(moduleStatusReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(moduleStatusReporterRule.argumentInterceptor.flatArguments())
            .contains(
                ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor,
                ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context),
                "client_modules"
            )
    }
}
