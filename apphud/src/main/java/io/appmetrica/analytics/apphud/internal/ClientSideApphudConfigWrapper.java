package io.appmetrica.analytics.apphud.internal;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.config.client.model.ClientSideApphudConfig;

public class ClientSideApphudConfigWrapper {

    @NonNull
    final ClientSideApphudConfig config;

    ClientSideApphudConfigWrapper(@NonNull ClientSideApphudConfig config) {
        this.config = config;
    }

    @NonNull
    static ClientSideApphudConfigWrapper toWrapper(@NonNull ClientSideApphudConfig config) {
        return new ClientSideApphudConfigWrapper(config);
    }
}
