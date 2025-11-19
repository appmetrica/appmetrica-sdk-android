package io.appmetrica.analytics.adrevenue.ironsource.v9.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.adrevenue.ironsource.v9.impl.Constants;
import io.appmetrica.analytics.adrevenue.ironsource.v9.impl.IronSourceAdRevenueAdapter;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueCollector;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.appmetrica.analytics.adrevenue.ironsource.v9.impl.Constants.LIBRARY_MAIN_CLASS;

public class IronSourceV9ClientModuleEntryPoint extends ModuleClientEntryPoint<Object> {

    private static final String TAG = "[IronSourceV9ClientModuleEntryPoint]";

    @Nullable
    private ClientContext clientContext = null;

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    @NonNull
    @Override
    public String getIdentifier() {
        return Constants.MODULE_ID;
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
            IronSourceAdRevenueAdapter.registerListener(clientContext);
            DebugLogger.INSTANCE.info(TAG, "Set enabled true");
            enabled.set(true);
        } else {
            DebugLogger.INSTANCE.info(TAG, LIBRARY_MAIN_CLASS + " not found");
        }
    }

    @Nullable
    @Override
    public AdRevenueCollector getAdRevenueCollector() {
        return new AdRevenueCollector() {
            @NonNull
            @Override
            public String getSourceIdentifier() {
                DebugLogger.INSTANCE.info(TAG, "getSourceIdentifier return: ${Constants.AD_REVENUE_SOURCE_IDENTIFIER}");
                return Constants.AD_REVENUE_SOURCE_IDENTIFIER;
            }

            @Override
            public boolean getEnabled() {
                DebugLogger.INSTANCE.info(TAG, "getEnabled return: ${enabled.get()}");
                return enabled.get();
            }
        };
    }
}
