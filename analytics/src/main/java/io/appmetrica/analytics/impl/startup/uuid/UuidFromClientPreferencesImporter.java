package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class UuidFromClientPreferencesImporter implements IOuterSourceUuidImporter {

    private static final String TAG = "[UuidFromClientPreferencesImporter]";
    @NonNull
    private final PreferencesClientDbStorage preferencesClientDbStorage;

    public UuidFromClientPreferencesImporter(@NonNull PreferencesClientDbStorage preferencesClientDbStorage) {
        this.preferencesClientDbStorage = preferencesClientDbStorage;
    }

    @Nullable
    @Override
    public String get(@NonNull Context context) {
        String uuid = null;
        IdentifiersResult identifiersResult = preferencesClientDbStorage.getUuidResult();
        if (!StringUtils.isNullOrEmpty(identifiersResult.id)) {
            uuid = identifiersResult.id;
            DebugLogger.INSTANCE.info(TAG, "Uuid from preference client db storage = %s", uuid);
        }
        return uuid;
    }

}
