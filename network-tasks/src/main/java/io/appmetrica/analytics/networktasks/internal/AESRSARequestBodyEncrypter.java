package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.encryption.AESRSAEncrypter;

public class AESRSARequestBodyEncrypter implements RequestBodyEncrypter {

    private AESRSAEncrypter mAESRSAEncrypter;

    public AESRSARequestBodyEncrypter() {
        this(new AESRSAEncrypter());
    }

    @Nullable
    public byte[] encrypt(@Nullable final byte[] input) {
        return mAESRSAEncrypter.encrypt(input);
    }

    @NonNull
    public RequestBodyEncryptionMode getEncryptionMode() {
        return RequestBodyEncryptionMode.AES_RSA;
    }

    @VisibleForTesting
    AESRSARequestBodyEncrypter(final AESRSAEncrypter aesRsaEncrypter) {
        mAESRSAEncrypter = aesRsaEncrypter;
    }
}
