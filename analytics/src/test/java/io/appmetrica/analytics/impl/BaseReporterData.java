package io.appmetrica.analytics.impl;

import android.app.Application;
import android.content.Context;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashClient;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.MapTrimmers;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.UUID;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class BaseReporterData extends CommonTest {

    protected String apiKey = UUID.randomUUID().toString();

    protected Context mContext;
    @Mock
    protected ReportsHandler mReportsHandler;
    @Mock
    protected NativeCrashClient nativeCrashClient;
    @Mock
    protected ReporterEnvironment mReporterEnvironment;
    @Mock
    protected CounterConfiguration mCounterConfiguration;
    protected Application mApplication;
    protected EventLimitationProcessor mEventLimitationProcessor;
    protected MapTrimmers mMapLimitation;
    @Mock
    protected KeepAliveHandler mKeepAliveHandler;
    @Mock
    protected PublicLogger mPublicLogger;
    protected AppMetricaConfig mConfig = AppMetricaConfig.newConfigBuilder(apiKey).build();
    @Mock
    protected AppStatusMonitor mAppStatusMonitor;
    @Mock
    protected StartupHelper mStartupHelper;
    @Mock
    protected ProcessDetector processDetector;
    @Mock
    protected ProcessConfiguration mProcessConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();

        when(mReporterEnvironment.getReporterConfiguration()).thenReturn(mCounterConfiguration);
        when(mCounterConfiguration.getApiKey()).thenReturn(apiKey);
        mMapLimitation = new MapTrimmers(MapTrimmers.DEFAULT_MAP_MAX_SIZE,
            MapTrimmers.DEFAULT_KEY_MAX_LENGTH, MapTrimmers.DEFAULT_VALUE_MAX_LENGTH, "", mPublicLogger);
        mEventLimitationProcessor = mock(EventLimitationProcessor.class);
        mApplication = spy(RuntimeEnvironment.getApplication());
    }
}
