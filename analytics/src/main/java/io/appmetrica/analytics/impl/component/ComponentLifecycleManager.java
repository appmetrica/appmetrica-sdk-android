package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ComponentLifecycleManager<CU extends ClientUnit> implements IClientConsumer<CU>{

    private final List<CU> mConnectedClients = new CopyOnWriteArrayList<CU>();

    @Override
    public void connectClient(@NonNull CU client) {
        mConnectedClients.add(client);
    }

    @Override
    public void disconnectClient(@NonNull CU client) {
        mConnectedClients.remove(client);
    }

    public List<CU> getConnectedClients() {
        return mConnectedClients;
    }

}
