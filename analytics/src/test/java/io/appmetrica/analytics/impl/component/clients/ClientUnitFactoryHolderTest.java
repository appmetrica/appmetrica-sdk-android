package io.appmetrica.analytics.impl.component.clients;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ClientUnitFactoryHolderTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Report type: {0} should return {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {CounterConfigurationReporterType.COMMUTATION, MainCommutationClientUnitFactory.class},
                {CounterConfigurationReporterType.MAIN, MainReporterClientFactory.class},
                {CounterConfigurationReporterType.MANUAL, ReporterClientUnitFactory.class},
                {CounterConfigurationReporterType.SELF_SDK, SelfSdkReportingFactory.class},
                {CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN, SelfDiagnosticMainClientUnitFactory.class},
                {CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL, SelfDiagnosticReporterClientUnitFactory.class},
        });
    }

    @NonNull
    private final Class mFactoryClass;
    private final ClientDescription mClientDescription;
    private final ClientUnitFactoryHolder mClientUnitFactoryHolder;

    public ClientUnitFactoryHolderTest(@NonNull CounterConfigurationReporterType reporterType,
                                       @NonNull Class factoryClass) {
        mFactoryClass = factoryClass;
        mClientDescription = mock(ClientDescription.class);
        when(mClientDescription.getReporterType()).thenReturn(reporterType);
        mClientUnitFactoryHolder = new ClientUnitFactoryHolder();
    }

    @Test
    public void test() {
        assertThat(mClientUnitFactoryHolder.getClientUnitFactory(mClientDescription)).isExactlyInstanceOf(mFactoryClass);
    }

}
