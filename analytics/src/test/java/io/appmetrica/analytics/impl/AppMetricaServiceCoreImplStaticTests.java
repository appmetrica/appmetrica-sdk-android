package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncher;
import io.appmetrica.analytics.impl.core.CoreImplFirstCreateTaskLauncherProvider;
import io.appmetrica.analytics.impl.crash.jvm.CrashDirectoryWatcher;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.io.File;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
    private CrashDirectoryWatcher crashDirectoryWatcher;
    @Mock
    private ICommonExecutor reportExecutor;
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
    public final MockedStaticRule<FileUtils> sFileUtils = new MockedStaticRule<>(FileUtils.class);
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

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProvider())
                .thenReturn(mock(VitalCommonDataProvider.class));

        when(FileUtils.getCrashesDirectory(mContext)).thenReturn(mock(File.class));
        doReturn(crashDirectoryWatcher).when(fieldsFactory).createCrashDirectoryWatcher(
                any(File.class),
                any(Consumer.class)
        );
        doReturn(reportConsumer).when(fieldsFactory).createReportConsumer(same(mContext), any(ClientRepository.class));

        mMetricaCore = new AppMetricaServiceCoreImpl(
            mContext,
            mCallback,
            mClientRepository,
            mAppMetricaServiceLifecycle,
            firstServiceEntryPointManager,
            mApplicationStateProvider,
            reportExecutor,
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

    @Test
    public void testOnStartWithCrashIntent() {
        CounterReport counterReport = mock(CounterReport.class);
        Intent intent = prepareCrashIntent(counterReport);
        final int startId = 20;
        mMetricaCore.onStart(intent, startId);
        verify(mReportConsumer).consumeCrash(any(ClientDescription.class), same(counterReport), any(CommonArguments.class));
        verify(mCallback).onStartFinished(startId);
    }

    @Test
    public void testOnStartWithCrashIntentUpdatedCallback() {
        AppMetricaServiceCallback secondCallback  = mock(AppMetricaServiceCallback.class);
        mMetricaCore.updateCallback(secondCallback);
        CounterReport counterReport = mock(CounterReport.class);
        Intent intent = prepareCrashIntent(counterReport);
        final int startId = 20;
        mMetricaCore.onStart(intent, startId);
        verify(mReportConsumer).consumeCrash(any(ClientDescription.class), same(counterReport), any(CommonArguments.class));
        verify(secondCallback).onStartFinished(startId);
        verifyNoMoreInteractions(mCallback);
    }

    @Test
    public void testOnStartCommandWithCrashIntent() {
        CounterReport counterReport = mock(CounterReport.class);
        Intent intent = prepareCrashIntent(counterReport);
        final int startId = 20;
        mMetricaCore.onStartCommand(intent, 0, startId);
        verify(mReportConsumer).consumeCrash(any(ClientDescription.class), same(counterReport), any(CommonArguments.class));
        verify(mCallback).onStartFinished(startId);
    }

    @Test
    public void testOnStartCommandWithCrashIntentUpdatedCallback() {
        AppMetricaServiceCallback secondCallback  = mock(AppMetricaServiceCallback.class);
        mMetricaCore.updateCallback(secondCallback);
        CounterReport counterReport = mock(CounterReport.class);
        Intent intent = prepareCrashIntent(counterReport);
        final int startId = 20;
        mMetricaCore.onStartCommand(intent, 0, startId);
        verify(mReportConsumer).consumeCrash(any(ClientDescription.class), same(counterReport), any(CommonArguments.class));
        verify(secondCallback).onStartFinished(startId);
        verifyNoMoreInteractions(mCallback);
    }

    private Intent prepareCrashIntent(CounterReport counterReport) {
        ProcessConfiguration processConfiguration = mock(ProcessConfiguration.class);
        CounterConfiguration counterConfiguration = mock(CounterConfiguration.class);
        Bundle bundle = mock(Bundle.class);
        Intent intent = mock(Intent.class);
        when(intent.getData()).thenReturn(mock(Uri.class));
        when(intent.getExtras()).thenReturn(bundle);
        when(ProcessConfiguration.fromBundle(bundle)).thenReturn(processConfiguration);
        when(CounterConfiguration.fromBundle(bundle)).thenReturn(counterConfiguration);
        when(CounterReport.fromBundle(bundle)).thenReturn(counterReport);
        when(ClientConfiguration.fromBundle(mContext, bundle)).thenReturn(clientConfiguration);
        when(clientConfiguration.getProcessConfiguration()).thenReturn(processConfiguration);
        when(clientConfiguration.getReporterConfiguration()).thenReturn(counterConfiguration);
        when(counterReport.isNoEvent()).thenReturn(false);
        when(counterReport.isUndefinedType()).thenReturn(false);
        return intent;
    }

}
