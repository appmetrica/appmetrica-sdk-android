package io.appmetrica.analytics.adrevenue.admob.v23.internal;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.processor.AdMobAdRevenueProcessor;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint;

import static io.appmetrica.analytics.adrevenue.admob.v23.impl.Constants.LIBRARY_MAIN_CLASS;
import static io.appmetrica.analytics.adrevenue.admob.v23.impl.Constants.MODULE_ID;

public class AdMobClientModuleEntryPoint extends ModuleClientEntryPoint<Object> {

    private static final String TAG = "[AdMobClientModuleEntryPoint]";

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
                new AdMobAdRevenueProcessor(
                    new AdRevenueConverter(),
                    clientContext
                )
            );
        } else {
            DebugLogger.INSTANCE.info(TAG, LIBRARY_MAIN_CLASS + " not found");
        }
    }
}
