package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Handler;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaImplActivateTest extends CommonTest {

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
    @Mock
    private PublicLogger mPublicLogger;
    @Mock
    private PublicLogger mPublicAnonymousLogger;
    @Mock
    private AppOpenWatcher appOpenWatcher;

    final String mApiKey = TestsData.UUID_API_KEY;
    private AppMetricaConfig originalConfig = AppMetricaConfig.newConfigBuilder(mApiKey)
            .withAppBuildNumber(76576)
            .build();
    private AppMetricaConfig mConfig = AppMetricaConfig.newConfigBuilder(mApiKey).build();
    private Map<String, String> mClientClids = new HashMap<String, String>();
    private AppMetricaImpl mAppMetrica;
    private Context mContext;

    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();

    @Rule
    public final MockedStaticRule<LoggerStorage> sLoggerStorage = new MockedStaticRule<>(LoggerStorage.class);

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
        when(mFieldsProvider.createStartupHelper(mContext, mReportsHandler, mPreferences, mHandler))
                .thenReturn(mStartupHelper);
        when(mFieldsProvider.createReporterFactory(mContext, mProcessConfiguration, mReportsHandler, mHandler, mStartupHelper)).thenReturn(mReporterFactory);
        when(mCore.getMetricaHandler()).thenReturn(mHandler);
        when(mCore.getExecutor()).thenReturn(mCommonExecutor);
        when(mCore.getAppOpenWatcher()).thenReturn(appOpenWatcher);
        AppMetricaFacade.killInstance();
        ClientExecutorProvider clientExecutorProvider = mock(ClientExecutorProvider.class);
        when(mClientServiceLocatorRule.instance.getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        when(mReporterFactory.buildMainReporter(any(AppMetricaConfig.class), anyBoolean())).thenReturn(mMainReporter);

        when(LoggerStorage.getAnonymousPublicLogger()).thenReturn(mPublicAnonymousLogger);
        when(LoggerStorage.getOrCreatePublicLogger(mApiKey)).thenReturn(mPublicLogger);

        mAppMetrica = new AppMetricaImpl(
                mContext,
                mCore,
                mPreferences,
                mFieldsProvider,
                mClientServiceLocatorRule.instance
        );
    }

    @Test
    public void logsEnabled() {
        AppMetricaConfig originalConfig = AppMetricaConfig.newConfigBuilder(mApiKey).build();
        mAppMetrica.activate(originalConfig, AppMetricaConfig.newConfigBuilder(mApiKey).withLogs().build());
        verify(mPublicLogger).setEnabled();
        verify(mPublicAnonymousLogger).setEnabled();
    }

    @Test
    public void logsDisabled() {
        mAppMetrica.activate(AppMetricaConfig.newConfigBuilder(mApiKey).withLogs().build(), mConfig);
        verify(mPublicLogger).setDisabled();
        verify(mPublicAnonymousLogger).setDisabled();
    }

    @Test
    public void activateTwice() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        mAppMetrica.activate(originalConfig, mConfig);
        verify(mReporterFactory).buildMainReporter(same(mConfig), anyBoolean());
        mAppMetrica.activate(originalConfig, mConfig);
        verify(mReporterFactory, times(1)).buildMainReporter(same(mConfig), anyBoolean());
        verify(mPublicLogger).w("Appmetrica already has been activated!");
    }

    @Test
    public void activateTwiceNoLogs() {
        when(mPublicLogger.isEnabled()).thenReturn(false);
        mAppMetrica.activate(originalConfig, mConfig);
        mAppMetrica.activate(originalConfig, mConfig);
        verify(mPublicLogger, never()).w(anyString());
    }

    @Test
    public void startupHelperInitialization() {
        List<String> customHosts = Arrays.asList("host1", "host2");
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");
        String referrer = "test referrer";
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey)
            .withCustomHosts(customHosts)
            .withAdditionalConfig("YMM_clids", clids)
            .withAdditionalConfig("YMM_preloadInfoAutoTracking", true)
            .withAdditionalConfig("YMM_distributionReferrer", referrer)
            .build();
        mAppMetrica.activate(originalConfig, config);

        verify(mStartupHelper).setPublicLogger(mPublicLogger);
        verify(mStartupHelper).setCustomHosts(customHosts);
        verify(mStartupHelper).setClids(clids);
        verify(mStartupHelper).setDistributionReferrer(referrer);
        verify(mStartupHelper).setInstallReferrerSource("api");
        verify(mStartupHelper).sendStartupIfNeeded();
    }

    @Test
    public void startupHelperInitializationEmptyConfig() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey).build();
        mAppMetrica.activate(originalConfig, config);

        verify(mStartupHelper).setPublicLogger(mPublicLogger);
        verify(mStartupHelper).setCustomHosts(null);
        verify(mStartupHelper).setClids(null);
        verify(mStartupHelper).setDistributionReferrer(null);
        verify(mStartupHelper, never()).setInstallReferrerSource(nullable(String.class));
        verify(mStartupHelper).sendStartupIfNeeded();
    }

    @Test
    public void configUpdate() {
        Random random = new Random();
        boolean needToClearEnv = random.nextBoolean();
        boolean locationTracking = random.nextBoolean();
        boolean dataSendingEnabled = random.nextBoolean();
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey)
                .withLocationTracking(locationTracking)
                .withDataSendingEnabled(dataSendingEnabled)
                .build();
        when(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig.wasAppEnvironmentCleared()).thenReturn(needToClearEnv);
        mAppMetrica.activate(originalConfig, config);

        verify(mProcessConfiguration).update(config);
        verify(mReportsHandler).updatePreActivationConfig(locationTracking, dataSendingEnabled);
    }

    @Test
    public void configUpdateEmpty() {
        boolean needToClearEnv = new Random().nextBoolean();
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey).build();
        when(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig.wasAppEnvironmentCleared()).thenReturn(needToClearEnv);
        mAppMetrica.activate(originalConfig, config);

        verify(mProcessConfiguration).update(config);
        verify(mReportsHandler).updatePreActivationConfig(null, null);
    }

    @Test
    public void mainReporterCreation() {
        boolean needToClearEnv = new Random().nextBoolean();
        when(mClientServiceLocatorRule.mDefaultOneShotMetricaConfig.wasAppEnvironmentCleared()).thenReturn(needToClearEnv);
        mAppMetrica.activate(originalConfig, mConfig);

        verify(mReporterFactory).buildMainReporter(mConfig, needToClearEnv);
        assertThat(mAppMetrica.getMainReporterApiConsumerProvider().getMainReporter()).isSameAs(mMainReporter);
    }

    @Test
    public void deeplinkConsumerIsSetToAppWatcher() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey).build();
        mAppMetrica.activate(originalConfig, config);
        verify(appOpenWatcher).setDeeplinkConsumer(any(DeeplinkConsumer.class));
    }

    @Test
    public void reporterIsSetToSessionTrackingManager() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey).build();
        mAppMetrica.activate(originalConfig, config);
        verify(mClientServiceLocatorRule.sessionsTrackingManager).setReporter(mMainReporter);
    }

    @Test
    public void requestReferrer() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(mApiKey).build();
        mAppMetrica.activate(originalConfig, config);
        verify(mReferrerHelper).maybeRequestReferrer();
    }
}
