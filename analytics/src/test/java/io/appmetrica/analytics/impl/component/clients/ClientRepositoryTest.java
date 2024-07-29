package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientRepositoryTest extends CommonTest {

    @Mock
    private ComponentsRepository mComponentsRepository;
    @Mock
    private ClientUnitFactoryHolder mClientUnitFactoryHolder;
    @Mock
    private ClientUnitFactory mFactory;
    @Mock
    private ClientUnit mCreatedClientUnit;
    private Context mContext;

    private ClientRepository mClientRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(mClientUnitFactoryHolder.getClientUnitFactory(any(ClientDescription.class))).thenReturn(mFactory);
        mClientRepository = new ClientRepository(mContext, mComponentsRepository, mClientUnitFactoryHolder);
    }

    @Test
    public void testCreateNewClient() {
        ClientDescription description = createDefaultMockedDescription(
                UUID.randomUUID().toString(), "com.test.package", CounterConfigurationReporterType.MAIN);
        CommonArguments counterConfiguration = CommonArgumentsTestUtils.createMockedArguments();
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description, counterConfiguration)
        ).thenReturn(mCreatedClientUnit);
        assertThat(mClientRepository.getOrCreateClient(description, counterConfiguration)).isEqualTo(mCreatedClientUnit);
        assertThat(mClientRepository.getClientsCount()).isEqualTo(1);
    }

    @Test
    public void testReturnOldUnitForOldProcess() {
        ClientDescription description = createMockedDescription(
                UUID.randomUUID().toString(), "com.test.package",
                1000, UUID.randomUUID().toString(), CounterConfigurationReporterType.MAIN);
        CommonArguments counterConfiguration = CommonArgumentsTestUtils.createMockedArguments();
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description, counterConfiguration)
        ).thenReturn(mock(ClientUnit.class));

        ClientUnit firstClientUnit = mClientRepository.getOrCreateClient(description, counterConfiguration);
        ClientDescription description2 = createMockedDescription(description.getApiKey(),
                description.getPackageName(), description.getProcessID(), description.getProcessSessionID(), CounterConfigurationReporterType.MAIN);
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description2, counterConfiguration)
        ).thenReturn(mock(ClientUnit.class));
        assertThat(mClientRepository.getOrCreateClient(description2, counterConfiguration)).isEqualTo(firstClientUnit);
        assertThat(mClientRepository.getClientsCount()).isEqualTo(1);
    }

    @Test
    public void testReturnNewUnitForNewProcess() {
        ClientDescription description = createMockedDescription(
                UUID.randomUUID().toString(), "com.test.package",
                1000, UUID.randomUUID().toString(), CounterConfigurationReporterType.MAIN);
        CommonArguments counterConfiguration = CommonArgumentsTestUtils.createMockedArguments();
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description, counterConfiguration)
        ).thenReturn(mCreatedClientUnit);

        assertThat(mClientRepository.getOrCreateClient(description, counterConfiguration)).isEqualTo(mCreatedClientUnit);
        ClientDescription description2 = createMockedDescription(description.getApiKey(),
                description.getPackageName(), 200, UUID.randomUUID().toString(), CounterConfigurationReporterType.MAIN);
        ClientUnit secondMockedClientUnit = mock(ClientUnit.class);
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description2, counterConfiguration)
        ).thenReturn(secondMockedClientUnit);
        assertThat(mClientRepository.getOrCreateClient(description2, counterConfiguration)).isEqualTo(secondMockedClientUnit);
        assertThat(mClientRepository.getClientsCount()).isEqualTo(2);
    }

    @Test
    public void testTwoDifferentClientsForOneProcess() {
        ClientDescription description = createMockedDescription(
                UUID.randomUUID().toString(), "com.test.package",
                1000, UUID.randomUUID().toString(), CounterConfigurationReporterType.MAIN);
        CommonArguments counterConfiguration = CommonArgumentsTestUtils.createMockedArguments();
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description, counterConfiguration)
        ).thenReturn(mCreatedClientUnit);
        assertThat(mClientRepository.getOrCreateClient(description, counterConfiguration)).isEqualTo(mCreatedClientUnit);

        ClientDescription description2 = createMockedDescription(UUID.randomUUID().toString(),
                description.getPackageName(), description.getProcessID(), description.getProcessSessionID(), CounterConfigurationReporterType.MAIN);
        ClientUnit secondMockedClientUnit = mock(ClientUnit.class);
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description2, counterConfiguration)
        ).thenReturn(secondMockedClientUnit);
        assertThat(mClientRepository.getOrCreateClient(description2, counterConfiguration)).isEqualTo(secondMockedClientUnit);
        assertThat(mClientRepository.getClientsCount()).isEqualTo(2);

        mClientRepository.remove(description.getPackageName(), description.getProcessID(), description.getProcessSessionID());
        assertThat(mClientRepository.getClientsCount()).isZero();
    }

    @Test
    public void testTwoDifferentClientUnitTypesForOneProcess() {
        final String apiKey = UUID.randomUUID().toString();
        ClientDescription description = createMockedDescription(
                apiKey, "com.test.package",
                1000, UUID.randomUUID().toString(), CounterConfigurationReporterType.MAIN);
        CommonArguments counterConfiguration = CommonArgumentsTestUtils.createMockedArguments();
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description, counterConfiguration)
        ).thenReturn(mCreatedClientUnit);
        assertThat(mClientRepository.getOrCreateClient(description, counterConfiguration)).isEqualTo(mCreatedClientUnit);

        ClientDescription description2 = createMockedDescription(apiKey,
                description.getPackageName(), description.getProcessID(), description.getProcessSessionID(), CounterConfigurationReporterType.MANUAL);
        ClientUnit secondMockedClientUnit = mock(ClientUnit.class);
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description2, counterConfiguration)
        ).thenReturn(secondMockedClientUnit);
        assertThat(mClientRepository.getOrCreateClient(description2, counterConfiguration)).isEqualTo(secondMockedClientUnit);
        assertThat(mClientRepository.getClientsCount()).isEqualTo(2);

        mClientRepository.remove(description.getPackageName(), description.getProcessID(), description.getProcessSessionID());
        assertThat(mClientRepository.getClientsCount()).isZero();
    }

    @Test
    public void testRemoveForOneProcess() {
        ClientDescription description = createMockedDescription(
                UUID.randomUUID().toString(), "com.test.package",
                1000, UUID.randomUUID().toString(), CounterConfigurationReporterType.MAIN);
        CommonArguments counterConfiguration = CommonArgumentsTestUtils.createMockedArguments();
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description, counterConfiguration)
        ).thenReturn(mCreatedClientUnit);
        ClientUnit unit = mClientRepository.getOrCreateClient(description, counterConfiguration);

        ClientDescription description2 = createMockedDescription(description.getApiKey(),
                description.getPackageName(), 200, UUID.randomUUID().toString(), CounterConfigurationReporterType.MAIN);
        ClientUnit secondMockedClientUnit = mock(ClientUnit.class);
        when(mFactory.createClientUnit(
                mContext, mComponentsRepository, description2, counterConfiguration)
        ).thenReturn(secondMockedClientUnit);
        ClientUnit unit2 = mClientRepository.getOrCreateClient(description2, counterConfiguration);
        assertThat(mClientRepository.getClientsCount()).isEqualTo(2);
        mClientRepository.remove(description2.getPackageName(), description2.getProcessID(), description2.getProcessSessionID());
        assertThat(mClientRepository.getClientsCount()).isEqualTo(1);
        verify(unit, never()).onDisconnect();
        verify(unit2).onDisconnect();
    }

    private ClientDescription createDefaultMockedDescription(@Nullable String apiKey,
                                                             @NonNull String packageName,
                                                             @NonNull CounterConfigurationReporterType reporterType) {
        return createMockedDescription(apiKey, packageName, new Random().nextInt(32000),
                UUID.randomUUID().toString(), reporterType);
    }

    private ClientDescription createMockedDescription(@Nullable String apiKey,
                                                      @NonNull String packageName,
                                                      @Nullable Integer processID,
                                                      @Nullable String processSessionID,
                                                      @NonNull CounterConfigurationReporterType reporterType) {
        return new ClientDescription(apiKey, packageName, processID, processSessionID, reporterType);
    }
}
