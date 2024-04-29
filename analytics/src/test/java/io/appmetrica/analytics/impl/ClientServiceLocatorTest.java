package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.crash.CrashProcessorFactory;
import io.appmetrica.analytics.impl.modules.ModuleEntryPointsRegister;
import io.appmetrica.analytics.impl.modules.client.ClientModulesController;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.startup.uuid.UuidFromClientPreferencesImporter;
import io.appmetrica.analytics.impl.utils.MainProcessDetector;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
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

@RunWith(RobolectricTestRunner.class)
public class ClientServiceLocatorTest extends CommonTest {

    @Mock
    private Context context = Mockito.mock(Context.class);
    @Mock
    private ClientExecutorProvider mClientExecutorProvider;
    @Mock
    private DefaultOneShotMetricaConfig mDefaultOneShotMetricaConfig;
    @Mock
    private ICommonExecutor mApiProxyExecutor;
    @Mock
    private MainProcessDetector mMainProcessDetector;
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
    private CrashProcessorFactory crashProcessorFactory;
    @Mock
    private AppMetricaCoreComponentsProvider coreComponentsProvider;

    @Rule
    public MockedConstructionRule<MultiProcessSafeUuidProvider> multiProcessSafeUuidProviderMockedConstructionRule =
        new MockedConstructionRule<>(MultiProcessSafeUuidProvider.class);

    @Rule
    public MockedConstructionRule<UuidFromClientPreferencesImporter>
        uuidFromClientPreferencesImporterMockedConstructionRule =
        new MockedConstructionRule<>(UuidFromClientPreferencesImporter.class);

    private ClientServiceLocator mClientServiceLocator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mClientServiceLocator = new ClientServiceLocator(
            mMainProcessDetector,
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
    public void getProcessDetector() {
        assertThat(mClientServiceLocator.getProcessDetector()).isSameAs(mMainProcessDetector);
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
            .withIgnoredFields("moduleEntryPointsRegister")
            .checkField("mainProcessDetector", "getMainProcessDetector", mMainProcessDetector)
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
}
