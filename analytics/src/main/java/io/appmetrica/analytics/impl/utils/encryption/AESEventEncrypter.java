package io.appmetrica.analytics.impl.utils.encryption;

import android.text.TextUtils;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class AESEventEncrypter implements EventEncrypter {

    private static final String TAG = "[AESEventEncrypter]";

    @NonNull
    private final AESEncrypter mAESEncrypter;

    AESEventEncrypter() {
        this(new AESCredentialProvider(GlobalServiceLocator.getInstance().getContext()));
    }

    AESEventEncrypter(@NonNull final AESCredentialProvider credentialProvider) {
        this(new AESEncrypter(AESEncrypter.DEFAULT_ALGORITHM, credentialProvider.getPassword(),
                credentialProvider.getIV()));
    }

    @VisibleForTesting
    AESEventEncrypter(@NonNull final AESEncrypter aesEncrypter) {
        mAESEncrypter = aesEncrypter;
    }

    @NonNull
    public EncryptedCounterReport encrypt(@NonNull final CounterReport counterReport) {
        String inputValue = counterReport.getValue();
        String result = null;
        if (TextUtils.isEmpty(inputValue) == false) {
            try {
                byte[] inputBytes = inputValue.getBytes(IOUtils.UTF8_ENCODING);
                byte[] encryptedBytes = mAESEncrypter.encrypt(inputBytes);
                if (encryptedBytes != null) {
                    result = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
                }
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(TAG, e, e.getMessage());
            }
        }
        counterReport.setValue(result);
        return new EncryptedCounterReport(counterReport, getEncryptionMode());
    }

    @NonNull
    public byte[] decrypt(@Nullable final byte[] input) {
        byte[] result = new byte[0];
        if (input != null && input.length > 0) {
            try {
                byte[] bytesToDecrypt = Base64.decode(input, Base64.DEFAULT);
                result = mAESEncrypter.decrypt(bytesToDecrypt);
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(TAG, e, e.getMessage());
            }
        }
        return result;
    }

    @NonNull
    public EventEncryptionMode getEncryptionMode() {
        return EventEncryptionMode.AES_VALUE_ENCRYPTION;
    }

    @NonNull
    @VisibleForTesting
    AESEncrypter getAESEncrypter() {
        return mAESEncrypter;
    }
}
