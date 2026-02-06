package io.appmetrica.analytics.testutils

import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver
import io.appmetrica.analytics.coreapi.internal.system.ActiveNetworkTypeProvider
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.ReferenceHolder
import io.appmetrica.analytics.coreutils.internal.services.FirstExecutionConditionServiceImpl
import io.appmetrica.analytics.coreutils.internal.services.WaitForActivationDelayBarrier
import io.appmetrica.analytics.impl.ApplicationStateProviderImpl
import io.appmetrica.analytics.impl.BatteryInfoProvider
import io.appmetrica.analytics.impl.ClidsInfoStorage
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.LifecycleDependentComponentManager
import io.appmetrica.analytics.impl.PreloadInfoStorage
import io.appmetrica.analytics.impl.SdkEnvironmentHolder
import io.appmetrica.analytics.impl.SelfDiagnosticReporterStorage
import io.appmetrica.analytics.impl.StartupStateHolder
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ServiceModuleReporterComponentLifecycleImpl
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashService
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.db.VitalDataProviderStorage
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.db.storage.ServiceStorageFactory
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter
import io.appmetrica.analytics.impl.id.AppSetIdGetter
import io.appmetrica.analytics.impl.location.LocationApi
import io.appmetrica.analytics.impl.modules.ModuleEntryPointsRegister
import io.appmetrica.analytics.impl.modules.ModuleEventHandlersHolder
import io.appmetrica.analytics.impl.modules.service.ServiceModulesController
import io.appmetrica.analytics.impl.network.http.SslSocketFactoryProviderImpl
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder
import io.appmetrica.analytics.impl.service.ServiceDataReporterHolder
import io.appmetrica.analytics.impl.servicecomponents.ServiceLifecycleTimeTracker
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider
import io.appmetrica.analytics.impl.utils.executors.ServiceExecutorProvider
import io.appmetrica.analytics.impl.utils.process.CurrentProcessDetector
import io.appmetrica.analytics.networktasks.internal.NetworkCore
import org.junit.rules.ExternalResource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class GlobalServiceLocatorRule : ExternalResource() {

    val contextRule = ContextRule()
    val context by contextRule::context
    val reportRunnableExecutor = MockProvider.mockedBlockingExecutorMock()
    val moduleExecutor = MockProvider.mockedBlockingExecutorMock()
    val networkTaskProcessorExecutor = MockProvider.mockedBlockingExecutorMock()
    val supportIOExecutor = MockProvider.mockedBlockingExecutorMock()
    val defaultExecutor = MockProvider.mockedBlockingExecutorMock()
    val uiExecutor = MockProvider.mockedBlockingExecutorMock()
    val metricaCoreExecutor = MockProvider.mockedBlockingExecutorMock()

    val serviceExecutorProviderMock = mock<ServiceExecutorProvider> {
        on { reportRunnableExecutor } doReturn reportRunnableExecutor
        on { networkTaskProcessorExecutor } doReturn networkTaskProcessorExecutor
        on { moduleExecutor } doReturn moduleExecutor
        on { supportIOExecutor } doReturn supportIOExecutor
        on { defaultExecutor } doReturn defaultExecutor
        on { uiExecutor } doReturn uiExecutor
        on { metricaCoreExecutor } doReturn metricaCoreExecutor
    }

    override fun before() {
        contextRule.before()
        val globalServiceLocator = mock<GlobalServiceLocator>()
        whenever(globalServiceLocator.serviceExecutorProvider).thenReturn(serviceExecutorProviderMock)
        whenever(globalServiceLocator.getServicePreferences()).thenReturn(mock<PreferencesServiceDbStorage>())
        val lifecycleDependentComponentManager = mock<LifecycleDependentComponentManager>()
        val applicationStateProvider = mock<ApplicationStateProviderImpl>()
        whenever(applicationStateProvider.currentState).thenReturn(ApplicationState.UNKNOWN)
        whenever(applicationStateProvider.registerStickyObserver(any<ApplicationStateObserver>())).thenReturn(
            ApplicationState.UNKNOWN
        )
        whenever(lifecycleDependentComponentManager.applicationStateProvider).thenReturn(applicationStateProvider)
        whenever(globalServiceLocator.applicationStateProvider).thenReturn(applicationStateProvider)
        whenever(globalServiceLocator.getLifecycleDependentComponentManager())
            .thenReturn(lifecycleDependentComponentManager)
        whenever(globalServiceLocator.getBatteryInfoProvider()).thenReturn(mock<BatteryInfoProvider>())
        whenever(globalServiceLocator.getClidsStorage()).thenReturn(mock<ClidsInfoStorage>())
        whenever(globalServiceLocator.context).thenReturn(contextRule.context)
        whenever(globalServiceLocator.getPreloadInfoStorage()).thenReturn(mock<PreloadInfoStorage>())
        whenever(globalServiceLocator.getReferrerHolder()).thenReturn(mock<ReferrerHolder>())
        whenever(globalServiceLocator.selfDiagnosticReporterStorage).thenReturn(mock<SelfDiagnosticReporterStorage>())
        whenever(globalServiceLocator.getDataSendingRestrictionController()).thenReturn(
            mock<DataSendingRestrictionControllerImpl>()
        )
        whenever(globalServiceLocator.getSslSocketFactoryProvider()).thenReturn(mock<SslSocketFactoryProviderImpl>())
        val vitalDataProviderStorage = mock<VitalDataProviderStorage>()
        whenever(vitalDataProviderStorage.commonDataProvider).thenReturn(mock<VitalCommonDataProvider>())
        whenever(vitalDataProviderStorage.commonDataProviderForMigration).thenReturn(mock<VitalCommonDataProvider>())
        whenever(vitalDataProviderStorage.getComponentDataProvider(any<ComponentId>())).thenReturn(
            mock<VitalComponentDataProvider>()
        )
        whenever(globalServiceLocator.getVitalDataProviderStorage()).thenReturn(vitalDataProviderStorage)
        whenever(globalServiceLocator.getModulesController()).thenReturn(mock<ServiceModulesController>())
        whenever(globalServiceLocator.getGeneralPermissionExtractor()).thenReturn(mock<PermissionExtractor>())
        val startupStateHolder = mock<StartupStateHolder>()
        whenever(startupStateHolder.getStartupState())
            .thenReturn(StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build())
        whenever(globalServiceLocator.startupStateHolder).thenReturn(startupStateHolder)
        val locationApi = mock<LocationApi>()
        whenever(globalServiceLocator.locationClientApi).thenReturn(locationApi)
        whenever(globalServiceLocator.locationServiceApi).thenReturn(locationApi)
        whenever(globalServiceLocator.serviceDataReporterHolder).thenReturn(mock<ServiceDataReporterHolder>())
        whenever(globalServiceLocator.moduleEventHandlersHolder).thenReturn(mock<ModuleEventHandlersHolder>())
        val telephonyDataProvider = mock<TelephonyDataProvider>()
        whenever(globalServiceLocator.getTelephonyDataProvider()).thenReturn(telephonyDataProvider)
        whenever(globalServiceLocator.moduleEntryPointsRegister).thenReturn(mock<ModuleEntryPointsRegister>())
        whenever(globalServiceLocator.getMultiProcessSafeUuidProvider())
            .thenReturn(mock<MultiProcessSafeUuidProvider>())
        whenever(globalServiceLocator.nativeCrashService).thenReturn(mock<NativeCrashService>())
        whenever(globalServiceLocator.getServicePreferences()).thenReturn(mock<PreferencesServiceDbStorage>())
        whenever(globalServiceLocator.networkCore).thenReturn(mock<NetworkCore>())
        whenever(globalServiceLocator.getSdkEnvironmentHolder()).thenReturn(mock<SdkEnvironmentHolder>())
        val appSetIdGetter = mock<AppSetIdGetter>()
        whenever(appSetIdGetter.getAppSetId()).thenReturn(AppSetId(UUID.randomUUID().toString(), AppSetIdScope.APP))
        val advertisingIdGetter = mock<AdvertisingIdGetter>()
        val platformIdentifiers = mock<PlatformIdentifiers>()
        whenever(platformIdentifiers.advIdentifiersProvider).thenReturn(advertisingIdGetter)
        whenever(platformIdentifiers.appSetIdProvider).thenReturn(appSetIdGetter)
        whenever(globalServiceLocator.getAppSetIdGetter()).thenReturn(appSetIdGetter)
        whenever(globalServiceLocator.getAdvertisingIdGetter()).thenReturn(advertisingIdGetter)
        whenever(globalServiceLocator.getPlatformIdentifiers()).thenReturn(platformIdentifiers)
        whenever(globalServiceLocator.activationBarrier).thenReturn(mock<WaitForActivationDelayBarrier>())
        whenever(globalServiceLocator.firstExecutionConditionService)
            .thenReturn(mock<FirstExecutionConditionServiceImpl>())
        whenever(globalServiceLocator.getExtraMetaInfoRetriever()).thenReturn(mock<ExtraMetaInfoRetriever>())
        whenever(globalServiceLocator.serviceLifecycleTimeTracker).thenReturn(mock<ServiceLifecycleTimeTracker>())
        whenever(globalServiceLocator.getCurrentProcessDetector()).thenReturn(mock<CurrentProcessDetector>())
        whenever(globalServiceLocator.referenceHolder).thenReturn(mock<ReferenceHolder>())
        whenever(globalServiceLocator.serviceModuleReporterComponentLifecycle)
            .thenReturn(mock<ServiceModuleReporterComponentLifecycleImpl>())
        whenever(globalServiceLocator.activeNetworkTypeProvider).thenReturn(mock<ActiveNetworkTypeProvider>())
        whenever(globalServiceLocator.getStorageFactory()).thenReturn(mock<ServiceStorageFactory>())
        GlobalServiceLocator.setInstance(globalServiceLocator)
    }

    override fun after() {
        GlobalServiceLocator.destroy()
        contextRule.after()
    }
}
