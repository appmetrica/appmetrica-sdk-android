package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientDescriptionTest extends CommonTest {

    private final String mPackageName = "package.name";
    private final String mApiKey = UUID.randomUUID().toString();
    private final int mPid = 222555;
    private final String mPsid = "333666";
    private final CounterConfigurationReporterType mReporterType = CounterConfigurationReporterType.MANUAL;
    @Mock
    private ProcessConfiguration mProcessConfiguration;
    @Mock
    private CounterConfiguration mCounterConfiguration;
    private ClientConfiguration mClientConfiguration;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mCounterConfiguration.getApiKey()).thenReturn(mApiKey);
        when(mProcessConfiguration.getPackageName()).thenReturn(mPackageName);
        when(mProcessConfiguration.getProcessID()).thenReturn(mPid);
        when(mProcessConfiguration.getProcessSessionID()).thenReturn(mPsid);
        when(mCounterConfiguration.getReporterType()).thenReturn(mReporterType);
        mClientConfiguration = new ClientConfiguration(mProcessConfiguration, mCounterConfiguration);
    }

    @Test
    public void testGetParametersFromConfig() {
        ClientDescription description = ClientDescription.fromClientConfiguration(
                mClientConfiguration);
        assertThat(description.getApiKey()).isEqualTo(mApiKey);
        assertThat(description.getPackageName()).isEqualTo(mPackageName);
        assertThat(description.getProcessID()).isEqualTo(mPid);
        assertThat(description.getProcessSessionID()).isEqualTo(mPsid);
        assertThat(description.getReporterType()).isEqualTo(mReporterType);
    }

    @Test
    public void testConstructor() {
        ClientDescription description = new ClientDescription(mApiKey, mPackageName, mPid, mPsid, mReporterType);
        assertThat(description.getApiKey()).isEqualTo(mApiKey);
        assertThat(description.getPackageName()).isEqualTo(mPackageName);
        assertThat(description.getProcessID()).isEqualTo(mPid);
        assertThat(description.getProcessSessionID()).isEqualTo(mPsid);
        assertThat(description.getReporterType()).isEqualTo(mReporterType);
    }
}
