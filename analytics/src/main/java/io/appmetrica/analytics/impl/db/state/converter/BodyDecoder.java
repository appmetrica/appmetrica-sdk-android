package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.coreutils.internal.io.GZIPCompressor;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.Arrays;

public class BodyDecoder {

    public static class AESEncryptedProvider {

        AESEncrypter getEncrypter(byte[] password, byte[] iv) {
            return new AESEncrypter(AESEncrypter.DEFAULT_ALGORITHM, password, iv);
        }
    }

    @NonNull
    private final AESEncryptedProvider mAesEncryptedProvider;
    @NonNull
    private final GZIPCompressor mGzipCompressor;

    public BodyDecoder() {
        this(new AESEncryptedProvider(), new GZIPCompressor());
    }

    @VisibleForTesting
    public BodyDecoder(@NonNull AESEncryptedProvider aesEncryptedProvider, @NonNull GZIPCompressor gzipCompressor) {
        mAesEncryptedProvider = aesEncryptedProvider;
        mGzipCompressor = gzipCompressor;
    }

    @Nullable
    public byte[] decode(@Nullable byte[] encoded, @NonNull String key) {
        byte[] result = null;
        try {
            final int ivLength = 16;
            byte[] iv = Arrays.copyOfRange(encoded, 0, ivLength);
            AESEncrypter aesEncrypter = mAesEncryptedProvider.getEncrypter(key.getBytes(), iv);
            if (Utils.isNullOrEmpty(encoded) == false) {
                byte[] decryptedData = aesEncrypter.decrypt(
                        encoded,
                        ivLength,
                        encoded.length - ivLength);
                result = mGzipCompressor.uncompress(decryptedData);
            }
        } catch (Throwable e) {
            YLogger.e(e.getMessage(), e);
            result = null;
        }
        return result;
    }
}
