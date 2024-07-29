package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Handler
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreapi.internal.clientcomponents.ClientComponentsInitializer
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.clientcomponents.ClientComponentsInitializerProvider
import io.appmetrica.analytics.impl.crash.jvm.client.JvmCrashClientController
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider
import io.appmetrica.analytics.logger.common.BaseReleaseLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaCoreTests : CommonTest() {

    private val apiKey = UUID.randomUUID().toString();
    private val context: Context = mock()
    private val handler: Handler = mock()

    private val defaultExecutor: IHandlerExecutor = mock {
        on { handler } doReturn handler
    }

    private val executorProvider: ClientExecutorProvider = mock {
        on { defaultExecutor } doReturn defaultExecutor
    }

    @get:Rule
    val clientTimeTrackerMockedConstructionRule = constructionRule<ClientTimeTracker>()
    private val clientTimeTracker: ClientTimeTracker by clientTimeTrackerMockedConstructionRule

    @get:Rule
    val appOpenWatcherMockedConstructionRule = constructionRule<AppOpenWatcher>()
    private val appOpenWatcher: AppOpenWatcher by appOpenWatcherMockedConstructionRule

    @get:Rule
    val jvmCrashClientControllerMockedConstructionRule = constructionRule<JvmCrashClientController>()
    private val jvmCrashClientController: JvmCrashClientController by jvmCrashClientControllerMockedConstructionRule

    @get:Rule
    val baseReleaseLoggerMockedStaticRule = staticRule<BaseReleaseLogger>()

    private val clientComponentsInitializer: ClientComponentsInitializer = mock()

    @get:Rule
    val clientComponentsInitializerProviderMockedConstructionRule =
        constructionRule<ClientComponentsInitializerProvider> {
            on { getClientComponentsInitializer() } doReturn clientComponentsInitializer
        }

    @get:Rule
    val sdkUtilsMockedStaticRule = staticRule<SdkUtils>()

    private val runnableCaptor = argumentCaptor<Runnable>()

    private val reporterFactoryProvider: IReporterFactoryProvider = mock()

    private val core: AppMetricaCore by setUp { AppMetricaCore(context, executorProvider) }

    @Test
    fun initLogger() {
        baseReleaseLoggerMockedStaticRule.staticMock.verify {
            BaseReleaseLogger.init(context)
        }
    }

    @Test
    fun logSdkInfo() {
        verify(defaultExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        sdkUtilsMockedStaticRule.staticMock.verify {
            SdkUtils.logSdkInfo()
        }
    }

    @Test
    fun trackCoreCreation() {
        verify(clientTimeTracker).trackCoreCreation()
    }

    @Test
    fun onCreateClientComponentsInitializer() {
        verify(clientComponentsInitializer).onCreate()
    }

    @Test
    fun `activate should setUp crash collecting for null config`() {
        core.activate(null, reporterFactoryProvider)
        verify(jvmCrashClientController).setUpCrashHandler()
        verify(jvmCrashClientController).registerTechnicalCrashConsumers(context, reporterFactoryProvider)
        verifyNoMoreInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate setUp crash collecting twice`() {
        core.activate(null, reporterFactoryProvider)
        clearInvocations(jvmCrashClientController)
        core.activate(null, reporterFactoryProvider)
        inOrder(jvmCrashClientController) {
            verify(jvmCrashClientController).setUpCrashHandler()
            verify(jvmCrashClientController)
                .registerTechnicalCrashConsumers(context, reporterFactoryProvider)
        }
    }

    @Test
    fun `activate does not setUp crash collecting if disabled`() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).withCrashReporting(false).build()
        core.activate(config, reporterFactoryProvider)
        verify(jvmCrashClientController).clearCrashConsumers()
        verifyNoMoreInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate setUp crash collecting if enabled`() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).withCrashReporting(true).build()
        core.activate(config, reporterFactoryProvider)
        inOrder(jvmCrashClientController) {
            verify(jvmCrashClientController).setUpCrashHandler()
            verify(jvmCrashClientController).registerTechnicalCrashConsumers(context, reporterFactoryProvider)
            verify(jvmCrashClientController).registerApplicationCrashConsumer(context, reporterFactoryProvider, config)
        }
    }

    @Test
    fun `activate twice setUp crash collecting if enabled`() {
        val config = AppMetricaConfig.newConfigBuilder(apiKey).withCrashReporting(true).build()
        core.activate(config, reporterFactoryProvider)
        clearInvocations(jvmCrashClientController)
        core.activate(config, reporterFactoryProvider)
        verifyNoInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate with config after activate without config setUp crash collecting`() {
        core.activate(null, reporterFactoryProvider)
        clearInvocations(jvmCrashClientController)
        val config = AppMetricaConfig.newConfigBuilder(apiKey).withCrashReporting(true).build()
        core.activate(config, reporterFactoryProvider)
        inOrder(jvmCrashClientController) {
            verify(jvmCrashClientController).setUpCrashHandler()
            verify(jvmCrashClientController).registerTechnicalCrashConsumers(context, reporterFactoryProvider)
            verify(jvmCrashClientController).registerApplicationCrashConsumer(context, reporterFactoryProvider, config)
        }
    }

    @Test
    fun `activate with config after activation without config disable crash collecting if disabled in config`() {
        core.activate(null, reporterFactoryProvider)
        clearInvocations(jvmCrashClientController)
        core.activate(
            AppMetricaConfig.newConfigBuilder(apiKey).withCrashReporting(false).build(),
            reporterFactoryProvider
        )
        verify(jvmCrashClientController).clearCrashConsumers()
        verifyNoMoreInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate without config after activation with config with crashes does not setUp crash collecting`() {
        core.activate(
            AppMetricaConfig.newConfigBuilder(apiKey).withCrashReporting(true).build(),
            reporterFactoryProvider
        )
        clearInvocations(jvmCrashClientController)
        core.activate(null, reporterFactoryProvider)
        verifyNoInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate without config after activation with config without crashes does not setUp crash collecting`() {
        core.activate(
            AppMetricaConfig.newConfigBuilder(apiKey).withCrashReporting(true).build(),
            reporterFactoryProvider
        )
        clearInvocations(jvmCrashClientController)
        core.activate(null, reporterFactoryProvider)
        verify(jvmCrashClientController)
    }

    @Test
    fun `activate without config setUp app open tracking`() {
        core.activate(null, reporterFactoryProvider)
        verify(appOpenWatcher).startWatching()
        verifyNoMoreInteractions(appOpenWatcher)
    }

    @Test
    fun `activate with config with app open tracking`() {
        core.activate(
            AppMetricaConfig.newConfigBuilder(apiKey).withAppOpenTrackingEnabled(true).build(),
            reporterFactoryProvider
        )
        verify(appOpenWatcher).startWatching()
        verifyNoMoreInteractions(appOpenWatcher)
    }

    @Test
    fun `activate with config without app open tracking`() {
        core.activate(
            AppMetricaConfig.newConfigBuilder(apiKey).withAppOpenTrackingEnabled(false).build(),
            reporterFactoryProvider
        )
        verify(appOpenWatcher).stopWatching()
        verifyNoMoreInteractions(appOpenWatcher)
    }

    @Test
    fun `activate with config twice`() {
        core.activate(
            AppMetricaConfig.newConfigBuilder(apiKey).withAppOpenTrackingEnabled(true).build(),
            reporterFactoryProvider
        )
        clearInvocations(appOpenWatcher)
        core.activate(
            AppMetricaConfig.newConfigBuilder(apiKey).withCrashReporting(false).build(),
            reporterFactoryProvider
        )
        verifyNoInteractions(appOpenWatcher)
    }

    @Test
    fun `activate without config after activation with config with app open tracking`() {
        core.activate(
            AppMetricaConfig.newConfigBuilder(apiKey).withAppOpenTrackingEnabled(true).build(),
            reporterFactoryProvider
        )
        clearInvocations(appOpenWatcher)
        core.activate(null, reporterFactoryProvider)
        verifyNoInteractions(appOpenWatcher)
    }
}
