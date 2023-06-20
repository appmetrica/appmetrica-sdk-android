package io.appmetrica.analytics.impl.db.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.StorageType;

public class KeyValueTableDbHelperWrapperTestProxy extends KeyValueTableDbHelperWrapper {

    public KeyValueTableDbHelperWrapperTestProxy(@NonNull Context context,
                                                 @NonNull StorageType storageType,
                                                 @NonNull IKeyValueTableDbHelper actualHelper) {
        super(context, storageType, actualHelper);
    }
}
