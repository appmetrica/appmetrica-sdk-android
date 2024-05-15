package io.appmetrica.analytics.impl.db.protobuf;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateSerializer;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.protobuf.nano.MessageNano;

public class ProtobufStateStorageImpl<T, P extends MessageNano> implements ProtobufStateStorage<T> {

    @NonNull
    private final String mKey;
    @NonNull
    private final IBinaryDataHelper mDbHelper;
    @NonNull
    private final ProtobufStateSerializer<P> mSerializer;
    @NonNull
    private final ProtobufConverter<T, P> mConverter;

    public ProtobufStateStorageImpl(@NonNull String key,
                                    @NonNull IBinaryDataHelper dbHelper,
                                    @NonNull ProtobufStateSerializer<P> serializer,
                                    @NonNull ProtobufConverter<T, P> converter) {
        mKey = key;
        mDbHelper = dbHelper;
        mSerializer = serializer;
        mConverter = converter;
    }

    public void save(@NonNull T state) {
        mDbHelper.insert(mKey, mSerializer.toByteArray(mConverter.fromModel(state)));
    }

    @NonNull
    public T read() {
        try {
            byte [] stored = mDbHelper.get(mKey);
            if (Utils.isNullOrEmpty(stored)) {
                return mConverter.toModel(mSerializer.defaultValue());
            } else {
                return mConverter.toModel(mSerializer.toState(stored));
            }
        } catch (Throwable ignored) {
            return mConverter.toModel(mSerializer.defaultValue());
        }
    }

    @Override
    public void delete() {
        mDbHelper.remove(mKey);
    }
}
