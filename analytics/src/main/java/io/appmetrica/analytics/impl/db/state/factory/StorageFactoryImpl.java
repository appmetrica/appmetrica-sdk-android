package io.appmetrica.analytics.impl.db.state.factory;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.impl.db.IBinaryDataHelper;

public abstract class StorageFactoryImpl<T> implements StorageFactory<T> {

    @Override
    public ProtobufStateStorage<T> create(@NonNull Context context) {
        return createWithHelper(context, getMainBinaryDataHelper(context));
    }

    @Override
    public ProtobufStateStorage<T> createForMigration(@NonNull Context context) {
        return createWithHelper(context, getMigrationBinaryDataHelper(context));
    }

    @NonNull
    protected abstract ProtobufStateStorage<T> createWithHelper(@NonNull Context context,
                                                                @NonNull IBinaryDataHelper helper);

    @NonNull
    protected abstract IBinaryDataHelper getMainBinaryDataHelper(@NonNull Context context);

    @NonNull
    protected abstract IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context);
}
