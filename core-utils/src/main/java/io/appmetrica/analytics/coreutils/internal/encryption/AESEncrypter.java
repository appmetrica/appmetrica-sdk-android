package io.appmetrica.analytics.coreutils.internal.encryption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.crypto.Encrypter;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypter implements Encrypter {

    public static final String TAG = "[AESEncrypter]";

    public static final String DEFAULT_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static final int DEFAULT_KEY_LENGTH = 16;

    private static final String SECRET_KEY_ALGORITHM = "AES";

    private final String mAlgorithm;
    private final byte[] mPassword;
    private final byte[] mIV;

    public AESEncrypter(String algorithm, byte[] password, byte[] iv) {
        mAlgorithm = algorithm;
        mPassword = password;
        mIV = iv;
    }

    @Override
    @Nullable
    public byte[] encrypt(@NonNull byte[] input) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(mPassword, SECRET_KEY_ALGORITHM);
            Cipher aesCipher = Cipher.getInstance(mAlgorithm);
            IvParameterSpec spec = new IvParameterSpec(mIV);
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);

            return aesCipher.doFinal(input);
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e);
        }
        return null;
    }

    @Nullable
    public byte[] decrypt(@NonNull byte[] input)  {
        return decrypt(input, 0, input.length);
    }

    @Nullable
    public byte[] decrypt(@NonNull byte[] input, int offset, int length) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(mPassword, SECRET_KEY_ALGORITHM);
            Cipher aesCipher = Cipher.getInstance(mAlgorithm);
            IvParameterSpec spec = new IvParameterSpec(mIV);
            aesCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);
            return aesCipher.doFinal(input, offset, length);
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e);
        }
        return null;
    }

    @VisibleForTesting
    public String getAlgorithm() {
        return mAlgorithm;
    }

    @VisibleForTesting
    public byte[] getPassword() {
        return mPassword;
    }

    @VisibleForTesting
    public byte[] getIV() {
        return mIV;
    }
}
