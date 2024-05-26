package io.appmetrica.analytics.adrevenue.ironsource.v7.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.adrevenue.ironsource.v7.impl.Constants;
import io.appmetrica.analytics.adrevenue.ironsource.v7.impl.IronSourceAdRevenueAdapter;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint;

import static io.appmetrica.analytics.adrevenue.ironsource.v7.impl.Constants.LIBRARY_MAIN_CLASS;

public class IronSourceClientModuleEntryPoint implements ModuleClientEntryPoint<Object> {

    private static final String TAG = "[IronSourceClientModuleEntryPoint]";

    @Nullable
    private ClientContext clientContext = null;

    @NonNull
    @Override
    public String getIdentifier() {
        return Constants.MODULE_ID;
    }

    @Override
    public void initClientSide(@NonNull ClientContext clientContext) {
        DebugLogger.info(TAG, "initClientSide");
        this.clientContext = clientContext;
    }

    @Override
    public void onActivated() {
        DebugLogger.info(TAG, "onActivated");
        if (ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS) && clientContext != null) {
            IronSourceAdRevenueAdapter.registerListener(clientContext);
        } else {
            DebugLogger.info(TAG, LIBRARY_MAIN_CLASS + " not found");
        }
    }
}
