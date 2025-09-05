package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.PreloadInfo
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter
import io.appmetrica.analytics.impl.crash.jvm.converter.AnrConverter
import io.appmetrica.analytics.impl.crash.jvm.converter.CustomErrorConverter
import io.appmetrica.analytics.impl.crash.jvm.converter.RegularErrorConverter
import io.appmetrica.analytics.impl.crash.jvm.converter.UnhandledExceptionConverter
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashClient
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoWrapper
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.CounterConfigurationReporterType
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.UUID

internal class MainReporterComponentsTest : CommonTest() {

    private val context: Context = mock()
    private val reporterFactoryProvider: IReporterFactoryProvider = mock()
    private val processConfiguration: ProcessConfiguration = mock()
    private val reportsHandler: ReportsHandler = mock()
    private val startupHelper: StartupHelper = mock()
    private val apiKey = UUID.randomUUID().toString()

    @get:Rule
    val unhandledSituationReporterProviderMockedConstructionRule =
        constructionRule<UnhandledSituationReporterProvider>()

    @get:Rule
    val nativeCrashClientMockedConstructionRule = constructionRule<NativeCrashClient>()

    @get:Rule
    val extraMetaInfoRetrieverMockedConstructionRule = constructionRule<ExtraMetaInfoRetriever>()

    @get:Rule
    val counterConfigurationMockedConstructionRule = constructionRule<CounterConfiguration>()
    private val reporterConfiguration: CounterConfiguration by counterConfigurationMockedConstructionRule

    @get:Rule
    val reporterEnvironmentMockedConstructionRule = constructionRule<ReporterEnvironment> {
        on { reporterConfiguration } doReturn reporterConfiguration
    }
    private val reporterEnvironment: ReporterEnvironment by reporterEnvironmentMockedConstructionRule

    @get:Rule
    val appStatusMonitorMockedConstructionRule = constructionRule<AppStatusMonitor>()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val activityStateManagerMockedConstructionRule = constructionRule<ActivityStateManager>()

    @get:Rule
    val pluginErrorDetailsConverterMockedConstructionRule = constructionRule<PluginErrorDetailsConverter>()

    @get:Rule
    val unhandledExceptionConverterMockedConstructionRule = constructionRule<UnhandledExceptionConverter>()

    @get:Rule
    val regularErrorConverterMockedConstructionRule = constructionRule<RegularErrorConverter>()

    @get:Rule
    val customErrorConverterMockedConstructionRule = constructionRule<CustomErrorConverter>()

    @get:Rule
    val anrConverterMockedConstructionRule = constructionRule<AnrConverter>()

    @get:Rule
    val preloadInfoWrapperMockedConstructionRule = constructionRule<PreloadInfoWrapper>()

    @get:Rule
    val errorEnvironmentMockedConstructionRule = constructionRule<ErrorEnvironment>()

    @get:Rule
    val simpleMapLimitationMockedConstructionRule = constructionRule<SimpleMapLimitation>()

    private val logger: PublicLogger = mock()

