package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.SharedPreferences;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.FirstExecutionConditionService;
import io.appmetrica.analytics.coreutils.internal.services.WaitForActivationDelayBarrier;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashService;
import io.appmetrica.analytics.impl.db.VitalDataProviderStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.id.AppSetIdGetter;
import io.appmetrica.analytics.impl.location.LocationClientApi;
import io.appmetrica.analytics.impl.modules.ModuleEntryPointsRegister;
import io.appmetrica.analytics.impl.modules.ModuleEventHandlersHolder;
import io.appmetrica.analytics.impl.modules.service.ServiceModulesController;
import io.appmetrica.analytics.impl.network.http.BaseSslSocketFactoryProvider;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper;
import io.appmetrica.analytics.impl.service.ServiceDataReporterHolder;
import io.appmetrica.analytics.impl.servicecomponents.ServiceLifecycleTimeTracker;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider;
import io.appmetrica.analytics.impl.utils.DebugAssert;
import io.appmetrica.analytics.impl.utils.executors.ServiceExecutorProvider;
import io.appmetrica.analytics.modulesapi.internal.service.LocationServiceApi;
import io.appmetrica.analytics.networktasks.internal.NetworkCore;
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class GlobalServiceLocatorGettersTest extends CommonTest {

    interface ServiceExtractor<T> {
        T getService(GlobalServiceLocator globalServiceLocator);
    }

    private String mServiceDescription;
    private ServiceExtractor mServiceExtractor;

    public GlobalServiceLocatorGettersTest(String serviceDescription, ServiceExtractor serviceExtractor) {
        mServiceDescription = serviceDescription;
        mServiceExtractor = serviceExtractor;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}]{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {
                "Context",
                new ServiceExtractor<Context>() {
                    @Override
                    public Context getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getContext();
                    }
                }
            },
            {
                "ReferrerHolder",
                new ServiceExtractor<ReferrerHolder>() {
                    @Override
                    public ReferrerHolder getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getReferrerHolder();
                    }
                }
            },
            {
                "DataSendingRestrictionController",
                new ServiceExtractor<DataSendingRestrictionControllerImpl>() {
                    @Override
                    public DataSendingRestrictionControllerImpl getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getDataSendingRestrictionController();
                    }
                }
            },
            {
                "ServiceExecutorProvider",
                new ServiceExtractor<ServiceExecutorProvider>() {
                    @Override
                    public ServiceExecutorProvider getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getServiceExecutorProvider();
                    }
                }
            },
            {
                "BatteryInfoProvider",
                new ServiceExtractor<BatteryInfoProvider>() {
                    @Override
                    public BatteryInfoProvider getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getBatteryInfoProvider();
                    }
                }
            },
            {
                "LifecycleDependentComponentManager",
                new ServiceExtractor<LifecycleDependentComponentManager>() {
                    @Override
                    public LifecycleDependentComponentManager getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getLifecycleDependentComponentManager();
                    }
                }
            },
            {
                "ApplicationStateProvider",
                new ServiceExtractor<ApplicationStateProviderImpl>() {
                    @Override
                    public ApplicationStateProviderImpl getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getApplicationStateProvider();
                    }
                }
            },
            {
                "AdvertisingIdGetter",
                new ServiceExtractor<AdvertisingIdGetter>() {
                    @Override
                    public AdvertisingIdGetter getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getAdvertisingIdGetter();
                    }
                }
            },
            {
                "PreloadInfoStorage",
                new ServiceExtractor<PreloadInfoStorage>() {
                    @Override
                    public PreloadInfoStorage getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getPreloadInfoStorage();
                    }
                }
            },
            {
                "ClidsStorage",
                new ServiceExtractor<ClidsInfoStorage>() {
                    @Override
                    public ClidsInfoStorage getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getClidsStorage();
                    }
                }
            },
            {
                "ServicePreferences",
                new ServiceExtractor<PreferencesServiceDbStorage>() {
                    @Override
                    public PreferencesServiceDbStorage getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getServicePreferences();
                    }
                }
            },
            {
                "VitalDataProviderStorage",
                new ServiceExtractor<VitalDataProviderStorage>() {
                    @Override
                    public VitalDataProviderStorage getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getVitalDataProviderStorage();
                    }
                }
            },
            {
                "AppSetIdGetter",
                new ServiceExtractor<AppSetIdGetter>() {
                    @Override
                    public AppSetIdGetter getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getAppSetIdGetter();
                    }
                }
            },
            {
                "sslSocketFactoryProvider",
                new ServiceExtractor<BaseSslSocketFactoryProvider>() {
                    @Override
                    public BaseSslSocketFactoryProvider getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getSslSocketFactoryProvider();
                    }
                }
            },
            {
                "modulesController",
                new ServiceExtractor<ServiceModulesController>() {
                    @Override
                    public ServiceModulesController getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getModulesController();
                    }
                }
            },
            {
                "startupStateHolder",
                new ServiceExtractor<StartupStateHolder>() {
                    @Override
                    public StartupStateHolder getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getStartupStateHolder();
                    }
                }
            },
            {
                "serviceDataReporterHolder",
                new ServiceExtractor<ServiceDataReporterHolder>() {
                    @Override
                    public ServiceDataReporterHolder getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getServiceDataReporterHolder();
                    }
                }
            },
            {
                "modulesEventHandlersHolder",
                new ServiceExtractor<ModuleEventHandlersHolder>() {
                    @Override
                    public ModuleEventHandlersHolder getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getModuleEventHandlersHolder();
                    }
                }
            },
            {
                "locationServiceApi",
                new ServiceExtractor<LocationServiceApi>() {
                    @Override
                    public LocationServiceApi getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getLocationServiceApi();
                    }
                }
            },
            {
                "locationClientApi",
                new ServiceExtractor<LocationClientApi>() {
                    @Override
                    public LocationClientApi getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getLocationClientApi();
                    }
                }
            },
            {
                "telephonyDataProvider",
                new ServiceExtractor<TelephonyDataProvider>() {
                    @Override
                    public TelephonyDataProvider getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getTelephonyDataProvider();
                    }
                }
            },
            {
                "selfDiagnosticReporterStorage",
                new ServiceExtractor<SelfDiagnosticReporterStorage>() {
                    @Override
                    public SelfDiagnosticReporterStorage getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getSelfDiagnosticReporterStorage();
                    }
                }
            },
            {
                "moduleEntryPointsRegister",
                new ServiceExtractor<ModuleEntryPointsRegister>() {
                    @Override
                    public ModuleEntryPointsRegister getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getModuleEntryPointsRegister();
                    }
                }
            },
            {
                "multiProcessSafeUuidProvider",
                new ServiceExtractor<MultiProcessSafeUuidProvider>() {
                    @Override
                    public MultiProcessSafeUuidProvider getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getMultiProcessSafeUuidProvider();
                    }
                }
            },
            {
                "nativeCrashService",
                new ServiceExtractor<NativeCrashService>() {
                    @Override
                    public NativeCrashService getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getNativeCrashService();
                    }
                }
            },
            {
               "platformIdentifier",
               new ServiceExtractor<PlatformIdentifiers>() {
                   @Override
                   public PlatformIdentifiers getService(GlobalServiceLocator globalServiceLocator) {
                       return globalServiceLocator.getPlatformIdentifiers();
                   }
               }
            },
            {
                "sdlEnvironmenHolder",
                new ServiceExtractor<SdkEnvironmentHolder>() {
                    @Override
                    public SdkEnvironmentHolder getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getSdkEnvironmentHolder();
                    }
                }
            },
            {
                "networkCore",
                new ServiceExtractor<NetworkCore>() {
                    @Override
                    public NetworkCore getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getNetworkCore();
                    }
                }
            },
            {
                "getFirstExecutionService",
                new ServiceExtractor<FirstExecutionConditionService>() {
                    @Override
                    public FirstExecutionConditionService getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getFirstExecutionConditionService();
                    }
                }
            },
            {
                "getActivationBarrier",
                new ServiceExtractor<WaitForActivationDelayBarrier>() {
                    @Override
                    public WaitForActivationDelayBarrier getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getActivationBarrier();
                    }
                }
            },
            {
                "getExtraMetaInfoRetriever",
                new ServiceExtractor<ExtraMetaInfoRetriever>() {
                    @Override
                    public ExtraMetaInfoRetriever getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getExtraMetaInfoRetriever();
                    }
                }
            },
            {
                "getServiceLifecycleTimeTracker",
                new ServiceExtractor<ServiceLifecycleTimeTracker>() {
                    @Override
                    public ServiceLifecycleTimeTracker getService(GlobalServiceLocator globalServiceLocator) {
                        return globalServiceLocator.getServiceLifecycleTimeTracker();
                    }
                }
            }
        });
    }

    @Mock
    private Context mContext;
    @Mock
    private SharedPreferences mSharedPreferences;
    @Mock
    private SharedPreferences.Editor mEditor;
    @Mock
    private SelfReporterWrapper selfReporterWrapper;
    @Mock
    private StartupState startupState;
    @Mock
    private SelfProcessReporter selfProcessReporter;
    @Mock
    private NetworkServiceLocator networkServiceLocator;
    @Mock
    private NetworkCore networkCore;

    @Rule
    public final MockedStaticRule<DebugAssert> sDebugAssert = new MockedStaticRule<>(DebugAssert.class);

    @Rule
    public final MockedStaticRule<AppMetricaSelfReportFacade> sAppMetricaSelfReporterFacadeMockedRule =
        new MockedStaticRule<>(AppMetricaSelfReportFacade.class);

    @Rule
    public final MockedStaticRule<NetworkServiceLocator> networkServiceLocatorMockedStaticRule =
        new MockedStaticRule<>(NetworkServiceLocator.class);

    @Rule
    public final MockedConstructionRule<StartupStateHolder> startupStateHolderMockedConstructionRule =
        new MockedConstructionRule<>(StartupStateHolder.class);

    @Rule
    public final MockedConstructionRule<ReferrerHolder> referrerHolderMockedConstructionRule =
        new MockedConstructionRule<>(ReferrerHolder.class);

    @Rule
    public final MockedConstructionRule<AdvertisingIdGetter> advertisingIdGetterMockedConstructionRule =
        new MockedConstructionRule<>(AdvertisingIdGetter.class);

    @Rule
    public final MockedConstructionRule<PlatformIdentifiers> platformIdentifiersMockedConstructionRule =
        new MockedConstructionRule<>(PlatformIdentifiers.class);

    @Rule
    public final MockedConstructionRule<ExtraMetaInfoRetriever> extraMetaInfoRetrieverMockedConstructionRule =
        new MockedConstructionRule<>(ExtraMetaInfoRetriever.class);

    @Rule
    public final MockedConstructionRule<ServiceLifecycleTimeTracker> serviceLifecycleTimeTrackerMockedConstructionRule =
        new MockedConstructionRule<>(ServiceLifecycleTimeTracker.class);

    private GlobalServiceLocator mGlobalServiceLocator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mContext = TestUtils.createMockedContext();
        when(mContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mSharedPreferences);
        when(mSharedPreferences.edit()).thenReturn(mEditor);

        when(AppMetricaSelfReportFacade.getReporter()).thenReturn(selfReporterWrapper);
        when(NetworkServiceLocator.getInstance()).thenReturn(networkServiceLocator);
        when(networkServiceLocator.getNetworkCore()).thenReturn(networkCore);

        GlobalServiceLocator.init(mContext);
        GlobalServiceLocator.getInstance().getStartupStateHolder().onStartupStateChanged(startupState);
        when(startupState.getCollectingFlags()).thenReturn(new CollectingFlags.CollectingFlagsBuilder().build());
        mGlobalServiceLocator = GlobalServiceLocator.getInstance();
        mGlobalServiceLocator.initAsync();
        mGlobalServiceLocator.initSelfDiagnosticReporterStorage(selfProcessReporter);
    }

    @After
    public void tearDown() throws Exception {
        GlobalServiceLocator.destroy();
    }

    @Test
    public void testGetService() {
        Object first = mServiceExtractor.getService(mGlobalServiceLocator);

        assertThat(first).as(mServiceDescription).isNotNull();

        Object second = mServiceExtractor.getService(mGlobalServiceLocator);

        assertThat(first).as(mServiceDescription).isSameAs(second);
    }
}
