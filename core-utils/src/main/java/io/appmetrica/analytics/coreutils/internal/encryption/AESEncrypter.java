package io.appmetrica.analytics.coreutils.internal.encryption;

import android.annotation.SuppressLint;
import androidx.annotation.VisibleForTesting;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypter {

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

    @SuppressLint("TrulyRandom")
    public byte[] encrypt(byte[] input) throws Throwable {
        SecretKeySpec secretKeySpec = new SecretKeySpec(mPassword, SECRET_KEY_ALGORITHM);
        Cipher aesCipher = Cipher.getInstance(mAlgorithm);
        IvParameterSpec spec = new IvParameterSpec(mIV);
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);

        return aesCipher.doFinal(input);
    }

    @SuppressLint("TrulyRandom")
    public byte[] decrypt(byte[] input) throws Throwable {
        return decrypt(input, 0, input.length);
    }

    public byte[] decrypt(byte[] input, int offset, int length) throws Throwable {
        SecretKeySpec secretKeySpec = new SecretKeySpec(mPassword, SECRET_KEY_ALGORITHM);
        Cipher aesCipher = Cipher.getInstance(mAlgorithm);
        IvParameterSpec spec = new IvParameterSpec(mIV);
        aesCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);
        return aesCipher.doFinal(input, offset, length);
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
