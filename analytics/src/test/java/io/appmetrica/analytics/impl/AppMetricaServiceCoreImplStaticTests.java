package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Bundle;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncher;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncherProvider;
import io.appmetrica.analytics.impl.crash.service.ServiceCrashController;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.modules.ServiceContextFacade;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaServiceCoreImplStaticTests extends CommonTest {

    private Context mContext;
    @Mock
    private AppMetricaServiceCallback mCallback;
    @Mock
    private ClientRepository mClientRepository;
    @Mock
    private AppMetricaServiceLifecycle mAppMetricaServiceLifecycle;
    @Mock
    private CollectingFlags mCollectingFlags;
    @Mock
    private ReportConsumer mReportConsumer;
    @Mock
    private ApplicationStateProviderImpl mApplicationStateProvider;
    @Mock
    private FirstServiceEntryPointManager firstServiceEntryPointManager;
    @Mock
    private AppMetricaServiceCoreImplFieldsFactory fieldsFactory;
    @Mock
    private ReportConsumer reportConsumer;
    @Mock
    private ClientConfiguration clientConfiguration;

    private StartupState mStartupState;
    private AppMetricaServiceCoreImpl mMetricaCore;

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Rule
    public final MockedStaticRule<CounterReport> sCounterReport = new MockedStaticRule<>(CounterReport.class);
    @Rule
    public final MockedStaticRule<ProcessConfiguration> sProcessConfiguration = new MockedStaticRule<>(ProcessConfiguration.class);
    @Rule
    public final MockedStaticRule<CounterConfiguration> sCounterConfiguration = new MockedStaticRule<>(CounterConfiguration.class);
    @Rule
    public final MockedStaticRule<ClientConfiguration> sClientConfiguration = new MockedStaticRule<>(ClientConfiguration.class);
    @Rule
    public MockedConstructionRule<CoreImplFirstCreateTaskLauncherProvider> firstCreateTaskLauncherProviderRule =
        new MockedConstructionRule<>(
            CoreImplFirstCreateTaskLauncherProvider.class,
            new MockedConstruction.MockInitializer<CoreImplFirstCreateTaskLauncherProvider>() {
                @Override
                public void prepare(CoreImplFirstCreateTaskLauncherProvider mock,
                                    MockedConstruction.Context context) throws Throwable {
                    when(mock.getLauncher()).thenReturn(mock(CoreImplFirstCreateTaskLauncher.class));
                }
            }
        );
    @Rule
    public MockedConstructionRule<ServiceCrashController> serviceCrashControllerMockedConstructionRule =
        new MockedConstructionRule<>(ServiceCrashController.class);
    @Rule
    public MockedConstructionRule<AppMetricaServiceCoreImplFieldsFactory>
        appMetricaServiceCoreImplFieldsFactoryMockedConstructionRule =
        new MockedConstructionRule<>(AppMetricaServiceCoreImplFieldsFactory.class);

    @Rule
    public MockedConstructionRule<ServiceContextFacade> serviceContextFacadeMockedConstructionRule =
        new MockedConstructionRule<>(ServiceContextFacade.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProvider())
            .thenReturn(mock(VitalCommonDataProvider.class));

        doReturn(reportConsumer).when(fieldsFactory).createReportConsumer(same(mContext), any(ClientRepository.class));

        mMetricaCore = new AppMetricaServiceCoreImpl(
            mContext,
            mCallback,
            mClientRepository,
            mAppMetricaServiceLifecycle,
            firstServiceEntryPointManager,
            mApplicationStateProvider,
            fieldsFactory
        );

        mStartupState = new StartupState.Builder(mCollectingFlags).build();
        GlobalServiceLocator.getInstance().getStartupStateHolder().onStartupStateChanged(mStartupState);
        mMetricaCore.onCreate();
        mMetricaCore.setReportConsumer(mReportConsumer);
    }

    @Test
    public void testReportData() throws Exception {
        CounterReport counterReport = mock(CounterReport.class);
        Bundle bundle = mock(Bundle.class);
        when(CounterReport.fromBundle(bundle)).thenReturn(counterReport);
        mMetricaCore.reportData(bundle);
        verify(mReportConsumer).consumeReport(counterReport, bundle);
    }
}
