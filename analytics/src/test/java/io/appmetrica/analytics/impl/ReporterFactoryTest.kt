package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Handler
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.coreutils.internal.ApiKeyUtils
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.reporter.MainReporterContext
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator
import io.appmetrica.analytics.impl.utils.validation.api.ReporterKeyIsUsedValidator
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
internal class ReporterFactoryTest : CommonTest() {

    @get:Rule
    val logRule = LogRule()

    private val context: Context = mock {
        on { applicationContext } doReturn it
    }

    private val processConfiguration: ProcessConfiguration = mock()
    private val reportsHandler: ReportsHandler = mock()
    private val taskHandler: Handler = mock()
    private val startupHelper: StartupHelper = mock()

    private val errorEnvironment: ErrorEnvironment = mock()

    @get:Rule
    val mainReporterComponentsMockedConstructionRule = constructionRule<MainReporterComponents> {
        on { errorEnvironment } doReturn errorEnvironment
    }
    private val mainReporterComponents: MainReporterComponents by mainReporterComponentsMockedConstructionRule

    @get:Rule
    val reporterApiKeyUsedValidatorRule = constructionRule<ReporterKeyIsUsedValidator>()
    private val reporterApiKeyIsUsedValidator: ReporterKeyIsUsedValidator by reporterApiKeyUsedValidatorRule

    @get:Rule
    val throwIfFailedValidatorMockedConstructionRule = constructionRule<ThrowIfFailedValidator<String>>()
    private val throwIfFailedValidator: ThrowIfFailedValidator<String> by throwIfFailedValidatorMockedConstructionRule

    @get:Rule
    val mainReporterContextMockedConstructionRule = constructionRule<MainReporterContext>()
    private val mainReporterContext: MainReporterContext by mainReporterContextMockedConstructionRule

    @get:Rule
    val mainReporterConstructionRule = constructionRule<MainReporter>()
    private val mainReporter: MainReporter by mainReporterConstructionRule

    @get:Rule
    val manualReporterConstructionRule = constructionRule<ManualReporter>()
    private val manualReporter: ManualReporter by manualReporterConstructionRule

    @get:Rule
    val crashReporterFieldsProviderMockedConstructionRule = constructionRule<CrashReporterFieldsProvider>()

    @get:Rule
    val crashReporterMockedConstructionRule = constructionRule<CrashReporter>()
    private val crashReporter: CrashReporter by crashReporterMockedConstructionRule

    @get:Rule
    val keepAliveHandlerConstructionRule = constructionRule<KeepAliveHandler>()
    private val keepAliveHandler: KeepAliveHandler by keepAliveHandlerConstructionRule

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val apiKey = UUID.randomUUID().toString()
    private val obfuscatedApiKey = UUID.randomUUID().toString()

    @get:Rule
    val apiKeyUtilsMockedStaticRule = staticRule<ApiKeyUtils> {
        on { ApiKeyUtils.createPartialApiKey(apiKey) } doReturn obfuscatedApiKey
    }

    private val config = AppMetricaConfig.newConfigBuilder(apiKey).build()

    private val firstReporterApiKey = UUID.randomUUID().toString()
    private val secondReporterApiKey = UUID.randomUUID().toString()

    private val logger: PublicLogger = mock()
    private val needToClearEnvironment = false

    private val reporterLifecycleListener: ReporterLifecycleListener = mock()

    private val firstReporterLogger: PublicLogger = mock()
    private val secondReporterLogger: PublicLogger = mock()

