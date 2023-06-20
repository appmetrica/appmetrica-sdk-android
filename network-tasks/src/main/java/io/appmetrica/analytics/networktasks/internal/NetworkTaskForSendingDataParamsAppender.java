package io.appmetrica.analytics.networktasks.internal;

import android.net.Uri;
import androidx.annotation.NonNull;

public class NetworkTaskForSendingDataParamsAppender {

    @NonNull
    private final RequestBodyEncrypter requestBodyEncrypter;

    public NetworkTaskForSendingDataParamsAppender(@NonNull RequestBodyEncrypter requestBodyEncrypter) {
        this.requestBodyEncrypter = requestBodyEncrypter;
    }

    public void appendEncryptedData(@NonNull Uri.Builder uriBuilder) {
        if (requestBodyEncrypter.getEncryptionMode() == RequestBodyEncryptionMode.AES_RSA) {
            uriBuilder.appendQueryParameter(
                    CommonUrlParts.ENCRYPTED_REQUEST,
                    CommonUrlParts.EncryptedRequestValues.AES_RSA
            );
        }
    }
}
