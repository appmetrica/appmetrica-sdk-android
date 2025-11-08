package io.appmetrica.analytics.impl.component.remarketing;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentId;

public class EventHashesStorage {

    private static final String EVENT_HASHES_DB_KEY = "event_hashes";

    @NonNull
    private final EventHashesConverter mConverter;
    @NonNull
    private final EventHashesSerializer mEventHashesSerializer;
    @NonNull
    private final IBinaryDataHelper mBinaryDataHelper;
    @NonNull
    private final String mDbKey;

    public EventHashesStorage(@NonNull final Context context, @NonNull final ComponentId componentId) {
        this(
            new EventHashesSerializer(),
            new EventHashesConverter(),
            GlobalServiceLocator.getInstance().getStorageFactory().getComponentBinaryDataHelper(
                context,
                componentId
            ),
            EVENT_HASHES_DB_KEY
        );
    }

    @VisibleForTesting
    EventHashesStorage(@NonNull final EventHashesSerializer eventHashesSerializer,
                       @NonNull final EventHashesConverter eventHashesConverter,
                       @NonNull final IBinaryDataHelper binaryDataHelper,
                       @NonNull final String dbKey) {
        mEventHashesSerializer = eventHashesSerializer;
        mConverter = eventHashesConverter;
        mBinaryDataHelper = binaryDataHelper;
        mDbKey = dbKey;
    }

    @NonNull
    public EventHashes read() {
        try {
            byte[] valueFromDb = mBinaryDataHelper.get(mDbKey);
            if (Utils.isNullOrEmpty(valueFromDb)) {
                EventHashes tmp = mConverter.toModel(mEventHashesSerializer.defaultValue());
                return tmp;
            } else {
                return mConverter.toModel(mEventHashesSerializer.toState(valueFromDb));
            }
        } catch (Throwable ignored) {
            return mConverter.toModel(mEventHashesSerializer.defaultValue());
        }
    }

    public void write(@NonNull final EventHashes input) {
        mBinaryDataHelper.insert(mDbKey, mEventHashesSerializer.toByteArray(mConverter.fromModel(input)));
    }

    @NonNull
    @VisibleForTesting
    EventHashesSerializer getEventHashesSerializer() {
        return mEventHashesSerializer;
    }

    @NonNull
    @VisibleForTesting
    public IBinaryDataHelper getBinaryDataHelper() {
        return mBinaryDataHelper;
    }

    @NonNull
    @VisibleForTesting
    public String getDbKey() {
        return mDbKey;
    }
}
