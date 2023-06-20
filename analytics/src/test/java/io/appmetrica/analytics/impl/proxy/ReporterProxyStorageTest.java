package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReporterProxyStorageTest extends CommonTest {

    private Context mContext;
    private final String mApiKey = TestsData.generateApiKey();

    @Mock
    private ICommonExecutor mExecutor;

    private ReporterProxyStorage storage;

    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(mClientServiceLocatorRule.instance.getApiProxyExecutor()).thenReturn(mExecutor);
        storage = new ReporterProxyStorage(mExecutor, mock(AppMetricaFacadeProvider.class));
    }

    @After
    public void tearDown() {
        AppMetricaFacade.killInstance();
    }

    @Test
    public void testGetOrCreateApiKey() {
        ReporterExtendedProxy proxy = storage.getOrCreate(mContext, mApiKey);
        assertThat(proxy).isNotNull();
        assertThat(storage.getOrCreate(mContext, mApiKey)).isEqualTo(proxy);
        assertThat(storage.getOrCreate(mContext, TestsData.generateApiKey())).isNotNull().isNotEqualTo(proxy);
    }

    @Test
    public void testGetOrCreateConfig() {
        ReporterConfig config = ReporterConfig.newConfigBuilder(mApiKey).build();
        ReporterExtendedProxy proxy = storage.getOrCreate(mContext, config);
        assertThat(proxy).isNotNull();
        assertThat(storage.getOrCreate(mContext, config)).isEqualTo(proxy);
        assertThat(storage.getOrCreate(mContext, ReporterConfig.newConfigBuilder(TestsData.generateApiKey()).build()))
                .isNotNull()
                .isNotEqualTo(proxy);
    }

    @Test
    public void testSameApiKeyThenConfig() {
        ReporterExtendedProxy proxy = storage.getOrCreate(mContext, mApiKey);
        assertThat(proxy).isNotNull();
        assertThat(storage.getOrCreate(mContext, ReporterConfig.newConfigBuilder(mApiKey).build())).isEqualTo(proxy);
    }

    @Test
    public void testSameApiKeyDifferentConfigs() {
        ReporterExtendedProxy proxy = storage.getOrCreate(mContext, ReporterConfig.newConfigBuilder(mApiKey).withDispatchPeriodSeconds(20).build());
        assertThat(proxy).isNotNull();
        assertThat(storage.getOrCreate(mContext, ReporterConfig.newConfigBuilder(mApiKey).withDispatchPeriodSeconds(10).withLogs().build())).isEqualTo(proxy);
    }
}
