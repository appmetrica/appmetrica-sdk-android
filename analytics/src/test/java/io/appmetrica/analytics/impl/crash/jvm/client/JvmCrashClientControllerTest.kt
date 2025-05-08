package io.appmetrica.analytics.impl.crash.jvm.client

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.crash.ApplicationCrashProcessorCreator
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class JvmCrashClientControllerTest : CommonTest() {

    private val context: Context = mock()
    private val config: AppMetricaConfig = mock()
    private val reporterFactoryProvider: IReporterFactoryProvider = mock()
    private val applicationCrashProcessor: ICrashProcessor = mock()
    private val firstTechnicalCrashProcessor: ICrashProcessor = mock()
    private val secondTechnicalCrashProcessor: ICrashProcessor = mock()

    @get:Rule
    val applicationCrashProcessorCreatorMockedConstructionRule = constructionRule<ApplicationCrashProcessorCreator> {
        on { createCrashProcessor(context, config, reporterFactoryProvider) } doReturn applicationCrashProcessor
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
