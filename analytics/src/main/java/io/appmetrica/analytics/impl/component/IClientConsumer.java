package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;

public interface IClientConsumer<CU extends ClientUnit> {

    void connectClient(@NonNull CU client);

    void disconnectClient(@NonNull CU client);

}
