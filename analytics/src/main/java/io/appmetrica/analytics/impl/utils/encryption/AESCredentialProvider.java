package io.appmetrica.analytics.impl.utils.encryption;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.impl.utils.SecurityUtils;
import io.appmetrica.analytics.logger.internal.YLogger;

public class AESCredentialProvider {

    private static final String TAG = "[AESCredentialProvider]";

    @NonNull
    private final Context context;

    public AESCredentialProvider(@NonNull final Context context) {
        this.context = context;
    }

    public byte[] getPassword() {
        byte[] password;
        try {
            password = SecurityUtils.getMD5Hash(context.getPackageName());
        } catch (Throwable e) {
            YLogger.e(e, "%s could not get password", TAG);
            password = new byte[AESEncrypter.DEFAULT_KEY_LENGTH];
        }
        return password;
    }

    public byte[] getIV() {
        byte[] iv = null;
        try {
            iv = SecurityUtils.getMD5Hash(
                    new StringBuilder(context.getPackageName())
                            .reverse()
                            .toString()
            );
        } catch (Throwable e) {
            YLogger.e(e, "%s could not get iv", TAG);
            iv = new byte[AESEncrypter.DEFAULT_KEY_LENGTH];
        }
        return iv;
    }
}
