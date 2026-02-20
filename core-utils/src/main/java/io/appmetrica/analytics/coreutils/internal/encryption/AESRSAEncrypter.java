package io.appmetrica.analytics.coreutils.internal.encryption;

import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.crypto.Encrypter;
import io.appmetrica.analytics.coreutils.internal.io.CloseableUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class AESRSAEncrypter implements Encrypter {

    private static final String TAG = "[AESRSAEncrypter]";

    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private static final String PUBLIC_KEY_ALGORITHM = "RSA";

    private final String mAESAlgorithm;
    private final String mRSAAlgorithm;

    public AESRSAEncrypter() {
        this(AESEncrypter.DEFAULT_ALGORITHM, RSA_ALGORITHM);
    }

    @VisibleForTesting
    AESRSAEncrypter(@NonNull String aesAlgorithm, @NonNull String rsaAlgorithm) {
        mAESAlgorithm = aesAlgorithm;
        mRSAAlgorithm = rsaAlgorithm;
    }

    @Nullable
    public byte[] encrypt(@NonNull byte[] input) {
        try {
            SecureRandom random = new SecureRandom();
            final byte[] iv = new byte[AESEncrypter.DEFAULT_KEY_LENGTH];
            final byte[] password = new byte[AESEncrypter.DEFAULT_KEY_LENGTH];
            random.nextBytes(password);
            random.nextBytes(iv);
            PublicKey publicKey = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM).generatePublic(
                    new X509EncodedKeySpec(Base64.decode(RSAPublicKeyString.VALUE, Base64.DEFAULT))
            );

            return encryptInternal(input, password, iv, publicKey);
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e);
        }
        return null;
    }

    @VisibleForTesting
    @Nullable
    byte[] encryptInternal(@NonNull byte[] input,
                           @NonNull byte[] password,
                           @NonNull byte[] iv,
                           @NonNull PublicKey publicKey) {
        ByteArrayOutputStream outputStream = null;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream(password.length + iv.length);

            stream.write(password);
            stream.write(iv);

            byte[] keys = stream.toByteArray();
            stream.close();

            Cipher rsaCipher = Cipher.getInstance(mRSAAlgorithm);

            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            outputStream = new ByteArrayOutputStream(input.length);
            outputStream.write(rsaCipher.doFinal(keys));

            byte[] aesEncryptedBytes = new AESEncrypter(mAESAlgorithm, password, iv).encrypt(input);
            if (aesEncryptedBytes != null) {
                outputStream.write(aesEncryptedBytes);
                return outputStream.toByteArray();
            }
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e);
        } finally {
            CloseableUtils.closeSafely(outputStream);
        }
        return null;
    }

    @VisibleForTesting
    byte[] decryptInternal(byte[] input, PrivateKey privateKey) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(input);
            byte[] encryptedKeys = new byte[128];
            bis.read(encryptedKeys);
            Cipher rsaCipher = Cipher.getInstance(mRSAAlgorithm);
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] keys = rsaCipher.doFinal(encryptedKeys);
            byte[] password = new byte[16];
            byte[] iv = new byte[16];
            System.arraycopy(keys, 0, password, 0, password.length);
            System.arraycopy(keys, password.length, iv, 0, iv.length);

            byte[] text = new byte[input.length - encryptedKeys.length];
            bis.read(text);
            bis.close();

            return new AESEncrypter(mAESAlgorithm, password, iv).decrypt(text);
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e);
        }
        return null;
    }
}
