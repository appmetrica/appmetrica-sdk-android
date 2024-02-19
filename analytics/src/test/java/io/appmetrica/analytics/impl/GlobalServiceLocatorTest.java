package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceLocator;
import io.appmetrica.analytics.impl.clids.ClidsCandidatesHelper;
import io.appmetrica.analytics.impl.clids.ClidsDataAwaiter;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.clids.ClidsPriorityProvider;
import io.appmetrica.analytics.impl.clids.ClidsSatelliteCheckedProvider;
import io.appmetrica.analytics.impl.clids.ClidsStateProvider;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.network.http.BaseSslSocketFactoryProvider;
import io.appmetrica.analytics.impl.network.http.SslSocketFactoryProviderImpl;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoCandidatesHelper;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoDataAwaiter;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoFromSatelliteProvider;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoPriorityProvider;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoSatelliteCheckedProvider;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoStateProvider;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.service.ServiceDataReporterHolder;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidFromStartupStateImporter;
import io.appmetrica.analytics.impl.utils.DebugAssert;
import io.appmetrica.analytics.networktasks.internal.NetworkCore;
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ConstructionArgumentCaptor;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import io.appmetrica.analytics.testutils.rules.coreutils.UtilityServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GlobalServiceLocatorTest extends CommonTest {

    private Context mContext;
    @Mock
    private LifecycleDependentComponentManager lifecycleDependentComponentManager;
    @Mock
    private StartupState startupState;
    @Mock
    private IReporterExtended reporter;
    @Mock
    private NetworkServiceLocator networkServiceLocator;
    @Mock
    private NetworkCore networkCore;
    @Rule
    public final MockedStaticRule<DebugAssert> sDebugAssert = new MockedStaticRule<>(DebugAssert.class);
    @Rule
    public final MockedConstructionRule<ClidsInfoStorage> clidsInfoMock = new MockedConstructionRule<>(ClidsInfoStorage.class);
    @Rule
    public final MockedConstructionRule<PreloadInfoStorage> preloadInfoMock = new MockedConstructionRule<>(PreloadInfoStorage.class);
    @Rule
    public final MockedStaticRule<NetworkServiceLocator> networkServiceLocatorMockedStaticRule =
        new MockedStaticRule<>(NetworkServiceLocator.class);
    @Rule
    public final UtilityServiceLocatorRule utilityServiceLocatorRule = new UtilityServiceLocatorRule();
    @Rule
    public final MockedStaticRule<AppMetricaSelfReportFacade> appMetricaSelfReportFacadeMockedRule =
        new MockedStaticRule<>(AppMetricaSelfReportFacade.class);
    @Rule
    public final MockedConstructionRule<UtilityServiceStartupStateObserver> utilityServiceStartupObserverMockedRule =
        new MockedConstructionRule<>(UtilityServiceStartupStateObserver.class);

    @Rule
    public final MockedConstructionRule<StartupStateHolder> startupStateHolderMockedRule =
        new MockedConstructionRule<>(StartupStateHolder.class, new MockedConstruction.MockInitializer<StartupStateHolder>() {
            @Override
            public void prepare(StartupStateHolder mock, MockedConstruction.Context context) throws Throwable {
                when(mock.getStartupState()).thenReturn(startupState);
            }
        });

    @Rule
    public final MockedConstructionRule<ServiceDataReporterHolder> serviceDataReporterHolderMockedRule =
        new MockedConstructionRule<>(ServiceDataReporterHolder.class);

    @Rule
    public final MockedConstructionRule<MultiProcessSafeUuidProvider> multiProcessSafeUuidProviderMockedConstructionRule =
        new MockedConstructionRule<>(MultiProcessSafeUuidProvider.class);

    @Rule
    public final MockedConstructionRule<UuidFromStartupStateImporter> uuidFromStartupStateImporterMockedConstructionRule =
        new MockedConstructionRule<>(UuidFromStartupStateImporter.class);

    @Rule
    public final MockedConstructionRule<PlatformIdentifiers> platformIdentifiersMockedConstructionRule =
        new MockedConstructionRule<>(PlatformIdentifiers.class);

    @Rule
    public final MockedConstructionRule<SdkEnvironmentHolder> sdkEnvironmentHolderMockedConstructionRule =
        new MockedConstructionRule<>(SdkEnvironmentHolder.class);

    private GlobalServiceLocator mGlobalServiceLocator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(NetworkServiceLocator.getInstance()).thenReturn(networkServiceLocator);
        when(networkServiceLocator.getNetworkCore()).thenReturn(networkCore);
        when(AppMetricaSelfReportFacade.getReporter()).thenReturn(reporter);
        mContext = TestUtils.createMockedContext();
    }

    @After
    public void tearDown() {
        GlobalServiceLocator.destroy();
    }

    @Test
    public void initAsync() {
        GlobalServiceLocator.init(mContext);
        mGlobalServiceLocator = GlobalServiceLocator.getInstance();
        mGlobalServiceLocator.initAsync();

        StartupStateHolder startupStateHolder = startupStateHolder();

        InOrder inOrder = inOrder(
            UtilityServiceLocator.getInstance(),
            NetworkServiceLocator.getInstance(),
            startupStateHolder
        );

        inOrder.verify(UtilityServiceLocator.getInstance()).initAsync();
        inOrder.verify(startupStateHolder).init(mContext);
        inOrder.verify(startupStateHolder)
            .registerObserver(utilityServiceStartupObserverMockedRule.getConstructionMock().constructed().get(0));
        networkServiceLocatorMockedStaticRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                NetworkServiceLocator.init();
            }
        });
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void networkServiceLifecycleObserver() {
        GlobalServiceLocator.init(mContext);
        mGlobalServiceLocator = GlobalServiceLocator.getInstance();
        mGlobalServiceLocator.setLifecycleDependentComponentManager(lifecycleDependentComponentManager);
        mGlobalServiceLocator.initAsync();
        ArgumentCaptor<ServiceLifecycleObserver> observerArgumentCaptor =
            ArgumentCaptor.forClass(ServiceLifecycleObserver.class);
        verify(lifecycleDependentComponentManager)
            .addLifecycleObserver(observerArgumentCaptor.capture());
        assertThat(observerArgumentCaptor.getAllValues()).hasSize(1);
        observerArgumentCaptor.getValue().onCreate();
        verify(NetworkServiceLocator.getInstance()).onCreate();
        observerArgumentCaptor.getValue().onDestroy();
        verify(NetworkServiceLocator.getInstance()).onDestroy();
    }

    @Test
    public void testAdvertisingIdGetterCreation() {
        ConstructionArgumentCaptor<AdvertisingIdGetter> captor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<AdvertisingIdGetter> construction = Mockito.mockConstruction(AdvertisingIdGetter.class, captor)) {
            GlobalServiceLocator.init(mContext);
            mGlobalServiceLocator = GlobalServiceLocator.getInstance();
            mGlobalServiceLocator.getServiceInternalAdvertisingIdGetter();
            assertThat(captor.getArguments()).flatExtracting(
                new Function<List<Object>, Object>() {
                    @Override
                    public Collection<? extends Object> apply(List<Object> input) {
                        return Arrays.asList(input.get(3), input.get(4));
                    }
                }
            ).containsExactly(
                Arrays.asList(GlobalServiceLocator.getInstance().getServiceExecutorProvider().getDefaultExecutor(),
                    "ServiceInternal")
            );
        }
    }

    @Test
    public void testGetPreloadInfoStorageNoInitAsync() {
        GlobalServiceLocator.init(mContext);
        mGlobalServiceLocator = GlobalServiceLocator.getInstance();
        PreloadInfoStorage storage = mGlobalServiceLocator.getPreloadInfoStorage();
        assertThat(storage).isNotNull();
        mGlobalServiceLocator.initAsync();
        assertThat(mGlobalServiceLocator.getPreloadInfoStorage()).isSameAs(storage);
    }

    @Test
    public void getApplicationStateProvider() {
        GlobalServiceLocator.init(mContext);
        mGlobalServiceLocator = GlobalServiceLocator.getInstance();
        assertThat(mGlobalServiceLocator.getApplicationStateProvider())
            .isSameAs(mGlobalServiceLocator.getLifecycleDependentComponentManager().getApplicationStateProvider());
    }

    @Test
    public void getServiceDataReporterHolder() {
        GlobalServiceLocator.init(mContext);
        mGlobalServiceLocator = GlobalServiceLocator.getInstance();
        assertThat(mGlobalServiceLocator.getServiceDataReporterHolder())
            .isSameAs(serviceDataReporterHolderMockedRule.getConstructionMock().constructed().get(0));
    }

    @Test
    public void clidsInfoStorageCreation() {
        GlobalServiceLocator.init(mContext);
        mGlobalServiceLocator = GlobalServiceLocator.getInstance();
        mGlobalServiceLocator.getClidsStorage();
        List<Object> arguments = clidsInfoMock.getArgumentInterceptor().flatArguments();
        assertThat(arguments.size()).isEqualTo(10);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(arguments.get(0)).isSameAs(mContext);
        ProtobufStateStorage<ClidsInfo> ignored = (ProtobufStateStorage<ClidsInfo>) arguments.get(1);
        softly.assertThat(arguments.get(2)).isInstanceOf(ClidsPriorityProvider.class);
        softly.assertThat(arguments.get(3)).isInstanceOf(ClidsCandidatesHelper.class);
        softly.assertThat(arguments.get(4)).isInstanceOf(ClidsStateProvider.class);
        softly.assertThat(arguments.get(5)).isInstanceOf(SatelliteClidsInfoProvider.class);
        softly.assertThat(arguments.get(6)).isInstanceOf(ClidsSatelliteCheckedProvider.class);
        softly.assertThat(arguments.get(7)).isInstanceOf(ClidsDataAwaiter.class);
        softly.assertThat(arguments.get(8)).isInstanceOf(ClidsInfo.class);
        softly.assertThat(arguments.get(9)).isEqualTo("[ClidsInfoStorage]");
        softly.assertAll();
    }

    @Test
    public void preloadInfoStorageCreation() {
        GlobalServiceLocator.init(mContext);
        mGlobalServiceLocator = GlobalServiceLocator.getInstance();
        mGlobalServiceLocator.getPreloadInfoStorage();
        List<Object> arguments = preloadInfoMock.getArgumentInterceptor().flatArguments();
        assertThat(arguments.size()).isEqualTo(10);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(arguments.get(0)).isSameAs(mContext);
        ProtobufStateStorage<PreloadInfoData> ignored = (ProtobufStateStorage<PreloadInfoData>) arguments.get(1);
        softly.assertThat(arguments.get(2)).isInstanceOf(PreloadInfoPriorityProvider.class);
        softly.assertThat(arguments.get(3)).isInstanceOf(PreloadInfoCandidatesHelper.class);
        softly.assertThat(arguments.get(4)).isInstanceOf(PreloadInfoStateProvider.class);
        softly.assertThat(arguments.get(5)).isInstanceOf(PreloadInfoFromSatelliteProvider.class);
        softly.assertThat(arguments.get(6)).isInstanceOf(PreloadInfoSatelliteCheckedProvider.class);
        softly.assertThat(arguments.get(7)).isInstanceOf(PreloadInfoDataAwaiter.class);
        softly.assertThat(arguments.get(8)).isInstanceOf(PreloadInfoData.class);
        softly.assertThat(arguments.get(9)).isEqualTo("[PreloadInfoStorage]");
        softly.assertAll();
    }

    @Test
    public void initStartupStateHolder() {
        GlobalServiceLocator.init(mContext);
        assertThat(GlobalServiceLocator.getInstance().getStartupStateHolder())
            .isEqualTo(startupStateHolder());
    }

    @Test
    public void getServiceInternalAdvertisingIdGetterSubscribeStartupObserver() {
        GlobalServiceLocator.init(mContext);
        AdvertisingIdGetter advertisingIdGetter =
            GlobalServiceLocator.getInstance().getServiceInternalAdvertisingIdGetter();
        verify(startupStateHolder()).registerObserver(advertisingIdGetter);
    }

    @Test
    public void getSslSockedFactoryProviderSubscribeStartupObserver() {
        ConstructionArgumentCaptor<SslSocketFactoryProviderImpl> captor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<SslSocketFactoryProviderImpl> mockedSslFactoryImpl =
                 Mockito.mockConstruction(SslSocketFactoryProviderImpl.class, captor)) {

            GlobalServiceLocator.init(mContext);
            BaseSslSocketFactoryProvider sslSocketFactoryProvider =
                GlobalServiceLocator.getInstance().getSslSocketFactoryProvider();

            assertThat(mockedSslFactoryImpl.constructed()).hasSize(1);
            assertThat(captor.flatArguments()).containsExactly(mContext);
            assertThat(sslSocketFactoryProvider).isEqualTo(mockedSslFactoryImpl.constructed().get(0));

            verify(startupStateHolder()).registerObserver(sslSocketFactoryProvider);
        }
    }

    @Test
    public void setSslSockedFactoryProviderReSubscribeStartupObserver() {
        ConstructionArgumentCaptor<SslSocketFactoryProviderImpl> captor = new ConstructionArgumentCaptor<>();
        try (MockedConstruction<SslSocketFactoryProviderImpl> mockedSslFactoryImpl =
                 Mockito.mockConstruction(SslSocketFactoryProviderImpl.class, captor)) {

            GlobalServiceLocator.init(mContext);
            BaseSslSocketFactoryProvider sslSocketFactoryProvider =
                GlobalServiceLocator.getInstance().getSslSocketFactoryProvider();

            BaseSslSocketFactoryProvider newSslSocketFactoryProvider = mock(BaseSslSocketFactoryProvider.class);
            GlobalServiceLocator.getInstance().setSslSocketFactoryProvider(newSslSocketFactoryProvider);

            assertThat(GlobalServiceLocator.getInstance().getSslSocketFactoryProvider())
                .isNotEqualTo(sslSocketFactoryProvider);
            StartupStateHolder startupStateHolder = startupStateHolder();

            InOrder inOrder = inOrder(startupStateHolder);
            inOrder.verify(startupStateHolder).registerObserver(sslSocketFactoryProvider);
            inOrder.verify(startupStateHolder).removeObserver(sslSocketFactoryProvider);
            inOrder.verify(startupStateHolder).registerObserver(newSslSocketFactoryProvider);
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    public void getMultiProcessSafeUuidProvider() {
        GlobalServiceLocator.init(mContext);
        MultiProcessSafeUuidProvider first = GlobalServiceLocator.getInstance().getMultiProcessSafeUuidProvider();
        MultiProcessSafeUuidProvider second = GlobalServiceLocator.getInstance().getMultiProcessSafeUuidProvider();

        assertThat(multiProcessSafeUuidProviderMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1)
            .first()
            .isEqualTo(first)
            .isEqualTo(second);

        assertThat(multiProcessSafeUuidProviderMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(
                mContext,
                uuidFromStartupStateImporterMockedConstructionRule.getConstructionMock().constructed().get(0)
            );

        assertThat(uuidFromStartupStateImporterMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1);
        assertThat(uuidFromStartupStateImporterMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .isEmpty();
    }

    @Test
    public void getPlatformIdentifiers() {
        GlobalServiceLocator.init(mContext);

        assertThat(GlobalServiceLocator.getInstance().getPlatformIdentifiers())
            .isSameAs(platformIdentifiersMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(platformIdentifiersMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);

        assertThat(platformIdentifiersMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(
                GlobalServiceLocator.getInstance().getServiceInternalAdvertisingIdGetter(),
                GlobalServiceLocator.getInstance().getAppSetIdGetter()
            );
    }

    @Test
    public void getNetworkCore() {
        GlobalServiceLocator.init(mContext);

        assertThat(GlobalServiceLocator.getInstance().getNetworkCore()).isSameAs(networkCore);
    }

    @Test
    public void getSdkEnvironmentHolder() {
        GlobalServiceLocator.init(mContext);

        assertThat(GlobalServiceLocator.getInstance().getSdkEnvironmentHolder())
            .isSameAs(sdkEnvironmentHolderMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(sdkEnvironmentHolderMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(sdkEnvironmentHolderMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(mContext);
    }

    private StartupStateHolder startupStateHolder() {
        assertThat(startupStateHolderMockedRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(startupStateHolderMockedRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return startupStateHolderMockedRule.getConstructionMock().constructed().get(0);
    }
}