    @get:Rule
    val loggerStorageMockedStaticRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getOrCreatePublicLogger(firstReporterApiKey) } doReturn firstReporterLogger
        on { LoggerStorage.getOrCreatePublicLogger(secondReporterApiKey) } doReturn secondReporterLogger
        on { LoggerStorage.getOrCreatePublicLogger(apiKey) } doReturn logger
    }

    private val reporterFactory: ReporterFactory by setUp {
        ReporterFactory(context, processConfiguration, reportsHandler, taskHandler, startupHelper)
    }

    @Test
    fun `mainReporterComponents creation`() {
        assertThat(mainReporterComponentsMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterComponentsMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, reporterFactory, processConfiguration, reportsHandler, startupHelper)
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter twice`() {
        val reporter = reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        assertThat(reporter)
            .isSameAs(reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment))
            .isSameAs(mainReporter)

        assertThat(mainReporterConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(mainReporterComponents)
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter validation`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        verify(throwIfFailedValidator).validate(apiKey)
        checkValidator(apiKey)
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter perform init keepAliveHandler`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        verify(mainReporter).setKeepAliveHandler(keepAliveHandler)
        assertThat(keepAliveHandlerConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(keepAliveHandlerConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(taskHandler, mainReporter)
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter init startup provider`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        verify(mainReporter).setStartupParamsProvider(startupHelper)
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter updates mainReporterComponents`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        verify(mainReporterComponents).updateConfig(config, logger)
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter init and start mainReporter`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        verify(mainReporter).updateConfig(config, needToClearEnvironment)
        verify(mainReporter).start()
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter init reportsHandler`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        val shouldDisconnectCheckerCaptor = argumentCaptor<ShouldDisconnectFromServiceChecker>()
        whenever(mainReporter.isPaused).thenReturn(true)
        verify(reportsHandler).setShouldDisconnectFromServiceChecker(shouldDisconnectCheckerCaptor.capture())
        assertThat(shouldDisconnectCheckerCaptor.firstValue.shouldDisconnect()).isTrue()
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter after buildOrUpdateAnonymousMainReporter`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        clearInvocations(
            reportsHandler,
            mainReporterComponents,
            startupHelper,
            keepAliveHandler,
            throwIfFailedValidator
        )
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        verifyNoInteractions(
            reportsHandler,
            mainReporterComponents,
            startupHelper,
            keepAliveHandler,
            throwIfFailedValidator
        )
    }

    @Test
    fun `buildOrUpdateAnonymousMainReporter after buildOrUpdateMainReporter`() {
        val mainReporter = reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        clearInvocations(
            reportsHandler,
            mainReporterComponents,
            startupHelper,
            keepAliveHandler,
            throwIfFailedValidator
        )
        assertThat(reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment))
            .isSameAs(mainReporter)
        verifyNoInteractions(
            reportsHandler,
            mainReporterComponents,
            startupHelper,
            keepAliveHandler,
            throwIfFailedValidator
        )
    }

    @Test
    fun `buildOrUpdateMainReporter twice`() {
        val reporter = reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        assertThat(reporter)
            .isSameAs(reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment))
            .isSameAs(mainReporter)

        assertThat(mainReporterConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(mainReporterComponents)
    }

    @Test
    fun `buildOrUpdateMainReporter validation`() {
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        verify(throwIfFailedValidator).validate(apiKey)
        checkValidator(apiKey)
    }

    @Test
    fun `buildOrUpdateMainReporter perform init keepAliveHandler`() {
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        verify(mainReporter).setKeepAliveHandler(keepAliveHandler)
        assertThat(keepAliveHandlerConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(keepAliveHandlerConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(taskHandler, mainReporter)
    }

    @Test
    fun `buildOrUpdateMainReporter init startup provider`() {
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        verify(mainReporter).setStartupParamsProvider(startupHelper)
    }

    @Test
    fun `buildOrUpdateMainReporter updates mainReporterComponents`() {
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        verify(mainReporterComponents).updateConfig(config, logger)
    }

    @Test
    fun `buildOrUpdateMainReporter init and start mainReporter`() {
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        verify(mainReporter).updateConfig(config, needToClearEnvironment)
        verify(mainReporter).start()
    }

    @Test
    fun `buildOrUpdateMainReporter init reportsHandler`() {
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        val shouldDisconnectCheckerCaptor = argumentCaptor<ShouldDisconnectFromServiceChecker>()
        whenever(mainReporter.isPaused).thenReturn(true)
        verify(reportsHandler).setShouldDisconnectFromServiceChecker(shouldDisconnectCheckerCaptor.capture())
        assertThat(shouldDisconnectCheckerCaptor.firstValue.shouldDisconnect()).isTrue()
    }

    @Test
    fun `buildOrUpdateMainReporter notify reporter lifecycle listener if null`() {
        whenever(ClientServiceLocator.getInstance().reporterLifecycleListener).thenReturn(null)
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
    }

    @Test
    fun `buildOrUpdateMainReporter notify reporter lifecycle listener`() {
        whenever(ClientServiceLocator.getInstance().reporterLifecycleListener).thenReturn(reporterLifecycleListener)
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        verify(reporterLifecycleListener).onCreateMainReporter(mainReporterContext, mainReporter)
        assertThat(mainReporterContextMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterContextMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(mainReporterComponents, config, logger)
    }

    @Test
    fun `buildOrUpdateMainReporter after buildOrUpdateMainReporter`() {
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        clearInvocations(
            reportsHandler,
            mainReporterComponents,
            startupHelper,
            keepAliveHandler,
            throwIfFailedValidator
        )
        whenever(ClientServiceLocator.getInstance().reporterLifecycleListener).thenReturn(reporterLifecycleListener)
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        verifyNoInteractions(
            reportsHandler,
            startupHelper,
            keepAliveHandler,
            throwIfFailedValidator
        )
        verify(mainReporterComponents).updateConfig(config, logger)
        verify(reporterLifecycleListener)
            .onCreateMainReporter(
                mainReporterContextMockedConstructionRule.constructionMock.constructed()[1],
                mainReporter
            )
    }

    @Test
    fun `buildOrUpdateMainReporter after buildOrUpdateAnonymousMainReporter`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        clearInvocations(
            reportsHandler,
            mainReporterComponents,
            startupHelper,
            keepAliveHandler,
            throwIfFailedValidator
        )
        whenever(ClientServiceLocator.getInstance().reporterLifecycleListener).thenReturn(reporterLifecycleListener)
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        verifyNoInteractions(
            reportsHandler,
            startupHelper,
            keepAliveHandler,
            throwIfFailedValidator
        )
        verify(mainReporterComponents).updateConfig(config, logger)
        verify(reporterLifecycleListener).onCreateMainReporter(mainReporterContext, mainReporter)
    }

    @Test
    fun `buildAnonymousReporter, buildOrUpdateMainReporter and getReporter`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        val reporter = reporterFactory.getOrCreateReporter(ReporterConfig.newConfigBuilder(apiKey).build())
        assertThat(reporter).isSameAs(mainReporter)
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun activateReporter() {
        val config = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        reporterFactory.activateReporter(config)
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(manualReporterConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, processConfiguration, config, reportsHandler)
        verify(manualReporter).start()
    }

    @Test
    fun `activate reporter perform common initialization`() {
        val config = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        reporterFactory.activateReporter(config)
        verify(manualReporter).setKeepAliveHandler(keepAliveHandler)
        verify(manualReporter).setStartupParamsProvider(startupHelper)
        assertThat(keepAliveHandlerConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(taskHandler, manualReporter)
    }

    @Test
    fun `activate two reporters`() {
        val firstConfig = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        val secondConfig = ReporterConfig.newConfigBuilder(secondReporterApiKey).build()
        reporterFactory.activateReporter(firstConfig)
        reporterFactory.activateReporter(secondConfig)
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(manualReporterConstructionRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(context, processConfiguration, firstConfig, reportsHandler),
                listOf(context, processConfiguration, secondConfig, reportsHandler)
            )
    }

    @Test
    fun `activateReporter after buildOrUpdateMainReporter with same key`() {
        reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        reporterFactory.activateReporter(ReporterConfig.newConfigBuilder(apiKey).build())
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).isEmpty()
        verify(logger).warning("Reporter with apiKey=%s already exists.", obfuscatedApiKey)
    }

    @Test
    fun `activateReporter after buildMainAnonymousReporter with same key`() {
        reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        reporterFactory.activateReporter(ReporterConfig.newConfigBuilder(apiKey).build())
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun `activateReporter after getReporter`() {
        val config = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        reporterFactory.getOrCreateReporter(config)
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).hasSize(1)
        clearInvocations(manualReporterConstructionRule.constructionMock.constructed().first())
        reporterFactory.activateReporter(config)
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).hasSize(1)
        verifyNoInteractions(manualReporterConstructionRule.constructionMock.constructed().first())
    }

    @Test
    fun getOrCreateReporter() {
        val config = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        val reporter = reporterFactory.getOrCreateReporter(config)
        assertThat(reporter).isSameAs(manualReporter)
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(manualReporterConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, processConfiguration, config, reportsHandler)
    }

    @Test
    fun `getOrCreateReporter perform common initialization`() {
        val config = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        reporterFactory.getOrCreateReporter(config)
        verify(manualReporter).setKeepAliveHandler(keepAliveHandler)
        verify(manualReporter).setStartupParamsProvider(startupHelper)
        assertThat(keepAliveHandlerConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(taskHandler, manualReporter)
    }

    @Test
    fun `getOrCreateReporter twice for same api key`() {
        val firstConfig = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        val secondConfig = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        assertThat(reporterFactory.getOrCreateReporter(firstConfig))
            .isSameAs(reporterFactory.getOrCreateReporter(secondConfig))
    }

    @Test
    fun `getOrCreateReporter for different api key`() {
        val firstConfig = ReporterConfig.newConfigBuilder(firstReporterApiKey).build()
        val secondConfig = ReporterConfig.newConfigBuilder(secondReporterApiKey).build()
        val firstReporter = reporterFactory.getOrCreateReporter(firstConfig)
        val secondReporter = reporterFactory.getOrCreateReporter(secondConfig)
        assertThat(firstReporter).isNotSameAs(secondReporter)
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(manualReporterConstructionRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(context, processConfiguration, firstConfig, reportsHandler),
                listOf(context, processConfiguration, secondConfig, reportsHandler)
            )
    }

    @Test
    fun `getOrCreateReporter after buildOrUpdateMainReporter with same key`() {
        val mainReporter = reporterFactory.buildOrUpdateMainReporter(config, logger, needToClearEnvironment)
        val reporter = reporterFactory.getOrCreateReporter(ReporterConfig.newConfigBuilder(apiKey).build())
        assertThat(reporter).isSameAs(mainReporter)
    }

    @Test
    fun `getOrCreateReporter after buildMainAnonymousReporter`() {
        val mainReporter = reporterFactory.buildOrUpdateAnonymousMainReporter(config, logger, needToClearEnvironment)
        val reporter = reporterFactory.getOrCreateReporter(ReporterConfig.newConfigBuilder(apiKey).build())
        assertThat(reporter).isSameAs(mainReporter)
    }

    @Test
    fun `getOrCreateReporter after activate`() {
        reporterFactory.activateReporter(ReporterConfig.newConfigBuilder(firstReporterApiKey).build())
        reporterFactory.getOrCreateReporter(ReporterConfig.newConfigBuilder(firstReporterApiKey).build())
        assertThat(manualReporterConstructionRule.constructionMock.constructed()).hasSize(1)
    }

    @Test
    fun getUnhandledSituationReporter() {
        val firstConfig = AppMetricaConfig.newConfigBuilder(apiKey).build()
        val secondConfig = AppMetricaConfig.newConfigBuilder(apiKey).withUserProfileID("Some").build()
        val firstReporter = reporterFactory.getUnhandhedSituationReporter(firstConfig)
        val secondReporter = reporterFactory.getUnhandhedSituationReporter(secondConfig)

        assertThat(firstReporter)
            .isSameAs(secondReporter)
            .isSameAs(crashReporter)
        assertThat(crashReporterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(crashReporterMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(crashReporterFieldsProviderMockedConstructionRule.constructionMock.constructed().first())

        assertThat(crashReporterFieldsProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(crashReporterFieldsProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(processConfiguration, errorEnvironment, reportsHandler, firstConfig)

        verify(crashReporter, never()).updateConfig(firstConfig)
        verify(crashReporter).updateConfig(secondConfig)
    }

    @Test
    fun getReportersFactory() {
        assertThat(reporterFactory.reporterFactory).isSameAs(reporterFactory)
    }

    private fun checkValidator(vararg apiKeys: String) {
        assertThat(throwIfFailedValidatorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(throwIfFailedValidatorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(reporterApiKeyIsUsedValidator)
        assertThat(reporterApiKeyUsedValidatorRule.constructionMock.constructed()).hasSize(1)
        val arguments = reporterApiKeyUsedValidatorRule.argumentInterceptor.flatArguments()
        assertThat(arguments).hasSize(1)
        assertThat((arguments.first() as Map<String, Any?>).keys)
            .containsExactly(*apiKeys)
    }
}
