package io.appmetrica.analytics.apphud.internal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientConfigExtension;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint;
import io.appmetrica.analytics.modulesapi.internal.client.ClientConfigListener;

public class ApphudClientModuleEntryPoint extends ModuleClientEntryPoint<Object> {

    private static final String TAG = "[ApphudClientModuleEntryPoint]";

    @Nullable
    private String apiKey = null;

    @NonNull
    @Override
    public String getIdentifier() {
        return Constants.MODULE_ID;
    }

    @Override
    public void initClientSide(@NonNull ClientContext clientContext) {
    }

    @Override
    public void onActivated() {
    }

    @Nullable
    @Override
    public ClientConfigExtension getClientConfigExtension() {
        return new ClientConfigExtension() {
            @NonNull
            @Override
            public ClientConfigListener getClientConfigListener() {
                return new ClientConfigListener() {
                    @Override
                    public void onConfigReceived(@NonNull Bundle startupBundle) {
                        String apiKey = startupBundle.getString(Constants.Config.API_KEY_KEY);
                        DebugLogger.INSTANCE.info(TAG, "received config on client side apiKey = " + apiKey);
                        ApphudClientModuleEntryPoint.this.apiKey = apiKey;
                    }
                };
            }

            @Override
            public boolean doesModuleNeedConfig() {
                return ApphudClientModuleEntryPoint.this.apiKey == null;
            }
        };
    }
}
