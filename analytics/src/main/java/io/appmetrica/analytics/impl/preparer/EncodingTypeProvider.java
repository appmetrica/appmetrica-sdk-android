package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode;

interface EncodingTypeProvider {

    int getEncodingType(@NonNull EventEncryptionMode encryptionMode);
}
