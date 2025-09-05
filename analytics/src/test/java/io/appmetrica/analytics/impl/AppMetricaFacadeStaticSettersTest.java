package io.appmetrica.analytics.impl;

import android.content.Context;
import android.location.Location;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaFacadeStaticSettersTest extends CommonTest {

    @Mock
    private MainReporterApiConsumerProvider mainReporterApiConsumerProvider;
    @Mock
    private AppMetricaCore core;
    @Mock
    private AppMetricaImpl impl;
    @Mock
    private IReporterExtended someReporter;
    @Mock
    private IHandlerExecutor executor;
    @Mock
    private Thread initCoreThread;

    private Context context;

    @Rule
    public final ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Rule
    public MockedConstructionRule<ClientMigrationManager> clientMigrationManagerMockedConstructionRule =
        new MockedConstructionRule<>(ClientMigrationManager.class);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        AppMetricaFacade.killInstance();
        when(ClientServiceLocator.getInstance().getAppMetricaCoreComponentsProvider().getCore(
            same(context), any(ClientExecutorProvider.class)
        )).thenReturn(core);
        when(ClientServiceLocator.getInstance().getAppMetricaCoreComponentsProvider().getImpl(
            context, core
        )).thenReturn(impl);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor()).thenReturn(executor);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getCoreInitThread(any()))
            .thenReturn(initCoreThread);
        doReturn(someReporter).when(impl).getReporter(ArgumentMatchers.<ReporterConfig>any());
    }

    @Test
    public void setLocationNoInstance() {
        Location location = mock(Location.class);
        setUpNoInstance();
        AppMetricaFacade.setLocation(location);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocation(location);
    }

    @Test
    public void setLocationFutureNotDone() {
        Location location = mock(Location.class);
        setUpFutureNotDone();
        AppMetricaFacade.setLocation(location);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocation(location);
    }

    @Test
    public void setLocationNoMainReporter() {
        Location location = mock(Location.class);
        setUpNoMainReporter();
        AppMetricaFacade.setLocation(location);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocation(location);
    }

    @Test
    public void setLocationInitialized() {
        Location location = mock(Location.class);
        setUpInitialized();
        AppMetricaFacade.setLocation(location);
        verify(impl).setLocation(location);
    }

    @Test
    public void setLocationTrackingNoInstance() {
        boolean value = new Random().nextBoolean();
        setUpNoInstance();
        AppMetricaFacade.setLocationTracking(value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocationTracking(value);
    }

    @Test
    public void setLocationTrackingFutureNotDone() {
        boolean value = new Random().nextBoolean();
        setUpFutureNotDone();
        AppMetricaFacade.setLocationTracking(value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocationTracking(value);
    }

    @Test
    public void setLocationTrackingNoMainReporter() {
        boolean value = new Random().nextBoolean();
        setUpNoMainReporter();
        AppMetricaFacade.setLocationTracking(value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocationTracking(value);
    }

    @Test
    public void setLocationTrackingInitialized() {
        boolean value = new Random().nextBoolean();
        setUpInitialized();
        AppMetricaFacade.setLocationTracking(value);
        verify(impl).setLocationTracking(value);
    }

    @Test
    public void setAdvIdentifiersTrackingNoInstance() {
        setUpNoInstance();
        AppMetricaFacade.setAdvIdentifiersTracking(true);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setAdvIdentifiersTracking(true, true);
    }

    @Test
    public void setAdvIdentifiersTrackingFutureNotDone() {
        setUpFutureNotDone();
        AppMetricaFacade.setAdvIdentifiersTracking(true);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setAdvIdentifiersTracking(true, true);
    }

    @Test
    public void setAdvIdentifiersTrackingNoMainReporter() {
        setUpNoMainReporter();
        AppMetricaFacade.setAdvIdentifiersTracking(true);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setAdvIdentifiersTracking(true, true);
    }

    @Test
    public void setAdvIdentifiersTrackingInitialized() {
        setUpInitialized();
        AppMetricaFacade.setAdvIdentifiersTracking(true);
        verify(impl).setAdvIdentifiersTracking(true, true);
    }

    @Test
    public void setDataSendingEnabledNoInstance() {
        boolean value = new Random().nextBoolean();
        setUpNoInstance();
        AppMetricaFacade.setDataSendingEnabled(value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setDataSendingEnabled(value);
    }

    @Test
    public void setDataSendingEnabledFutureNotDone() {
        boolean value = new Random().nextBoolean();
        setUpFutureNotDone();
        AppMetricaFacade.setDataSendingEnabled(value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setDataSendingEnabled(value);
    }

    @Test
    public void setDataSendingEnabledNoMainReporter() {
        boolean value = new Random().nextBoolean();
        setUpNoMainReporter();
        AppMetricaFacade.setDataSendingEnabled(value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setDataSendingEnabled(value);
    }

    @Test
    public void setDataSendingEnabledInitialized() {
        boolean value = new Random().nextBoolean();
        setUpInitialized();
        AppMetricaFacade.setDataSendingEnabled(value);
        verify(impl).setDataSendingEnabled(value);
    }

    @Test
    public void putErrorEnvironmentValueNoInstance() {
        final String key = "key";
        final String value = "value";
        setUpNoInstance();
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putErrorEnvironmentValueFutureNotDone() {
        final String key = "key";
        final String value = "value";
        setUpFutureNotDone();
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putErrorEnvironmentValueNoMainReporter() {
        final String key = "key";
        final String value = "value";
        setUpNoMainReporter();
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putErrorEnvironmentValueInitialized() {
        final String key = "key";
        final String value = "value";
        setUpInitialized();
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
        verify(impl).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValueNoInstance() {
        final String key = "key";
        final String value = "value";
        setUpNoInstance();
        AppMetricaFacade.putAppEnvironmentValue(key, value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).putAppEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValueFutureNotDone() {
        final String key = "key";
        final String value = "value";
        setUpFutureNotDone();
        AppMetricaFacade.putAppEnvironmentValue(key, value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).putAppEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValueNoMainReporter() {
        final String key = "key";
        final String value = "value";
        setUpNoMainReporter();
        AppMetricaFacade.putAppEnvironmentValue(key, value);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).putAppEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValueInitialized() {
        final String key = "key";
        final String value = "value";
        setUpInitialized();
        AppMetricaFacade.putAppEnvironmentValue(key, value);
        verify(impl).putAppEnvironmentValue(key, value);
    }

    @Test
    public void clearAppEnvironmentNoInstance() {
        setUpNoInstance();
        AppMetricaFacade.clearAppEnvironment();
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).clearAppEnvironment();
    }

    @Test
    public void clearAppEnvironmentFutureNotDone() {
        setUpFutureNotDone();
        AppMetricaFacade.clearAppEnvironment();
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).clearAppEnvironment();
    }

    @Test
    public void clearAppEnvironmentNoMainReporter() {
        setUpNoMainReporter();
        AppMetricaFacade.clearAppEnvironment();
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).clearAppEnvironment();
    }

    @Test
    public void clearAppEnvironmentInitialized() {
        setUpInitialized();
        AppMetricaFacade.clearAppEnvironment();
        verify(impl).clearAppEnvironment();
    }

    @Test
    public void setUserProfileIDNoInstance() {
        String userProfileID = "user_profile_id";
        setUpNoInstance();
        AppMetricaFacade.setUserProfileID(userProfileID);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setUserProfileID(userProfileID);
    }

    @Test
    public void setUserProfileIDFutureNotDone() {
        String userProfileID = "user_profile_id";
        setUpFutureNotDone();
        AppMetricaFacade.setUserProfileID(userProfileID);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setUserProfileID(userProfileID);
    }

    @Test
    public void setUserProfileIDNoMainReporter() {
        String userProfileID = "user_profile_id";
        setUpNoMainReporter();
        AppMetricaFacade.setUserProfileID(userProfileID);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).setUserProfileID(userProfileID);
    }

    @Test
    public void setUserProfileIDInitialized() {
        String userProfileID = "user_profile_id";
        setUpInitialized();
        AppMetricaFacade.setUserProfileID(userProfileID);
        verify(impl).setUserProfileID(userProfileID);
    }

    @Test
    public void addAutoCollectedDataSubscriberNoInstance() {
        String subscriber = "subscriber";
        setUpNoInstance();
        AppMetricaFacade.addAutoCollectedDataSubscriber(subscriber);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).addAutoCollectedDataSubscriber(subscriber);
    }

    @Test
    public void addAutoCollectedDataSubscriberFutureNotDone() {
        String subscriber = "subscriber";
        setUpFutureNotDone();
        AppMetricaFacade.addAutoCollectedDataSubscriber(subscriber);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).addAutoCollectedDataSubscriber(subscriber);
    }

    @Test
    public void addAutoCollectedDataSubscriberNoMainReporter() {
        String subscriber = "subscriber";
        setUpNoMainReporter();
        AppMetricaFacade.addAutoCollectedDataSubscriber(subscriber);
        verify(clientServiceLocatorRule.mDefaultOneShotMetricaConfig).addAutoCollectedDataSubscriber(subscriber);
    }

    @Test
    public void addAutoCollectedDataSubscriberInitialized() {
        String subscriber = "subscriber";
        setUpInitialized();
        AppMetricaFacade.addAutoCollectedDataSubscriber(subscriber);
        verify(impl).addAutoCollectedDataSubscriber(subscriber);
    }

    private void setUpNoInstance() {
        AppMetricaFacade.killInstance();
    }

    private void setUpFutureNotDone() {
        ClientExecutorProvider clientExecutorProvider = mock(ClientExecutorProvider.class);
        when(clientExecutorProvider.getDefaultExecutor()).thenReturn(mock(IHandlerExecutor.class));
        when(clientExecutorProvider.getCoreInitThread(any())).thenReturn(mock(Thread.class));
        when(clientServiceLocatorRule.instance.getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        AppMetricaFacade.getInstance(context);
    }

    private void setUpNoMainReporter() {
        AppMetricaFacade.getInstance(context);
    }

    private void setUpInitialized() {
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(impl.getMainReporterApiConsumerProvider()).thenReturn(mainReporterApiConsumerProvider);
        AppMetricaFacade.getInstance(context);
        verify(ClientServiceLocator.getInstance().getClientExecutorProvider())
            .getCoreInitThread(runnableArgumentCaptor.capture());
        runnableArgumentCaptor.getValue().run();
    }
}
