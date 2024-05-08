package io.appmetrica.analytics.impl.db.state;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.StateSerializer;
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.io.IOException;

public class EncryptedStateSerializer<T> implements StateSerializer<T> {

    private static final String TAG = "[EncryptedStateSerializer]";

    @NonNull
    private final StateSerializer<T> mBackedSerializer;
    @NonNull
    private final AESEncrypter mEncrypter;

    public EncryptedStateSerializer(@NonNull final StateSerializer<T> backedSerializer,
                                    @NonNull final AESEncrypter encrypter) {
        mBackedSerializer = backedSerializer;
        mEncrypter = encrypter;
    }

    @NonNull
    @Override
    public byte[] toByteArray(@NonNull final T message) {
        try {
            return mEncrypter.encrypt(mBackedSerializer.toByteArray(message));
        } catch (Throwable e) {
            YLogger.error(TAG, e, e.getMessage());
        }
        return new byte[0];
    }

    @NonNull
    @Override
    public T toState(@NonNull byte[] data) throws IOException {
        byte[] decryptedData = null;
        try {
            decryptedData = mEncrypter.decrypt(data);
        } catch (Throwable e) {
            throw new IOException(e);
        }
        return mBackedSerializer.toState(decryptedData);
    }

    @NonNull
    @Override
    public T defaultValue() {
        return mBackedSerializer.defaultValue();
    }
}
