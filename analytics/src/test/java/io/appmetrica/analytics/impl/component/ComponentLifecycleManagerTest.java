package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.impl.component.clients.ClientUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class ComponentLifecycleManagerTest extends CommonTest {

    private final ComponentLifecycleManager<ClientUnit> mLifecycleManager = new ComponentLifecycleManager<ClientUnit>();

    @Test
    public void testClientConnected() {
        ClientUnit clientUnit = mock(ClientUnit.class);
        assertThat(mLifecycleManager.getConnectedClients()).isEmpty();
        mLifecycleManager.connectClient(clientUnit);
        assertThat(mLifecycleManager.getConnectedClients()).hasSize(1);
    }

    @Test
    public void testAllClientsDisconnected() {
        ClientUnit clientUnit = mock(ClientUnit.class);
        ClientUnit clientUnit2 = mock(ClientUnit.class);
        mLifecycleManager.connectClient(clientUnit);
        mLifecycleManager.connectClient(clientUnit2);
        assertThat(mLifecycleManager.getConnectedClients()).hasSize(2);
        mLifecycleManager.disconnectClient(clientUnit);
        mLifecycleManager.disconnectClient(clientUnit2);
        assertThat(mLifecycleManager.getConnectedClients()).isEmpty();
    }

    @Test
    public void testNotAllClientsDisconnected() {
        ClientUnit clientUnit = mock(ClientUnit.class);
        ClientUnit clientUnit2 = mock(ClientUnit.class);
        mLifecycleManager.connectClient(clientUnit);
        mLifecycleManager.connectClient(clientUnit2);
        assertThat(mLifecycleManager.getConnectedClients()).hasSize(2);
        mLifecycleManager.disconnectClient(clientUnit);
        assertThat(mLifecycleManager.getConnectedClients()).hasSize(1);
    }

}
