package io.appmetrica.analytics.adrevenue.fyber.v3.internal;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.adrevenue.fyber.v3.impl.AdRevenueConverter;
import io.appmetrica.analytics.adrevenue.fyber.v3.impl.FyberAdRevenueProcessor;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint;

import static io.appmetrica.analytics.adrevenue.fyber.v3.impl.Constants.LIBRARY_MAIN_CLASS;
import static io.appmetrica.analytics.adrevenue.fyber.v3.impl.Constants.MODULE_ID;

public class FyberClientModuleEntryPoint extends ModuleClientEntryPoint<Object> {

    private static final String TAG = "[FyberClientModuleEntryPoint]";

    @NonNull
    @Override
    public String getIdentifier() {
        return MODULE_ID;
    }

    @Override
    public void initClientSide(@NonNull ClientContext clientContext) {
        DebugLogger.INSTANCE.info(TAG, "initClientSide");
        if (ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS)) {
            clientContext.getModuleAdRevenueContext().getAdRevenueProcessorsHolder().register(
                new FyberAdRevenueProcessor(
                    new AdRevenueConverter(),
                    clientContext
                )
            );
        } else {
            DebugLogger.INSTANCE.info(TAG, LIBRARY_MAIN_CLASS + " not found");
        }
    }
}
