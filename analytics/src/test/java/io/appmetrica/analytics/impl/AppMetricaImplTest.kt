package io.appmetrica.analytics.impl

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Handler
import io.appmetrica.analytics.AdvIdentifiersResult
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.DeferredDeeplinkListener
import io.appmetrica.analytics.DeferredDeeplinkParametersListener
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.StartupParamsCallback
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.crash.jvm.client.JvmCrashClientController
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.impl.modules.ModulesSeeker
import io.appmetrica.analytics.impl.modules.client.ClientModulesController
import io.appmetrica.analytics.impl.modules.client.context.ClientContextImpl
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaImplTest : CommonTest() {

    private val context: Context = mock()

    private val defaultHandler: Handler = mock()
    private val defaultExecutor: ICommonExecutor = mock()
    private val appOpenWatcher: AppOpenWatcher = mock()
    private val jvmCrashClientController: JvmCrashClientController = mock()

    private val appmetricaCore: IAppMetricaCore = mock {
        on { defaultHandler } doReturn defaultHandler
        on { defaultExecutor } doReturn defaultExecutor
        on { appOpenWatcher } doReturn appOpenWatcher
        on { jvmCrashClientController } doReturn jvmCrashClientController
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()
    private val clientPreferences: PreferencesClientDbStorage by setUp {
        ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context)
    }
    private val modulesController: ClientModulesController by setUp {
        ClientServiceLocator.getInstance().modulesController
    }

    private val wasAppEnvironmentCleared = false
    private val defaultOneShotMetricaConfig: DefaultOneShotMetricaConfig by setUp {
        whenever(ClientServiceLocator.getInstance().defaultOneShotConfig.wasAppEnvironmentCleared())
            .thenReturn(wasAppEnvironmentCleared)
        ClientServiceLocator.getInstance().defaultOneShotConfig
    }

    private val sessionsTrackingManager: SessionsTrackingManager by setUp {
        ClientServiceLocator.getInstance().sessionsTrackingManager
    }

    @get:Rule
    val modulesSeekerConstructionRule = constructionRule<ModulesSeeker>()
    private val modulesSeeker: ModulesSeeker by modulesSeekerConstructionRule

    @get:Rule
    val clientContextImplMockedConstructionRule = constructionRule<ClientContextImpl>()
    private val clientContextImpl: ClientContextImpl by clientContextImplMockedConstructionRule

    private val anonymousConfig: AppMetricaConfig =
        AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build()

    private val appMetricaLibraryAdapterConfig: AppMetricaLibraryAdapterConfig = mock()

    @get:Rule
    val anonymousConfigProviderMockedConstructionRule =
        constructionRule<AppMetricaConfigForAnonymousActivationProvider> {
            on { getConfig(appMetricaLibraryAdapterConfig) } doReturn anonymousConfig
        }

    private val apiKey = UUID.randomUUID().toString()

    private val config = AppMetricaConfig.newConfigBuilder(apiKey)
        .withLocationTracking(true)
        .withAdvIdentifiersTracking(true)
        .withDataSendingEnabled(true)
        .withSessionsAutoTrackingEnabled(true)
        .withLogs()
        .build()

    private val configWithDisabled = AppMetricaConfig.newConfigBuilder(apiKey)
        .withAnrMonitoring(false)
        .withCrashReporting(false)
        .withDataSendingEnabled(false)
        .withLocationTracking(false)
        .withAdvIdentifiersTracking(false)
        .withSessionsAutoTrackingEnabled(false)
        .build()

    private val publicLogger: PublicLogger = mock()
    private val publicOrAnonymousLogger: PublicLogger = mock()

    @get:Rule
    val loggerStorageMockedStaticRule = staticRule<LoggerStorage> {
        on { LoggerStorage.getOrCreateMainPublicLogger(apiKey) } doReturn publicLogger
        on { LoggerStorage.getMainPublicOrAnonymousLogger() } doReturn publicOrAnonymousLogger
    }

    private val dataResultReceiver: DataResultReceiver = mock()
    private val processConfiguration: ProcessConfiguration = mock()

    private val reportsHandler: ReportsHandler = mock()
    private val startupHelper: StartupHelper = mock()
    private val referrerHelper: ReferrerHelper = mock()

    private val deeplinkConsumer: DeeplinkConsumer = mock()

    private val reporterFromConsumerProvider: MainReporter = mock()

    @get:Rule
    val mainReporterApiConsumerProviderMockedConstructionRule = constructionRule<MainReporterApiConsumerProvider> {
        on { deeplinkConsumer } doReturn deeplinkConsumer
        on { mainReporter } doReturn reporterFromConsumerProvider
    }

    private val mainReporter: MainReporter = mock()
    private val anonymousMainReporter: MainReporter = mock()

    private val reporterFactory: ReporterFactory = mock {
        on { buildOrUpdateMainReporter(config, publicLogger, wasAppEnvironmentCleared) } doReturn mainReporter
        on { buildOrUpdateMainReporter(configWithDisabled, publicLogger, wasAppEnvironmentCleared) } doReturn mainReporter
        on {
            buildOrUpdateAnonymousMainReporter(anonymousConfig, publicOrAnonymousLogger, wasAppEnvironmentCleared)
        } doReturn anonymousMainReporter
    }

    @get:Rule
    val fieldsProviderMockedConstructionRule = constructionRule<AppMetricaImplFieldsProvider> {
        on { createDataResultReceiver(eq(defaultHandler), any()) } doReturn dataResultReceiver
        on { createProcessConfiguration(context, dataResultReceiver) } doReturn processConfiguration
        on { createReportsHandler(processConfiguration, context, defaultExecutor) } doReturn reportsHandler
        on { createStartupHelper(context, reportsHandler, clientPreferences, defaultHandler) } doReturn startupHelper
        on { createReferrerHelper(reportsHandler, clientPreferences, defaultHandler) } doReturn referrerHelper
        on {
            createReporterFactory(context, processConfiguration, reportsHandler, defaultHandler, startupHelper)
        } doReturn reporterFactory
    }

    private val impl: AppMetricaImpl by setUp {
        AppMetricaImpl(context, appmetricaCore)
    }

    @Test
    fun `constructor modules registration`() {
        inOrder(modulesSeeker, modulesController) {
            verify(modulesSeeker).discoverClientModules()
            verify(modulesController).initClientSide(clientContextImpl)
        }
    }

    @Test
    fun `constructor set reportsHandler to defaultOneShotMetricaConfig`() {
        verify(defaultOneShotMetricaConfig).setReportsHandler(reportsHandler)
    }

    @Test
    fun `constructor set startupParamsProvider to reportsHandler`() {
        verify(reportsHandler).setStartupParamsProvider(startupHelper)
    }

    @Test
    fun `activate - request referrer`() {
        impl.activate(config)
        verify(referrerHelper).maybeRequestReferrer()
    }

    @Test
    fun `activate twice - request referrer`() {
        impl.activate(config)
        clearInvocations(referrerHelper)
        verifyNoInteractions(referrerHelper)
    }

    @Test
    fun `activate anonymously - request referrer`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(referrerHelper).maybeRequestReferrer()
    }

    @Test
    fun `activate anonymously twice - request referrer`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(referrerHelper)
        verifyNoInteractions(referrerHelper)
    }

    @Test
    fun `activate after activate anonymously - request referrer`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(referrerHelper)
        impl.activate(config)
        verifyNoInteractions(referrerHelper)
    }

    @Test
    fun `activate anonymously after activate - request referrer`() {
        impl.activate(config)
        clearInvocations(referrerHelper)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(referrerHelper)
    }

    @Test
    fun `activate - setup startup helper`() {
        impl.activate(config)
        verify(startupHelper).setPublicLogger(publicLogger)
        verify(startupHelper).setCustomHosts(null)
        verify(startupHelper).clids = null
        verify(startupHelper).setDistributionReferrer(null)
        verify(startupHelper).sendStartupIfNeeded()
        verifyNoMoreInteractions(startupHelper)
    }

    @Test
    fun `activate twice - setup startup helper`() {
        impl.activate(config)
        clearInvocations(startupHelper)
        impl.activate(config)
        verifyNoInteractions(startupHelper)
    }

    @Test
    fun `activate anonymously - setup startup helper`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(startupHelper).setPublicLogger(publicOrAnonymousLogger)
        verify(startupHelper).setCustomHosts(null)
        verify(startupHelper).clids = null
        verify(startupHelper).setDistributionReferrer(null)
        verify(startupHelper).sendStartupIfNeeded()
        verifyNoMoreInteractions(startupHelper)
    }

    @Test
    fun `activate anonymously twice - setup startup helper`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(startupHelper)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(startupHelper)
    }

    @Test
    fun `activate anonymously after activate - setup startup helper`() {
        impl.activate(config)
        clearInvocations(startupHelper)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(startupHelper)
    }

    @Test
    fun `activate after activate anonymously - setup startup helper`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(startupHelper)
        impl.activate(config)
        verify(startupHelper).setPublicLogger(publicLogger)
        verify(startupHelper).setCustomHosts(null)
        verify(startupHelper).clids = null
        verify(startupHelper).setDistributionReferrer(null)
        verify(startupHelper).sendStartupIfNeeded()
        verifyNoMoreInteractions(startupHelper)
    }

    @Test
    fun `activate - update process configuration`() {
        impl.activate(config)
        verify(processConfiguration).update(config)
    }

    @Test
    fun `activate twice - update process configuration`() {
        impl.activate(config)
        clearInvocations(processConfiguration)
        impl.activate(config)
        verifyNoInteractions(processConfiguration)
    }

    @Test
    fun `activate anonymously - update process configuration`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(processConfiguration).update(anonymousConfig)
    }

    @Test
    fun `activate anonymously twice - update process configuration`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(processConfiguration)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(processConfiguration)
    }

    @Test
    fun `activate anonymously after activate - update process configuration`() {
        impl.activate(config)
        clearInvocations(processConfiguration)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(processConfiguration)
    }

    @Test
    fun `activate after activate anonymously - update process configuration`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(processConfiguration)
        impl.activate(config)
        verify(processConfiguration).update(config)
    }

    @Test
    fun `activate - setup logger`() {
        impl.activate(config)
        verify(publicLogger).setEnabled(true)
        verify(publicOrAnonymousLogger, never()).setEnabled(any())
    }

    @Test
    fun `activate - setup logger if disabled`() {
        impl.activate(configWithDisabled)
        verify(publicLogger).setEnabled(false)
        verify(publicOrAnonymousLogger, never()).setEnabled(false)
    }

    @Test
    fun `activate anonymously - setup logger`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(publicLogger)
        verify(publicOrAnonymousLogger).setEnabled(false)
        verify(publicLogger, never()).setEnabled(any())
    }

    @Test
    fun `activate twice - setup logger`() {
        impl.activate(config)
        clearInvocations(publicLogger)
        impl.activate(config)
        verify(publicLogger, never()).setEnabled(any())
        verify(publicOrAnonymousLogger, never()).setEnabled(any())
    }

    @Test
    fun `activate anonymously twice - setup logger`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(publicOrAnonymousLogger)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(publicLogger, never()).setEnabled(any())
        verify(publicOrAnonymousLogger, never()).setEnabled(any())
    }

    @Test
    fun `activate after activate anonymously - setup logger`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(publicLogger, publicOrAnonymousLogger)
        impl.activate(config)
        verify(publicLogger).setEnabled(true)
        verify(publicOrAnonymousLogger, never()).setEnabled(any())
    }

    @Test
    fun `activate after activate anonymously - setup logger if disabled`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(publicLogger, publicOrAnonymousLogger)
        impl.activate(config)
        verify(publicLogger).setEnabled(true)
        verify(publicOrAnonymousLogger, never()).setEnabled(any())
    }

    @Test
    fun `activate anonymously after activate - setup logger`() {
        impl.activate(config)
        clearInvocations(publicLogger, publicOrAnonymousLogger)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(publicLogger, never()).setEnabled(any())
        verify(publicOrAnonymousLogger, never()).setEnabled(any())
    }

    @Test
    fun `activate - save config`() {
        impl.activate(config)
        verify(clientPreferences).saveAppMetricaConfig(config)
    }

    @Test
    fun `activate twice - save config`() {
        impl.activate(config)
        clearInvocations(clientPreferences)
        verify(clientPreferences, never()).saveAppMetricaConfig(any())
    }

    @Test
    fun `activate anonymously - save config`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(clientPreferences, never()).saveAppMetricaConfig(any())
    }

    @Test
    fun `activate anonymously twice - save config`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(clientPreferences, never()).saveAppMetricaConfig(any())
    }

    @Test
    fun `activate after activate anonymously - save config`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(clientPreferences)
        impl.activate(config)
        verify(clientPreferences).saveAppMetricaConfig(config)
    }

    @Test
    fun `activate anonymously after activate - save config`() {
        impl.activate(config)
        clearInvocations(clientPreferences)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(clientPreferences, never()).saveAppMetricaConfig(any())
    }

    @Test
    fun `activate - jvm crash controller`() {
        impl.activate(config)
        verify(jvmCrashClientController).registerApplicationCrashConsumer(context, impl, config)
        verify(jvmCrashClientController).registerTechnicalCrashConsumers(context, impl)
        verifyNoMoreInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate with disabled = jvm crash controller`() {
        impl.activate(configWithDisabled)
        verify(jvmCrashClientController).clearCrashConsumers()
        verifyNoMoreInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate twice - jvm crash controller`() {
        impl.activate(config)
        clearInvocations(jvmCrashClientController)
        verifyNoInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate anonymously - jvm crash controller`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(jvmCrashClientController).registerApplicationCrashConsumer(context, impl, anonymousConfig)
        verify(jvmCrashClientController).registerTechnicalCrashConsumers(context, impl)
        verifyNoMoreInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate anonymously twice - jvm crash controller`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(jvmCrashClientController)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate after activate anonymously`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(jvmCrashClientController)
        impl.activate(config)
        verify(jvmCrashClientController).registerApplicationCrashConsumer(context, impl, config)
        verify(jvmCrashClientController).registerTechnicalCrashConsumers(context, impl)
        verifyNoMoreInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate after activate anonymously - jvm crash controller if disabled`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(jvmCrashClientController)
        impl.activate(configWithDisabled)
        verify(jvmCrashClientController).clearCrashConsumers()
        verifyNoMoreInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate anonymously after activate - jvm crash controller`() {
        impl.activate(config)
        clearInvocations(jvmCrashClientController)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(jvmCrashClientController)
    }

    @Test
    fun `activate anonymously - session auto tracking`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(sessionsTrackingManager).startWatchingIfNotYet()
    }

    @Test
    fun `activate anonymously twice - session auto tracking`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(sessionsTrackingManager)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(sessionsTrackingManager)
    }

    @Test
    fun `activate - session auto tracking enabled`() {
        impl.activate(config)
        verify(sessionsTrackingManager).startWatchingIfNotYet()
    }

    @Test
    fun `activate - session auto tracking disabled`() {
        impl.activate(configWithDisabled)
        verify(sessionsTrackingManager).stopWatchingIfHasAlreadyBeenStarted()
    }

    @Test
    fun `activate twice - session auto tracking`() {
        impl.activate(config)
        clearInvocations(sessionsTrackingManager)
        impl.activate(config)
        verifyNoInteractions(sessionsTrackingManager)
    }

    @Test
    fun `activate anonymously after activate - session auto tracking`() {
        impl.activate(config)
        clearInvocations(sessionsTrackingManager)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verifyNoInteractions(sessionsTrackingManager)
    }

    @Test
    fun `activate after activate anonymously - session auto tracking enabled`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(sessionsTrackingManager)
        impl.activate(config)
        verify(sessionsTrackingManager).startWatchingIfNotYet()
    }

    @Test
    fun `activate after activate anonymously - session auto tracking disabled`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(sessionsTrackingManager)
        impl.activate(configWithDisabled)
        verify(sessionsTrackingManager).stopWatchingIfHasAlreadyBeenStarted()
    }

    @Test
    fun `activate - update pre activation settings`() {
        impl.activate(config)
        verify(reportsHandler).updatePreActivationConfig(true, true, true)
    }

    @Test
    fun `activate - update pre activation settings if disabled`() {
        impl.activate(configWithDisabled)
        verify(reportsHandler).updatePreActivationConfig(false, false, false)
    }

    @Test
    fun `activate twice - update pre activation settings`() {
        impl.activate(config)
        clearInvocations(reportsHandler)
        impl.activate(config)
        verify(reportsHandler, never()).updatePreActivationConfig(any(), any(), any())
    }

    @Test
    fun `activate anonymously - update pre activate settings`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(reportsHandler).updatePreActivationConfig(null, null, null)
    }

    @Test
    fun `activate anonymously twice - update pre activation settings`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(reportsHandler)
        verify(reportsHandler, never()).updatePreActivationConfig(any(), any(), any())
    }

    @Test
    fun `activate after activate anonymously - update pre activation settings`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(reportsHandler)
        impl.activate(config)
        verify(reportsHandler).updatePreActivationConfig(true, true, true)
    }

    @Test
    fun `activate anonymously after activate - update pre activation settings`() {
        impl.activate(config)
        clearInvocations(reportsHandler)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(reportsHandler, never()).updatePreActivationConfig(any(), any(), any())
    }

    @Test
    fun `activate - build main reporter`() {
        impl.activate(config)
        verify(reporterFactory).buildOrUpdateMainReporter(config, publicLogger, wasAppEnvironmentCleared)
        verify(appOpenWatcher).setDeeplinkConsumer(deeplinkConsumer)
        assertThat(mainReporterApiConsumerProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(mainReporter)
        verify(sessionsTrackingManager).setReporter(mainReporter)
        verifyNoMoreInteractions(reporterFactory)
    }

    @Test
    fun `activate twice - build main reporter`() {
        impl.activate(config)
        clearInvocations(reporterFactory, appOpenWatcher, sessionsTrackingManager)
        impl.activate(config)
        verify(reporterFactory).buildOrUpdateMainReporter(config, publicLogger, wasAppEnvironmentCleared)
        verifyNoInteractions(appOpenWatcher, sessionsTrackingManager)
    }

    @Test
    fun `activate anonymously - build main reporter`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(reporterFactory)
            .buildOrUpdateAnonymousMainReporter(anonymousConfig, publicOrAnonymousLogger, wasAppEnvironmentCleared)
        verify(appOpenWatcher).setDeeplinkConsumer(deeplinkConsumer)
        assertThat(mainReporterApiConsumerProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(anonymousMainReporter)
        verify(sessionsTrackingManager).setReporter(anonymousMainReporter)
        verifyNoMoreInteractions(reporterFactory)
    }

    @Test
    fun `activate anonymously twice - build main reporter`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(reporterFactory, appOpenWatcher, sessionsTrackingManager)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(reporterFactory)
            .buildOrUpdateAnonymousMainReporter(anonymousConfig, publicOrAnonymousLogger, wasAppEnvironmentCleared)
        verifyNoInteractions(appOpenWatcher, sessionsTrackingManager)
    }

    @Test
    fun `activate after activate anonymously - build main reporter`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        clearInvocations(reporterFactory, appOpenWatcher, sessionsTrackingManager)
        impl.activate(config)
        verify(reporterFactory).buildOrUpdateMainReporter(config, publicLogger, wasAppEnvironmentCleared)
        verifyNoInteractions(appOpenWatcher)
        verify(sessionsTrackingManager, never()).setReporter(any())
    }

    @Test
    fun `activate anonymously after activate - build main reporter`() {
        impl.activate(config)
        clearInvocations(reporterFactory, appOpenWatcher, sessionsTrackingManager)
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        verify(reporterFactory)
            .buildOrUpdateAnonymousMainReporter(anonymousConfig, publicOrAnonymousLogger, wasAppEnvironmentCleared)
        verifyNoInteractions(appOpenWatcher, sessionsTrackingManager)
    }

    @Test
    fun `getMainReporterApiConsumerProvider before activate`() {
        assertThat(impl.mainReporterApiConsumerProvider).isNull()
    }

    @Test
    fun `getMainReporterApiConsumerProvider after activate`() {
        impl.activate(config)
        assertThat(impl.mainReporterApiConsumerProvider)
            .isEqualTo(mainReporterApiConsumerProviderMockedConstructionRule.constructionMock.constructed().first())
        assertThat(mainReporterApiConsumerProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(mainReporter)
    }

    @Test
    fun `getMainReporterApiConsumerProvider after activate anonymously`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        assertThat(impl.mainReporterApiConsumerProvider)
            .isEqualTo(mainReporterApiConsumerProviderMockedConstructionRule.constructionMock.constructed().first())
        assertThat(mainReporterApiConsumerProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(anonymousMainReporter)
    }

    @Test
    fun onStartupResult() {
        val bundle: Bundle = mock()
        val resultCode = 7
        impl.onReceiveResult(resultCode, bundle)
        verify(startupHelper).processResultFromResultReceiver(bundle)
    }

    @Test
    fun requestDeferredDeeplinkParameters() {
        val listener: DeferredDeeplinkParametersListener = mock()
        impl.requestDeferredDeeplinkParameters(listener)
        verify(referrerHelper).requestDeferredDeeplinkParameters(listener)
    }

    @Test
    fun requestDeferredDeeplink() {
        val listener: DeferredDeeplinkListener = mock()
        impl.requestDeferredDeeplink(listener)
        verify(referrerHelper).requestDeferredDeeplink(listener)
    }

    @Test
    fun activateReporter() {
        val reporterConfig: ReporterConfig = mock()
        impl.activateReporter(reporterConfig)
        verify(reporterFactory).activateReporter(reporterConfig)
    }

    @Test
    fun getReporter() {
        val reporter: IReporterExtended = mock()
        val reporterConfig: ReporterConfig = mock()
        whenever(reporterFactory.getOrCreateReporter(reporterConfig)).thenReturn(reporter)
        assertThat(impl.getReporter(reporterConfig)).isEqualTo(reporter)
    }

    @Test
    fun getDeviceId() {
        val deviceId = "device id"
        whenever(startupHelper.deviceId).thenReturn(deviceId)
        assertThat(impl.deviceId).isEqualTo(deviceId)
    }

    @Test
    fun getCachedAdvIdentifiers() {
        val cachedAdvIdentifierResult: AdvIdentifiersResult = mock()
        whenever(startupHelper.cachedAdvIdentifiers).thenReturn(cachedAdvIdentifierResult)
        assertThat(impl.cachedAdvIdentifiers).isEqualTo(cachedAdvIdentifierResult)
    }

    @Test
    fun getFeatures() {
        val features: FeaturesResult = mock()
        whenever(startupHelper.features).thenReturn(features)
        assertThat(impl.features).isEqualTo(features)
    }

    @Test
    fun getClids() {
        val clids = mapOf("a" to "b")
        whenever(startupHelper.clids).thenReturn(clids)
        assertThat(impl.clids).isEqualTo(clids)
    }

    @Test
    fun requestStartupParams() {
        val callback: StartupParamsCallback = mock()
        val params = listOf("first", "second")
        val clientClids = mapOf("first" to "second")
        whenever(processConfiguration.clientClids).thenReturn(clientClids)
        impl.requestStartupParams(callback, params)
        verify(startupHelper).requestStartupParams(callback, params, clientClids)
    }

    @Test
    fun `setLocation after activate`() {
        val location: Location = mock()
        impl.activate(config)
        impl.setLocation(location)
        verify(reporterFromConsumerProvider).setLocation(location)
    }

    @Test
    fun `setLocation after activate anonymously`() {
        val location: Location = mock()
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.setLocation(location)
        verify(reporterFromConsumerProvider).setLocation(location)
    }

    @Test
    fun `setLocationTracking after activation`() {
        impl.activate(config)
        impl.setLocationTracking(true)
        verify(reporterFromConsumerProvider).setLocationTracking(true)
    }

    @Test
    fun `setLocationTracking after activate anonymously`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.setLocationTracking(true)
        verify(reporterFromConsumerProvider).setLocationTracking(true)
    }

    @Test
    fun `setAdvIdentifiersTracking after activation`() {
        impl.activate(config)
        impl.setAdvIdentifiersTracking(true)
        verify(reporterFromConsumerProvider).setAdvIdentifiersTracking(true)
    }

    @Test
    fun `setAdvIdentifiersTracking after anonymous activation`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.setAdvIdentifiersTracking(true)
        verify(reporterFromConsumerProvider).setAdvIdentifiersTracking(true)
    }

    @Test
    fun `setDataSendingEnabled after activate`() {
        impl.activate(config)
        impl.setDataSendingEnabled(true)
        verify(reporterFromConsumerProvider).setDataSendingEnabled(true)
    }

    @Test
    fun `setDataSendingEnabled after activate anonymously`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.setDataSendingEnabled(true)
        verify(reporterFromConsumerProvider).setDataSendingEnabled(true)
    }

    @Test
    fun `putAppEnvironmentValue after activate`() {
        impl.activate(config)
        impl.putAppEnvironmentValue("key", "value")
        verify(reporterFromConsumerProvider).putAppEnvironmentValue("key", "value")
    }

    @Test
    fun `putAppEnvironmentValue after activate anonymously`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.putAppEnvironmentValue("key", "value")
        verify(reporterFromConsumerProvider).putAppEnvironmentValue("key", "value")
    }

    @Test
    fun `clearAppEnvironment after activate`() {
        impl.activate(config)
        impl.clearAppEnvironment()
        verify(reporterFromConsumerProvider).clearAppEnvironment()
    }

    @Test
    fun `clearAppEnvironment after activate anonymously`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.clearAppEnvironment()
        verify(reporterFromConsumerProvider).clearAppEnvironment()
    }

    @Test
    fun `putErrorEnvironmentValue after activate`() {
        impl.activate(config)
        impl.putErrorEnvironmentValue("key", "value")
        verify(reporterFromConsumerProvider).putErrorEnvironmentValue("key", "value")
    }

    @Test
    fun `putErrorEnvironmentValue after activate anonymously`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.putErrorEnvironmentValue("key", "value")
        verify(reporterFromConsumerProvider).putErrorEnvironmentValue("key", "value")
    }

    @Test
    fun `setUserProfileId after activate`() {
        impl.activate(config)
        impl.setUserProfileID("profileId")
        verify(reporterFromConsumerProvider).setUserProfileID("profileId")
    }

    @Test
    fun `setUserProfileId after activate anonymously`() {
        impl.activateAnonymously(appMetricaLibraryAdapterConfig)
        impl.setUserProfileID("profileId")
        verify(reporterFromConsumerProvider).setUserProfileID("profileId")
    }

    @Test
    fun getReporterFactory() {
        assertThat(impl.reporterFactory).isEqualTo(reporterFactory)
    }
}
