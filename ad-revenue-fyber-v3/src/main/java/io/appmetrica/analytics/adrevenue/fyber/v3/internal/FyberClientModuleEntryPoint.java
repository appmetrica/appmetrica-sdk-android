package io.appmetrica.analytics.adrevenue.fyber.v3.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.adrevenue.fyber.v3.impl.FyberAdRevenueAdapter;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint;

import static io.appmetrica.analytics.adrevenue.fyber.v3.impl.Constants.LIBRARY_MAIN_CLASS;
import static io.appmetrica.analytics.adrevenue.fyber.v3.impl.Constants.MODULE_ID;

public class FyberClientModuleEntryPoint implements ModuleClientEntryPoint<Object> {

    private static final String TAG = "[FyberClientModuleEntryPoint]";

    @Nullable
    private ClientContext clientContext = null;

    @NonNull
    @Override
    public String getIdentifier() {
        return MODULE_ID;
    }

    @Override
    public void initClientSide(@NonNull ClientContext clientContext) {
        DebugLogger.INSTANCE.info(TAG, "initClientSide");
        this.clientContext = clientContext;
    }

    @Override
    public void onActivated() {
        DebugLogger.INSTANCE.info(TAG, "onActivated");
        if (ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS) && clientContext != null) {
            FyberAdRevenueAdapter.registerListener(clientContext);
        } else {
            DebugLogger.INSTANCE.info(TAG, LIBRARY_MAIN_CLASS + " not found");
        }
    }
}
