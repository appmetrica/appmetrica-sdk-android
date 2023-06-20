package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CommutationReporterEnvironmentTest extends ReporterEnvironmentTest {

    @Test
    public void testReporterType() {
        assertThat(new CommutationReporterEnvironment(
                        new ProcessConfiguration(RuntimeEnvironment.getApplication(), null)
                ).getReporterConfiguration().getReporterType()
        ).isEqualTo(CounterConfigurationReporterType.COMMUTATION);
    }
}
