package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashClient;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.MapTrimmers;
import io.appmetrica.analytics.impl.utils.process.ProcessNameProvider;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

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
    protected ProcessNameProvider processNameProvider;
    @Mock
    protected ProcessConfiguration mProcessConfiguration;

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = contextRule.getContext();

        when(mReporterEnvironment.getReporterConfiguration()).thenReturn(mCounterConfiguration);
        when(mCounterConfiguration.getApiKey()).thenReturn(apiKey);
        mMapLimitation = new MapTrimmers(MapTrimmers.DEFAULT_MAP_MAX_SIZE,
            MapTrimmers.DEFAULT_KEY_MAX_LENGTH, MapTrimmers.DEFAULT_VALUE_MAX_LENGTH, "", mPublicLogger);
        mEventLimitationProcessor = mock(EventLimitationProcessor.class);
    }

    public static String randomString() {
        return new RandomStringGenerator(new Random().nextInt(100) + 1).nextString();
    }
}
