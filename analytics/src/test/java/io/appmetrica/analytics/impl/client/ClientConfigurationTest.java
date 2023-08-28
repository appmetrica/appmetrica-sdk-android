package io.appmetrica.analytics.impl.client;

import android.content.Context;
import android.os.Bundle;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientConfigurationTest extends CommonTest {

    @Mock
    private CounterConfiguration counterConfiguration;
    @Mock
    private ProcessConfiguration processConfiguration;
    @Mock
    private Bundle bundle;
    private final String packageName = "packagename";
    private Context context;

    @Rule
    public MockedStaticRule<CounterConfiguration> clientConfigurationMockedStaticRule =
        new MockedStaticRule<>(CounterConfiguration.class);

    @Rule
    public MockedStaticRule<ProcessConfiguration> processConfigurationMockedStaticRule =
        new MockedStaticRule<>(ProcessConfiguration.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(CounterConfiguration.fromBundle(bundle)).thenReturn(counterConfiguration);
        when(ProcessConfiguration.fromBundle(bundle)).thenReturn(processConfiguration);
        when(processConfiguration.getSdkApiLevel()).thenReturn(BuildConfig.API_LEVEL);
        when(processConfiguration.getPackageName()).thenReturn(packageName);
        context = TestUtils.createMockedContext();
        when(context.getPackageName()).thenReturn(packageName);
    }

    @Test
    public void testInvalidNull() {
        assertThat(ClientConfiguration.fromBundle(context, null)).isNull();
    }

    @Test
    public void testInvalidNullProcessConfiguration() {
        when(ProcessConfiguration.fromBundle(bundle)).thenReturn(null);
        assertThat(ClientConfiguration.fromBundle(context, bundle)).isNull();
    }

    @Test
    public void testInvalidDifferentPackageName() {
        when(processConfiguration.getPackageName()).thenReturn("another package name");
        assertThat(ClientConfiguration.fromBundle(context, bundle)).isNull();
    }

    @Test
    public void testInvalidDifferentSdkApiLevel() {
        when(processConfiguration.getSdkApiLevel()).thenReturn(BuildConfig.API_LEVEL - 1);
        assertThat(ClientConfiguration.fromBundle(context, bundle)).isNull();
    }

    @Test
    public void testValid() {
        assertThat(ClientConfiguration.fromBundle(context, bundle)).isNotNull();
    }
}