    @get:Rule
    val loggerStorageMockedStaticRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getMainPublicOrAnonymousLogger() } doReturn logger
        on { LoggerStorage.getOrCreatePublicLogger(apiKey) } doReturn logger
    }

    private val mainReporterComponents: MainReporterComponents by setUp {
        MainReporterComponents(context, reporterFactoryProvider, processConfiguration, reportsHandler, startupHelper)
    }

    @Test
    fun nativeCrashClient() {
        assertThat(nativeCrashClientMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterComponents.nativeCrashClient)
            .isEqualTo(nativeCrashClientMockedConstructionRule.constructionMock.constructed().first())
        assertThat(nativeCrashClientMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(processConfiguration)
    }

    @Test
    fun extraMetaInfoRetriever() {
        assertThat(extraMetaInfoRetrieverMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterComponents.extraMetaInfoRetriever)
            .isEqualTo(extraMetaInfoRetrieverMockedConstructionRule.constructionMock.constructed().first())
        assertThat(extraMetaInfoRetrieverMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
    }

    @Test
    fun reporterEnvironment() {
        assertThat(reporterEnvironmentMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterComponents.reporterEnvironment)
            .isEqualTo(reporterEnvironmentMockedConstructionRule.constructionMock.constructed().first())
        assertThat(reporterEnvironmentMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                processConfiguration,
                counterConfigurationMockedConstructionRule.constructionMock.constructed().first(),
                errorEnvironmentMockedConstructionRule.constructionMock.constructed().first()
            )
        assertThat(counterConfigurationMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(counterConfigurationMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(CounterConfigurationReporterType.MAIN)

        assertThat(errorEnvironmentMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(errorEnvironmentMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(simpleMapLimitationMockedConstructionRule.constructionMock.constructed().first())

        assertThat(simpleMapLimitationMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(simpleMapLimitationMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(logger, ErrorEnvironment.TAG)
    }

    @Test
    fun appStatusMonitor() {
        assertThat(mainReporterComponents.appStatusMonitor)
            .isEqualTo(appStatusMonitorMockedConstructionRule.constructionMock.constructed().first())
        assertThat(appStatusMonitorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(appStatusMonitorMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun processDetector() {
        assertThat(mainReporterComponents.processDetector)
            .isEqualTo(ClientServiceLocator.getInstance().processNameProvider)
    }

    @Test
    fun activityStateManager() {
        assertThat(mainReporterComponents.activityStateManager)
            .isEqualTo(activityStateManagerMockedConstructionRule.constructionMock.constructed().first())
        assertThat(activityStateManagerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        activityStateManagerMockedConstructionRule.argumentInterceptor.flatArguments().isEmpty()
    }

    @Test
    fun pluginErrorDetailsConverter() {
        assertThat(mainReporterComponents.pluginErrorDetailsConverter)
            .isEqualTo(pluginErrorDetailsConverterMockedConstructionRule.constructionMock.constructed().first())
        assertThat(pluginErrorDetailsConverterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(pluginErrorDetailsConverterMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(extraMetaInfoRetrieverMockedConstructionRule.constructionMock.constructed().first())
    }

    @Test
    fun unhandledExceptionConverter() {
        assertThat(mainReporterComponents.unhandledExceptionConverter)
            .isEqualTo(unhandledExceptionConverterMockedConstructionRule.constructionMock.constructed().first())
        assertThat(unhandledExceptionConverterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(unhandledExceptionConverterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun regularErrorConverter() {
        assertThat(mainReporterComponents.regularErrorConverter)
            .isEqualTo(regularErrorConverterMockedConstructionRule.constructionMock.constructed().first())
        assertThat(regularErrorConverterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(regularErrorConverterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun customErrorConverter() {
        assertThat(mainReporterComponents.customErrorConverter)
            .isEqualTo(customErrorConverterMockedConstructionRule.constructionMock.constructed().first())
        assertThat(customErrorConverterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(customErrorConverterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun anrConverter() {
        assertThat(mainReporterComponents.anrConverter)
            .isEqualTo(anrConverterMockedConstructionRule.constructionMock.constructed().first())
        assertThat(anrConverterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(anrConverterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }

    @Test
    fun updateConfig() {
        val userProfileId = "User profile id"
        val preloadInfo = PreloadInfo.newBuilder("TrackingId").build()
        val config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withUserProfileID(userProfileId)
            .withPreloadInfo(preloadInfo)
            .build()

        mainReporterComponents.updateConfig(config, logger)

        verify(reporterConfiguration).applyFromConfig(config)
        verify(reporterEnvironment).initialUserProfileID = userProfileId
        verify(reporterEnvironment).preloadInfoWrapper =
            preloadInfoWrapperMockedConstructionRule.constructionMock.constructed().first()
        assertThat(preloadInfoWrapperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(preloadInfoWrapperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(preloadInfo, logger, DefaultValues.DEFAULT_AUTO_PRELOAD_INFO_DETECTION)
    }

    @Test
    fun updateAnonymousConfig() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey)
            .build()

        mainReporterComponents.updateAnonymousConfig(config, logger)

        verify(reporterConfiguration).applyFromAnonymousConfig(config)
    }
}
