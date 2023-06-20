package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.BiFunction;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;

class CrashpadCrashParser implements BiFunction<String, Bundle, CrashpadCrash> {

    private static final String TAG = "[CrashpadCrashParser]";

    @NonNull
    private final PermanentConfigSerializer descriptionSerializer;
    @NonNull
    private final RuntimeConfigDeserializer runtimeConfigDeserializer;

    CrashpadCrashParser() {
        this(new PermanentConfigSerializer(), new RuntimeConfigDeserializer());
    }

    @VisibleForTesting
    CrashpadCrashParser(@NonNull PermanentConfigSerializer descriptionSerializer,
                        @NonNull RuntimeConfigDeserializer runtimeConfigDeserializer) {
        this.descriptionSerializer = descriptionSerializer;
        this.runtimeConfigDeserializer = runtimeConfigDeserializer;
    }

    @Override
    public CrashpadCrash apply(@NonNull String uuid, @NonNull Bundle crashData) {
        CrashpadCrashReport report = CrashpadCrashReport.fromBundle(uuid, crashData);
        if (report != null) {
            String descriptionString = crashData.getString(CrashpadConstants.ARGUMENT_CLIENT_DESCRIPTION);
            if (!TextUtils.isEmpty(descriptionString)) {
                ClientDescription description = descriptionSerializer.deserialize(descriptionString);
                if (description != null) {
                    RuntimeConfig runtimeConfig = runtimeConfigDeserializer.deserialize(
                            WrapUtils.getOrDefault(crashData.getString(CrashpadConstants.ARGUMENT_RUNTIME_CONFIG), "")
                    );
                    YLogger.debug(
                            TAG, " new crash from crashpad time %d path %s for client %s with runtime config %s",
                            report.creationTime, report.dumpFile, description, runtimeConfig
                    );
                    return new CrashpadCrash(report, description,
                            runtimeConfig
                    );
                }
            }
        } else {
            YLogger.debug(TAG, "report for %s is null", uuid);
        }
        return null;
    }
}
