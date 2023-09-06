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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
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
    private AppMetricaImpl mImpl;
    @Mock
    private IReporterExtended someReporter;

    private Context mContext;

    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();

    @Rule
    public final MockedConstructionRule<AppMetricaCoreComponentsProvider> mockedCoreProvider =
            new MockedConstructionRule<>(AppMetricaCoreComponentsProvider.class, new MockedConstruction.MockInitializer<AppMetricaCoreComponentsProvider>() {

                @Override
                public void prepare(AppMetricaCoreComponentsProvider mock, MockedConstruction.Context context) {
                    if (context.arguments().isEmpty()) {
                        when(mock.getCore(same(mContext), any(ClientExecutorProvider.class)))
                                .thenReturn(core);
                        when(mock.getImpl(mContext, core)).thenReturn(mImpl);
                    }
                }
            });

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        AppMetricaFacade.killInstance();
        doReturn(someReporter).when(mImpl).getReporter(ArgumentMatchers.<ReporterConfig>any());
    }

    @Test
    public void setLocationNoInstance() {
        Location location = mock(Location.class);
        setUpNoInstance();
        AppMetricaFacade.setLocation(location);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocation(location);
    }

    @Test
    public void setLocationFutureNotDone() {
        Location location = mock(Location.class);
        setUpFutureNotDone();
        AppMetricaFacade.setLocation(location);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocation(location);
    }

    @Test
    public void setLocationNoMainReporter() {
        Location location = mock(Location.class);
        setUpNoMainReporter();
        AppMetricaFacade.setLocation(location);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocation(location);
    }

    @Test
    public void setLocationInitialized() {
        Location location = mock(Location.class);
        setUpInitialized();
        AppMetricaFacade.setLocation(location);
        verify(mImpl).setLocation(location);
    }

    @Test
    public void setLocationTrackingNoInstance() {
        boolean value = new Random().nextBoolean();
        setUpNoInstance();
        AppMetricaFacade.setLocationTracking(value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocationTracking(value);
    }

    @Test
    public void setLocationTrackingFutureNotDone() {
        boolean value = new Random().nextBoolean();
        setUpFutureNotDone();
        AppMetricaFacade.setLocationTracking(value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocationTracking(value);
    }

    @Test
    public void setLocationTrackingNoMainReporter() {
        boolean value = new Random().nextBoolean();
        setUpNoMainReporter();
        AppMetricaFacade.setLocationTracking(value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setLocationTracking(value);
    }

    @Test
    public void setLocationTrackingInitialized() {
        boolean value = new Random().nextBoolean();
        setUpInitialized();
        AppMetricaFacade.setLocationTracking(value);
        verify(mImpl).setLocationTracking(value);
    }

    @Test
    public void setDataSendingEnabledNoInstance() {
        boolean value = new Random().nextBoolean();
        setUpNoInstance();
        AppMetricaFacade.setDataSendingEnabled(value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setDataSendingEnabled(value);
    }

    @Test
    public void setDataSendingEnabledFutureNotDone() {
        boolean value = new Random().nextBoolean();
        setUpFutureNotDone();
        AppMetricaFacade.setDataSendingEnabled(value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setDataSendingEnabled(value);
    }

    @Test
    public void setDataSendingEnabledNoMainReporter() {
        boolean value = new Random().nextBoolean();
        setUpNoMainReporter();
        AppMetricaFacade.setDataSendingEnabled(value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setDataSendingEnabled(value);
    }

    @Test
    public void setDataSendingEnabledInitialized() {
        boolean value = new Random().nextBoolean();
        setUpInitialized();
        AppMetricaFacade.setDataSendingEnabled(value);
        verify(mImpl).setDataSendingEnabled(value);
    }

    @Test
    public void putErrorEnvironmentValueNoInstance() {
        final String key = "key";
        final String value = "value";
        setUpNoInstance();
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putErrorEnvironmentValueFutureNotDone() {
        final String key = "key";
        final String value = "value";
        setUpFutureNotDone();
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putErrorEnvironmentValueNoMainReporter() {
        final String key = "key";
        final String value = "value";
        setUpNoMainReporter();
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putErrorEnvironmentValueInitialized() {
        final String key = "key";
        final String value = "value";
        setUpInitialized();
        AppMetricaFacade.putErrorEnvironmentValue(key, value);
        verify(mImpl).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValueNoInstance() {
        final String key = "key";
        final String value = "value";
        setUpNoInstance();
        AppMetricaFacade.putAppEnvironmentValue(key, value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).putAppEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValueFutureNotDone() {
        final String key = "key";
        final String value = "value";
        setUpFutureNotDone();
        AppMetricaFacade.putAppEnvironmentValue(key, value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).putAppEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValueNoMainReporter() {
        final String key = "key";
        final String value = "value";
        setUpNoMainReporter();
        AppMetricaFacade.putAppEnvironmentValue(key, value);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).putAppEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValueInitialized() {
        final String key = "key";
        final String value = "value";
        setUpInitialized();
        AppMetricaFacade.putAppEnvironmentValue(key, value);
        verify(mImpl).putAppEnvironmentValue(key, value);
    }

    @Test
    public void clearAppEnvironmentNoInstance() {
        setUpNoInstance();
        AppMetricaFacade.clearAppEnvironment();
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).clearAppEnvironment();
    }

    @Test
    public void clearAppEnvironmentFutureNotDone() {
        setUpFutureNotDone();
        AppMetricaFacade.clearAppEnvironment();
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).clearAppEnvironment();
    }

    @Test
    public void clearAppEnvironmentNoMainReporter() {
        setUpNoMainReporter();
        AppMetricaFacade.clearAppEnvironment();
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).clearAppEnvironment();
    }

    @Test
    public void clearAppEnvironmentInitialized() {
        setUpInitialized();
        AppMetricaFacade.clearAppEnvironment();
        verify(mImpl).clearAppEnvironment();
    }

    @Test
    public void setUserProfileIDNoInstance() {
        String userProfileID = "user_profile_id";
        setUpNoInstance();
        AppMetricaFacade.setUserProfileID(userProfileID);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setUserProfileID(userProfileID);
    }

    @Test
    public void setUserProfileIDFutureNotDone() {
        String userProfileID = "user_profile_id";
        setUpFutureNotDone();
        AppMetricaFacade.setUserProfileID(userProfileID);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setUserProfileID(userProfileID);
    }

    @Test
    public void setUserProfileIDNoMainReporter() {
        String userProfileID = "user_profile_id";
        setUpNoMainReporter();
        AppMetricaFacade.setUserProfileID(userProfileID);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setUserProfileID(userProfileID);
    }

    @Test
    public void setUserProfileIDInitialized() {
        String userProfileID = "user_profile_id";
        setUpInitialized();
        AppMetricaFacade.setUserProfileID(userProfileID);
        verify(mImpl).setUserProfileID(userProfileID);
    }

    private void setUpNoInstance() {
        AppMetricaFacade.killInstance();
    }

    private void setUpFutureNotDone() {
        ClientExecutorProvider clientExecutorProvider = mock(ClientExecutorProvider.class);
        when(clientExecutorProvider.getDefaultExecutor()).thenReturn(mock(IHandlerExecutor.class));
        when(mClientServiceLocatorRule.instance.getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        AppMetricaFacade.getInstance(mContext);
    }

    private void setUpNoMainReporter() {
        AppMetricaFacade.getInstance(mContext);
    }

    private void setUpInitialized() {
        when(mImpl.getMainReporterApiConsumerProvider()).thenReturn(mainReporterApiConsumerProvider);
        AppMetricaFacade.getInstance(mContext);
    }
}
