package io.appmetrica.analytics.impl;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.modules.ModulesSeeker;
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper;
import io.appmetrica.analytics.impl.startup.Constants;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaImplTest extends CommonTest {

    @Mock
    private Handler mHandler;
    @Mock
    private DataResultReceiver mReceiver;
    @Mock
    private ProcessConfiguration mProcessConfiguration;
    @Mock
    private ReportsHandler mReportsHandler;
    @Mock
    private ICommonExecutor mCommonExecutor;
    @Mock
    private StartupHelper mStartupHelper;
    @Mock
    private PreferencesClientDbStorage mPreferences;
    @Mock
    private ReferrerHelper mReferrerHelper;
    @Mock
    private ReporterFactory mReporterFactory;
    @Mock
    private AppMetricaCore mCore;
    @Mock
    private AppMetricaImplFieldsProvider mFieldsProvider;
    @Mock
    private MainReporter mMainReporter;
    final String mApiKey = TestsData.UUID_API_KEY;
    private AppMetricaConfig originalConfig = AppMetricaConfig
            .newConfigBuilder(mApiKey)
            .withAppBuildNumber(88)
            .build();
    private AppMetricaConfig mConfig = AppMetricaConfig.newConfigBuilder(mApiKey).build();
    private Map<String, String> mClientClids = new HashMap<String, String>();
    private AppMetricaImpl mAppMetrica;
    private Context mContext;

    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();
    private ModulesSeeker modulesSeeker;
    @Rule
    public final MockedConstructionRule<ModulesSeeker> mockedConstructionRule =
        new MockedConstructionRule<>(ModulesSeeker.class, new MockedConstruction.MockInitializer<ModulesSeeker>() {
            @Override
            public void prepare(ModulesSeeker mock, MockedConstruction.Context context) throws Throwable {
                modulesSeeker = mock;
            }
        });

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mClientClids.put("clid10", "10");
        mClientClids.put("clid20", "20");
        when(mProcessConfiguration.getClientClids()).thenReturn(mClientClids);
        when(mFieldsProvider.createDataResultReceiver(same(mHandler), any(AppMetricaImpl.class))).thenReturn(mReceiver);
        when(mFieldsProvider.createProcessConfiguration(mContext, mReceiver)).thenReturn(mProcessConfiguration);
        when(mFieldsProvider.createReportsHandler(mProcessConfiguration, mContext, mCommonExecutor)).thenReturn(mReportsHandler);
        when(mFieldsProvider.createReferrerHelper(mReportsHandler, mPreferences, mHandler)).thenReturn(mReferrerHelper);
        when(mFieldsProvider.createStartupHelper(mContext, mReportsHandler, mPreferences, mHandler)).thenReturn(mStartupHelper);
        when(mFieldsProvider.createReporterFactory(mContext, mProcessConfiguration, mReportsHandler, mHandler, mStartupHelper)).thenReturn(mReporterFactory);
        when(mCore.getMetricaHandler()).thenReturn(mHandler);
        when(mCore.getExecutor()).thenReturn(mCommonExecutor);
        when(mCore.getAppOpenWatcher()).thenReturn(mock(AppOpenWatcher.class));
        when(mReporterFactory.buildMainReporter(any(AppMetricaConfig.class), anyBoolean())).thenReturn(mMainReporter);
        AppMetricaFacade.killInstance();
        ClientExecutorProvider clientExecutorProvider = mock(ClientExecutorProvider.class);
        when(mClientServiceLocatorRule.instance.getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        mAppMetrica = new AppMetricaImpl(
                mContext,
                mCore,
                mPreferences,
                mFieldsProvider,
                mClientServiceLocatorRule.instance
        );
    }

    @Test
    public void constructor() {
        verify(mFieldsProvider).createDataResultReceiver(mHandler, mAppMetrica);
        verify(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig).setReportsHandler(mReportsHandler);
        verify(mClientServiceLocatorRule.modulesController).initClientSide(ArgumentMatchers.<ClientContext>any());
        InOrder inOrder = inOrder(modulesSeeker, ClientServiceLocator.getInstance().getModulesController());
        inOrder.verify(modulesSeeker).discoverClientModules();
        inOrder.verify(ClientServiceLocator.getInstance().getModulesController())
            .initClientSide(ArgumentMatchers.<ClientContext>any());
    }

    @Test
    public void getMainReporterBeforeActivation() {
        assertThat(mAppMetrica.getMainReporterApiConsumerProvider()).isNull();
    }

    @Test
    public void onReceiveResult() {
        final int code = 77;
        final Bundle bundle = mock(Bundle.class);
        mAppMetrica.onReceiveResult(code, bundle);
        verify(mStartupHelper).processResultFromResultReceiver(bundle);
    }

    @Test
    public void requestDeferredDeeplinkParameters() {
        DeferredDeeplinkParametersListener listener = mock(DeferredDeeplinkParametersListener.class);
        mAppMetrica.requestDeferredDeeplinkParameters(listener);
        verify(mReferrerHelper).requestDeferredDeeplinkParameters(listener);
    }

    @Test
    public void requestDeferredDeeplink() {
        DeferredDeeplinkListener listener = mock(DeferredDeeplinkListener.class);
        mAppMetrica.requestDeferredDeeplink(listener);
        verify(mReferrerHelper).requestDeferredDeeplink(listener);
    }

    @Test
    public void activate() {
        final AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey).build();
        mAppMetrica.activate(config, config);
    }

    @Test
    public void activateReporter() {
        ReporterConfig config = mock(ReporterConfig.class);
        mAppMetrica.activateReporter(config);
        verify(mReporterFactory).activateReporter(config);
    }

    @Test
    public void getReporter() {
        IReporterExtended reporter = mock(IReporterExtended.class);
        ReporterConfig config = mock(ReporterConfig.class);
        when(mReporterFactory.getOrCreateReporter(config)).thenReturn(reporter);
        assertThat(mAppMetrica.getReporter(config)).isSameAs(reporter);
    }

    @Test
    public void getDeviceId() {
        String deviceId = "768768";
        when(mStartupHelper.getDeviceId()).thenReturn(deviceId);
        assertThat(mAppMetrica.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    public void getClids() {
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("key1", "value1");
        clids.put("key2", "value2");
        when(mStartupHelper.getClids()).thenReturn(clids);
        assertThat(mAppMetrica.getClids()).isEqualTo(clids);
    }

    @Test
    public void getCachedAdvIdentifiers() {
        AdvIdentifiersResult result = mock(AdvIdentifiersResult.class);
        when(mStartupHelper.getCachedAdvIdentifiers()).thenReturn(result);
        assertThat(mAppMetrica.getCachedAdvIdentifiers()).isSameAs(result);
    }

    @Test
    public void requestStartupParamsWithStartupParamsCallback() {
        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        List<String> params = Arrays.asList(Constants.StartupParamsCallbackKeys.DEVICE_ID, Constants.StartupParamsCallbackKeys.UUID);
        mAppMetrica.requestStartupParams(callback, params);
        verify(mStartupHelper).requestStartupParams(callback, params, mClientClids);
    }

    @Test
    public void putErrorEnvironmentValue() {
        final String key = "key";
        final String value = "value";
        mAppMetrica.activate(originalConfig, mConfig);
        mAppMetrica.putErrorEnvironmentValue(key, value);
        verify(mMainReporter).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void putAppEnvironmentValue() {
        final String key = "key";
        final String value = "value";
        mAppMetrica.activate(originalConfig, mConfig);
        mAppMetrica.putAppEnvironmentValue(key, value);
        verify(mMainReporter).putAppEnvironmentValue(key, value);
    }

    @Test
    public void clearAppEnvironment() {
        mAppMetrica.activate(originalConfig, mConfig);
        mAppMetrica.clearAppEnvironment();
        verify(mMainReporter).clearAppEnvironment();
    }

    @Test
    public void setDataSendingEnabled() {
        final boolean value = new Random().nextBoolean();
        mAppMetrica.activate(originalConfig, mConfig);
        mAppMetrica.setDataSendingEnabled(value);
        verify(mMainReporter).setDataSendingEnabled(value);
    }

    @Test
    public void setLocationTracking() {
        final boolean value = new Random().nextBoolean();
        mAppMetrica.activate(originalConfig, mConfig);
        mAppMetrica.setLocationTracking(value);
        verify(mMainReporter).setLocationTracking(value);
    }

    @Test
    public void setLocation() {
        Location location = mock(Location.class);
        mAppMetrica.activate(originalConfig, mConfig);
        mAppMetrica.setLocation(location);
        verify(mMainReporter).setLocation(location);
    }

    @Test
    public void getReporterFactory() {
        assertThat(mAppMetrica.getReporterFactory()).isSameAs(mReporterFactory);
    }

    @Test
    public void setUserProfileID() {
        String userProfileID = "user_profile_id";
        mAppMetrica.activate(originalConfig, mConfig);
        mAppMetrica.setUserProfileID(userProfileID);
        verify(mMainReporter).setUserProfileID(userProfileID);
    }

    @Test
    public void getFeatures() {
        FeaturesResult features = mock(FeaturesResult.class);
        when(mStartupHelper.getFeatures()).thenReturn(features);
        assertThat(mAppMetrica.getFeatures()).isSameAs(features);
    }
}
