package io.appmetrica.analytics.impl.db.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import io.appmetrica.analytics.impl.db.IBinaryDataHelper;
import io.appmetrica.analytics.impl.db.StorageType;

public class BinaryDataHelperWrapperTestProxy extends BinaryDataHelperWrapper {

    public BinaryDataHelperWrapperTestProxy(@NonNull Context context,
                                            @NonNull StorageType storageType,
                                            @NonNull IBinaryDataHelper actualHelper) {
        super(context, storageType, actualHelper);
    }
}
