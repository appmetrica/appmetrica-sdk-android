package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class SelfDiagnosticReporterTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Report type: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {CounterConfigurationReporterType.COMMUTATION, null},
                {CounterConfigurationReporterType.MAIN, CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN},
                {CounterConfigurationReporterType.MANUAL, CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL},
                {CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN, null},
                {CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL, null}
        });
    }

    private final SelfProcessReporter selfProcessReporter;
    private final String apiKey = UUID.randomUUID().toString();
    private final Context context;
    private final SelfDiagnosticReporter selfDiagnosticReporter;
    @Nullable
    private final CounterConfigurationReporterType newReporterType;

    public SelfDiagnosticReporterTest(@NonNull CounterConfigurationReporterType originalReporterType,
                                      @Nullable CounterConfigurationReporterType newReporterType) {
        context = RuntimeEnvironment.getApplication();
        selfProcessReporter = mock(SelfProcessReporter.class);
        this.newReporterType = newReporterType;
        selfDiagnosticReporter = new SelfDiagnosticReporter(apiKey, context, originalReporterType, selfProcessReporter);
    }

    @Test
    public void test() {
        ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
        selfDiagnosticReporter.reportEvent(new CounterReport());
        if (newReporterType != null) {
            verify(selfProcessReporter).reportData(
                    eq(AppMetricaServiceDataReporter.TYPE_CORE),
                    bundleCaptor.capture()
            );
            ClientConfiguration env = ClientConfiguration.fromBundle(context, bundleCaptor.getValue());
            assertThat(env.getReporterConfiguration().getReporterType()).isEqualTo(newReporterType);
            assertThat(env.getReporterConfiguration().getApiKey()).isEqualTo(apiKey);
        } else {
            verifyNoMoreInteractions(selfProcessReporter);
        }
    }
}
