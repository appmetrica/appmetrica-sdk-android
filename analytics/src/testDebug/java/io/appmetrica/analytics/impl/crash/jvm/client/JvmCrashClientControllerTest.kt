package io.appmetrica.analytics.impl.crash.jvm.client

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.crash.ApplicationCrashProcessorCreator
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

internal class JvmCrashClientControllerTest : CommonTest() {

    private val context: Context = mock()
    private val apiKey = UUID.randomUUID().toString()
    private val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
    private val enabledConfig = AppMetricaConfig.newConfigBuilder(apiKey)
        .withCrashReporting(true)
        .build()
    private val disabledConfig = AppMetricaConfig.newConfigBuilder(apiKey)
        .withCrashReporting(false)
        .build()
    private val reporterFactoryProvider: IReporterFactoryProvider = mock()
    private val logger: PublicLogger = mock()
    private val applicationCrashProcessor: ICrashProcessor = mock()
    private val firstTechnicalCrashProcessor: ICrashProcessor = mock()
    private val secondTechnicalCrashProcessor: ICrashProcessor = mock()

    @get:Rule
    val loggerStorageRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getMainPublicOrAnonymousLogger() } doReturn logger
    }

    @get:Rule
    val applicationCrashProcessorCreatorMockedConstructionRule = constructionRule<ApplicationCrashProcessorCreator> {
        on { createCrashProcessor(context, config, reporterFactoryProvider) } doReturn applicationCrashProcessor
        on { createCrashProcessor(context, enabledConfig, reporterFactoryProvider) } doReturn applicationCrashProcessor
    }

    @get:Rule
    val appMetricaUncaughtExceptionHandlerMockedConstructionRule =
        constructionRule<AppMetricaUncaughtExceptionHandler>()
    private val appMetricaUncaughtExceptionHandler by appMetricaUncaughtExceptionHandlerMockedConstructionRule

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val crashProcessorCompositeMockedConstructionRule = constructionRule<CrashProcessorComposite>()
    val crashProcessorComposite: CrashProcessorComposite by crashProcessorCompositeMockedConstructionRule

    @get:Rule
    val crashProcessorInstallerRule = constructionRule<ThreadUncaughtExceptionHandlerInstaller>()
    private val crashProcessorInstaller: ThreadUncaughtExceptionHandlerInstaller by crashProcessorInstallerRule

    private val jvmCrashClientController: JvmCrashClientController by setUp { JvmCrashClientController() }

    @Before
    fun setUp() {
        whenever(
            ClientServiceLocator.getInstance().crashProcessorFactory.createCrashProcessors(
                context,
                reporterFactoryProvider
            )
        ).thenReturn(listOf(firstTechnicalCrashProcessor, secondTechnicalCrashProcessor))
    }

    @Test
    fun setUpCrashHandler() {
        jvmCrashClientController.setUpCrashHandler()

        verify(crashProcessorInstaller).install()
        assertThat(crashProcessorInstallerRule.constructionMock.constructed()).hasSize(1)
        assertThat(crashProcessorInstallerRule.argumentInterceptor.flatArguments())
            .containsExactly(appMetricaUncaughtExceptionHandler)
        assertThat(appMetricaUncaughtExceptionHandlerMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(crashProcessorComposite)

        clearInvocations(crashProcessorInstaller)
        jvmCrashClientController.setUpCrashHandler()
        verifyNoInteractions(crashProcessorInstaller)
        assertThat(crashProcessorInstallerRule.constructionMock.constructed()).hasSize(1)
    }

    @Test
    fun `activation with default crash reporting registers application before technical consumers`() {
        jvmCrashClientController.applyActivationConfig(context, reporterFactoryProvider, config)

        inOrder(crashProcessorComposite, logger) {
            verify(crashProcessorComposite).register(applicationCrashProcessor)
            verify(crashProcessorComposite)
                .register(listOf(firstTechnicalCrashProcessor, secondTechnicalCrashProcessor))
            verify(logger).info("Register application crash handler")
        }
        verifyNoMoreInteractions(crashProcessorComposite)
    }

    @Test
    fun `activation with crash reporting enabled registers crash consumers`() {
        jvmCrashClientController.applyActivationConfig(context, reporterFactoryProvider, enabledConfig)

        verify(crashProcessorComposite).register(applicationCrashProcessor)
        verify(crashProcessorComposite).register(listOf(firstTechnicalCrashProcessor, secondTechnicalCrashProcessor))
        verifyNoMoreInteractions(crashProcessorComposite)
    }

    @Test
    fun `activation with crash reporting disabled clears crash consumers`() {
        jvmCrashClientController.applyActivationConfig(context, reporterFactoryProvider, disabledConfig)

        inOrder(crashProcessorComposite, logger) {
            verify(crashProcessorComposite).clearAllCrashProcessors()
            verify(logger).info("Disable all crash handlers")
        }
        verifyNoMoreInteractions(crashProcessorComposite)
    }

    @Test
    fun `activation uses the current main logger after it changes`() {
        val mainLogger: PublicLogger = mock()
        jvmCrashClientController.applyActivationConfig(context, reporterFactoryProvider, config)

        loggerStorageRule.staticMock.on { LoggerStorage.getMainPublicOrAnonymousLogger() } doReturn mainLogger
        jvmCrashClientController.applyActivationConfig(context, reporterFactoryProvider, disabledConfig)

        verify(logger).info("Register application crash handler")
        verify(mainLogger).info("Disable all crash handlers")
        verifyNoMoreInteractions(logger, mainLogger)
    }

    @Test
    fun registerTechnicalCrashConsumers() {
        jvmCrashClientController.setUpCrashHandler()
        jvmCrashClientController.registerTechnicalCrashConsumers(context, reporterFactoryProvider)
        verify(crashProcessorComposite)
            .register(listOf(firstTechnicalCrashProcessor, secondTechnicalCrashProcessor))
    }

    @Test
    fun `registerTechnicalCrashConsumers twice`() {
        jvmCrashClientController.setUpCrashHandler()
        jvmCrashClientController.registerTechnicalCrashConsumers(context, reporterFactoryProvider)
        clearInvocations(crashProcessorComposite)
        jvmCrashClientController.registerTechnicalCrashConsumers(context, reporterFactoryProvider)
        verifyNoInteractions(crashProcessorComposite)
    }

    @Test
    fun registerApplicationCrashConsumer() {
        jvmCrashClientController.setUpCrashHandler()
        jvmCrashClientController.registerApplicationCrashConsumer(context, reporterFactoryProvider, config)
        verify(crashProcessorComposite).register(applicationCrashProcessor)
    }

    @Test
    fun `registerApplicationCrashConsumer twice`() {
        jvmCrashClientController.setUpCrashHandler()
        jvmCrashClientController.registerApplicationCrashConsumer(context, reporterFactoryProvider, config)
        clearInvocations(crashProcessorComposite)
        jvmCrashClientController.registerApplicationCrashConsumer(context, reporterFactoryProvider, config)
        verifyNoInteractions(crashProcessorComposite)
    }

    @Test
    fun clearCrashConsumers() {
        jvmCrashClientController.clearCrashConsumers()
        verify(crashProcessorComposite).clearAllCrashProcessors()
    }

    @Test
    fun `clearCrashConsumers and register again`() {
        jvmCrashClientController.setUpCrashHandler()
        jvmCrashClientController.registerTechnicalCrashConsumers(context, reporterFactoryProvider)
        jvmCrashClientController.registerApplicationCrashConsumer(context, reporterFactoryProvider, config)
        jvmCrashClientController.clearCrashConsumers()

        clearInvocations(crashProcessorComposite)

        jvmCrashClientController.registerTechnicalCrashConsumers(context, reporterFactoryProvider)
        jvmCrashClientController.registerApplicationCrashConsumer(context, reporterFactoryProvider, config)

        verify(crashProcessorComposite).register(listOf(firstTechnicalCrashProcessor, secondTechnicalCrashProcessor))
        verify(crashProcessorComposite).register(applicationCrashProcessor)
    }
}
