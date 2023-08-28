package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.MainReporterComponentId;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ComponentIdTest extends CommonTest {

    @Test
    public void testMainReporterComponentId() {
        final ComponentId componentId = new MainReporterComponentId(TestsData.APP_PACKAGE, UUID.randomUUID().toString());
        assertThat(componentId.toString()).isEqualTo(TestsData.APP_PACKAGE);
    }

    @Test
    public void testCustomReporterComponentId() {
        ClientDescription description = ClientDescription.fromClientConfiguration(
                createMockedConfiguration(TestsData.UUID_API_KEY, CounterConfigurationReporterType.MANUAL)); //todo ClientDescription logic tested through ComponentID
        final ComponentId componentId = new ComponentId(description.getPackageName(), description.getApiKey());
        assertThat(componentId.toString()).isEqualTo(
                RuntimeEnvironment.getApplication().getPackageName() + "_" + TestsData.UUID_API_KEY);
    }

    @Test
    public void testMainReporterComponentIdAnonymizedString() {
        final ComponentId componentId = new MainReporterComponentId(TestsData.APP_PACKAGE, UUID.randomUUID().toString());
        assertThat(componentId.toStringAnonymized()).isEqualTo(TestsData.APP_PACKAGE);
    }

    @Test
    public void testCustomReporterComponentIdAnonymizedString() {
        final String apiKey = "a3a0ed4a-f81a-4b87-966c-72500fd6c747";
        ClientDescription description = ClientDescription.fromClientConfiguration(
                createMockedConfiguration(apiKey, CounterConfigurationReporterType.MANUAL));
        final ComponentId componentId = new ComponentId(description.getPackageName(), description.getApiKey());
        assertThat(componentId.toStringAnonymized()).isEqualTo(
                RuntimeEnvironment.getApplication().getPackageName() + "_a3a0ed4a-xxxx-xxxx-xxxx-xxxxxxxxc747");
    }

    @Test
    public void toStringForNullApiKey() {
        ComponentId componentId = new ComponentId("test.package.name", null);
        assertThat(componentId.toString()).isEqualTo("test.package.name_null");
    }

    @Test
    public void toStringForNonNullApiKey() {
        ComponentId componentId = new ComponentId("test.package.name", "test-api-key");
        assertThat(componentId.toString()).isEqualTo("test.package.name_test-api-key");
    }

    public static ClientConfiguration createMockedConfiguration(String apiKey, CounterConfigurationReporterType reporterType) {
        CounterConfiguration configuration = mock(CounterConfiguration.class);
        doReturn(reporterType).when(configuration).getReporterType();
        doReturn(apiKey).when(configuration).getApiKey();
        return new ClientConfiguration(new ProcessConfiguration(RuntimeEnvironment.getApplication(), mock(DataResultReceiver.class)), configuration);
    }

}
