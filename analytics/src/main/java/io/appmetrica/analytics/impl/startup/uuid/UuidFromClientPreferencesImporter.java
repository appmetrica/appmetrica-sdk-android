package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;

public class UuidFromClientPreferencesImporter implements IOuterSourceUuidImporter {

    private static final String TAG = "[UuidFromClientPreferencesImporter]";

    @Nullable
    @Override
    public String get(@NonNull Context context) {
        PreferencesClientDbStorage preferencesClientDbStorage = new PreferencesClientDbStorage(
            DatabaseStorageFactory.getInstance(context.getApplicationContext()).getClientDbHelper()
        );
        String uuid = null;
        IdentifiersResult identifiersResult = preferencesClientDbStorage.getUuidResult();
        if (!TextUtils.isEmpty(identifiersResult.id)) {
            uuid = identifiersResult.id;
            YLogger.info(TAG, "Uuid from preference client db storage = %s", uuid);
        }
        return uuid;
    }

}
