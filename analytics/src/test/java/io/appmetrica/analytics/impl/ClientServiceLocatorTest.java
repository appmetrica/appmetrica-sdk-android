package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.impl.crash.jvm.client.TechnicalCrashProcessorFactory;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.modules.ModuleEntryPointsRegister;
import io.appmetrica.analytics.impl.modules.client.ClientModulesController;
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidFromClientPreferencesImporter;
import io.appmetrica.analytics.impl.utils.AppMetricaServiceProcessDetector;
import io.appmetrica.analytics.impl.utils.process.CurrentProcessDetector;
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientServiceLocatorTest extends CommonTest {

    @Mock
    private Context context = Mockito.mock(Context.class);
    @Mock
    private ClientExecutorProvider mClientExecutorProvider;
    @Mock
    private DefaultOneShotMetricaConfig mDefaultOneShotMetricaConfig;
    @Mock
    private CurrentProcessDetector mCurrentProcessDetector;
    @Mock
    private AppMetricaServiceDelayHandler appMetricaServiceDelayHandler;
    @Mock
    private SessionsTrackingManager sessionsTrackingManager;
    @Mock
    private ActivityLifecycleManager activityLifecycleManager;
    @Mock
    private ContextAppearedListener contextAppearedListener;
    @Mock
    private ActivityAppearedListener activityAppearedListener;
    @Mock
    private ReporterLifecycleListener reporterLifecycleListener;
    @Mock
    private TechnicalCrashProcessorFactory crashProcessorFactory;
    @Mock
    private AppMetricaCoreComponentsProvider coreComponentsProvider;

    @Rule
    public MockedConstructionRule<ScreenInfoRetriever> screenInfoRetrieverRule =
        new MockedConstructionRule<>(ScreenInfoRetriever.class);

    @Rule
    public MockedConstructionRule<MultiProcessSafeUuidProvider> multiProcessSafeUuidProviderMockedConstructionRule =
        new MockedConstructionRule<>(MultiProcessSafeUuidProvider.class);

    @Rule
    public MockedConstructionRule<UuidFromClientPreferencesImporter>
        uuidFromClientPreferencesImporterMockedConstructionRule =
        new MockedConstructionRule<>(UuidFromClientPreferencesImporter.class);

    @Rule
    public MockedConstructionRule<PreferencesClientDbStorage> preferencesClientDbStorageMockedConstructionRule =
        new MockedConstructionRule<>(PreferencesClientDbStorage.class);

    @Rule
    public MockedStaticRule<DatabaseStorageFactory> databaseStorageFactoryMockedStaticRule =
        new MockedStaticRule<>(DatabaseStorageFactory.class);

    @Rule
    public MockedConstructionRule<AppMetricaFacadeProvider> appMetricaFacadeProviderMockedConstructionRule =
        new MockedConstructionRule<>(AppMetricaFacadeProvider.class);

    @Rule
    public MockedConstructionRule<FirstLaunchDetector> firstLaunchDetectorMockedConstructionRule =
        new MockedConstructionRule<>(FirstLaunchDetector.class);

    @Rule
    public MockedConstructionRule<AppMetricaServiceProcessDetector> appMetricaServiceProcessDetectorMockedConstructionRule =
        new MockedConstructionRule<>(AppMetricaServiceProcessDetector.class);

    @Rule
    public MockedConstructionRule<ClientConfigSerializer> clientConfigSerializerMockedConstructionRule =
        new MockedConstructionRule<>(ClientConfigSerializer.class);

    @Rule
    public MockedConstructionRule<AnonymousClientActivator> anonymousClientActivatorMockedConstructionRule =
        new MockedConstructionRule<>(AnonymousClientActivator.class);

    @Rule
    public MockedConstructionRule<ExtraMetaInfoRetriever> extraMetaInfoRetrieverMockedConstructionRule =
        new MockedConstructionRule<>(ExtraMetaInfoRetriever.class);

    @Mock
    private DatabaseStorageFactory databaseStorage;
    @Mock
    private IKeyValueTableDbHelper keyValueTableDbHelper;

    private ClientServiceLocator mClientServiceLocator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(DatabaseStorageFactory.getInstance(context)).thenReturn(databaseStorage);
        when(databaseStorage.getClientDbHelper()).thenReturn(keyValueTableDbHelper);
        mClientServiceLocator = new ClientServiceLocator(
            mCurrentProcessDetector,
            mDefaultOneShotMetricaConfig,
            mClientExecutorProvider,
            activityAppearedListener,
            appMetricaServiceDelayHandler,
            activityLifecycleManager,
            sessionsTrackingManager,
            contextAppearedListener,
            crashProcessorFactory,
            coreComponentsProvider
        );
    }

    @Test
    public void getProcessNameProvider() {
        assertThat(mClientServiceLocator.getProcessNameProvider()).isSameAs(mCurrentProcessDetector);
    }

    @Test
    public void getMetricaServiceDelayHandler() {
        assertThat(mClientServiceLocator.getAppMetricaServiceDelayHandler()).isSameAs(appMetricaServiceDelayHandler);
    }

    @Test
    public void getSessionsTrackingManager() {
        assertThat(mClientServiceLocator.getSessionsTrackingManager()).isSameAs(sessionsTrackingManager);
    }

    @Test
    public void getActivityLifecycleManager() {
        assertThat(mClientServiceLocator.getActivityLifecycleManager()).isSameAs(activityLifecycleManager);
    }

    @Test
    public void getContextAppearedListener() {
        assertThat(mClientServiceLocator.getContextAppearedListener()).isSameAs(contextAppearedListener);
    }

    @Test
    public void getActivityListener() {
        assertThat(mClientServiceLocator.getActivityAppearedListener()).isSameAs(activityAppearedListener);
    }

    @Test
    public void getScreenInfoRetriever() {
        ScreenInfoRetriever first = mClientServiceLocator.getScreenInfoRetriever();
        ScreenInfoRetriever second = mClientServiceLocator.getScreenInfoRetriever();

        assertThat(screenInfoRetrieverRule.getConstructionMock().constructed())
            .hasSize(1)
            .first()
            .isEqualTo(first)
            .isEqualTo(second);

        assertThat(screenInfoRetrieverRule.getArgumentInterceptor().flatArguments())
            .isEmpty();
        verify(activityAppearedListener).registerListener(first);
    }

    @Test
    public void getCrashProcessorCreatorProvider() {
        assertThat(mClientServiceLocator.getCrashProcessorFactory()).isSameAs(crashProcessorFactory);
    }

    @Test
    public void getMainReporterLifecycleListener() {
        assertThat(mClientServiceLocator.getReporterLifecycleListener()).isNull();
        mClientServiceLocator.registerReporterLifecycleListener(reporterLifecycleListener);
        assertThat(mClientServiceLocator.getReporterLifecycleListener()).isSameAs(reporterLifecycleListener);
    }

    @Test
    public void allFieldsFilled() throws Exception {
        ObjectPropertyAssertions(mClientServiceLocator)
            .withDeclaredAccessibleFields(true)
            .withIgnoredFields(
                "moduleEntryPointsRegister",
                "appMetricaFacadeProvider",
                "firstLaunchDetector",
                "appMetricaServiceProcessDetector",
                "clientConfigSerializer"
            )
            .checkField("currentProcessDetector", "getCurrentProcessDetector", mCurrentProcessDetector)
            .checkField("defaultOneShotConfig", "getDefaultOneShotConfig", mDefaultOneShotMetricaConfig)
            .checkField("clientExecutorProvider", "getClientExecutorProvider", mClientExecutorProvider)
            .checkField("appMetricaServiceDelayHandler", "getAppMetricaServiceDelayHandler", appMetricaServiceDelayHandler)
            .checkField("activityLifecycleManager", "getActivityLifecycleManager", activityLifecycleManager)
            .checkField("sessionsTrackingManager", "getSessionsTrackingManager", sessionsTrackingManager)
            .checkField("contextAppearedListener", "getContextAppearedListener", contextAppearedListener)
            .checkField("activityAppearedListener", "getActivityAppearedListener", activityAppearedListener)
            .checkField("crashProcessorFactory", "getCrashProcessorFactory", crashProcessorFactory)
            .checkField("appMetricaCoreComponentsProvider", "getAppMetricaCoreComponentsProvider", coreComponentsProvider)
            .checkAll();
    }

    @Test
    public void moduleEntryPointsRegisterCreatedOnce() {
        final ModuleEntryPointsRegister first = mClientServiceLocator.getModuleEntryPointsRegister();
        final ModuleEntryPointsRegister second = mClientServiceLocator.getModuleEntryPointsRegister();

        assertThat(first).isSameAs(second);
    }

    @Test
    public void moduleControllerCreatedOnce() {
        final ClientModulesController first = mClientServiceLocator.getModulesController();
        final ClientModulesController second = mClientServiceLocator.getModulesController();

        assertThat(first).isSameAs(second);
    }

    @Test
    public void getMultiProcessSafeUuidProvider() {
        MultiProcessSafeUuidProvider first = mClientServiceLocator.getMultiProcessSafeUuidProvider(context);
        MultiProcessSafeUuidProvider second = mClientServiceLocator.getMultiProcessSafeUuidProvider(context);

        assertThat(multiProcessSafeUuidProviderMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1)
            .first()
            .isEqualTo(first)
            .isEqualTo(second);

        assertThat(multiProcessSafeUuidProviderMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(
                context,
                uuidFromClientPreferencesImporterMockedConstructionRule.getConstructionMock().constructed().get(0)
            );

        assertThat(uuidFromClientPreferencesImporterMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1);

        assertThat(uuidFromClientPreferencesImporterMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .isEmpty();
    }

    @Test
    public void getPreferencesClientDbStorage() {
        assertThat(mClientServiceLocator.getPreferencesClientDbStorage(context))
            .isSameAs(mClientServiceLocator.getPreferencesClientDbStorage(context))
            .isSameAs(preferencesClientDbStorageMockedConstructionRule.getConstructionMock().constructed().get(0));

        assertThat(preferencesClientDbStorageMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1);
        assertThat(preferencesClientDbStorageMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(keyValueTableDbHelper);
    }

    @Test
    public void getAppMetricaFacadeProvider() {
        assertThat(mClientServiceLocator.getAppMetricaFacadeProvider())
            .isEqualTo(appMetricaFacadeProviderMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(appMetricaFacadeProviderMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(appMetricaFacadeProviderMockedConstructionRule.getArgumentInterceptor().flatArguments()).isEmpty();
    }

    @Test
    public void getFirstLaunchDetector() {
        assertThat(mClientServiceLocator.getFirstLaunchDetector())
            .isEqualTo(firstLaunchDetectorMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(firstLaunchDetectorMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(firstLaunchDetectorMockedConstructionRule.getArgumentInterceptor().flatArguments()).isEmpty();
    }

    @Test
    public void getClientConfigSerializer() {
        assertThat(mClientServiceLocator.getClientConfigSerializer())
            .isEqualTo(mClientServiceLocator.getClientConfigSerializer())
            .isEqualTo(clientConfigSerializerMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(clientConfigSerializerMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(clientConfigSerializerMockedConstructionRule.getArgumentInterceptor().flatArguments()).isEmpty();
    }

    @Test
    public void getAnonymousClientActivator() {
        assertThat(mClientServiceLocator.getAnonymousClientActivator())
            .isSameAs(mClientServiceLocator.getAnonymousClientActivator())
            .isEqualTo(anonymousClientActivatorMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(anonymousClientActivatorMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(anonymousClientActivatorMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(
                mClientServiceLocator.getAppMetricaFacadeProvider(),
                mClientServiceLocator.getSessionsTrackingManager(),
                mClientServiceLocator.getClientExecutorProvider()
            );
    }

    @Test
    public void getAppMetricaServiceProcessDetector() {
        assertThat(mClientServiceLocator.getAppMetricaServiceProcessDetector())
            .isEqualTo(
                appMetricaServiceProcessDetectorMockedConstructionRule.getConstructionMock().constructed().get(0)
            );
        assertThat(appMetricaServiceProcessDetectorMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1);
        assertThat(appMetricaServiceProcessDetectorMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .isEmpty();
    }

    @Test
    public void getExtraMetaInfoRetriever() {
        assertThat(mClientServiceLocator.getExtraMetaInfoRetriever(context))
            .isSameAs(mClientServiceLocator.getExtraMetaInfoRetriever(context))
            .isEqualTo(extraMetaInfoRetrieverMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(extraMetaInfoRetrieverMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(extraMetaInfoRetrieverMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(context);
    }
}
