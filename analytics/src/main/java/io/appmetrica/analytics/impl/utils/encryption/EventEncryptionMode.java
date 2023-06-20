package io.appmetrica.analytics.impl.utils.encryption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum EventEncryptionMode {
    NONE(0),
    EXTERNALLY_ENCRYPTED_EVENT_CRYPTER(1),
    AES_VALUE_ENCRYPTION(2);

    private final int mModeId;

    private EventEncryptionMode(int modeId) {
        mModeId = modeId;
    }

    public int getModeId() {
        return mModeId;
    }

    @NonNull
    public static EventEncryptionMode valueOf(@Nullable Integer modeId) {
        if (modeId != null) {
            for (EventEncryptionMode eventEncryptionMode : EventEncryptionMode.values()) {
                if (eventEncryptionMode.getModeId() == modeId) {
                    return eventEncryptionMode;
                }
            }
        }
        return NONE;
    }
}
