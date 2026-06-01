package io.appmetrica.analytics.apphud.internal;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;

public class ServiceSideApphudConfigWrapper {

    @NonNull
    final ServiceSideApphudConfig config;

    ServiceSideApphudConfigWrapper(@NonNull ServiceSideApphudConfig config) {
        this.config = config;
    }

    @NonNull
    static ServiceSideApphudConfigWrapper toWrapper(@NonNull ServiceSideApphudConfig config) {
        return new ServiceSideApphudConfigWrapper(config);
    }
}
