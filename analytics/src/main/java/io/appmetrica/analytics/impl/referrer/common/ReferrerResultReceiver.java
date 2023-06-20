package io.appmetrica.analytics.impl.referrer.common;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;

public class ReferrerResultReceiver extends ResultReceiver {

    private static final String TAG = "[ReferrerResultReceiver]";

    public static final String BUNDLE_KEY = "io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver";
    private static final int CODE_OK = 1;
    private static final String KEY_REFERRER = "referrer";

    @NonNull
    private final ReferrerChosenListener listener;

    public ReferrerResultReceiver(@NonNull Handler handler,
                                  @NonNull ReferrerChosenListener listener) {
        super(handler);
        this.listener = listener;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        YLogger.info(TAG, "Received result with code %d and bundle %s", resultCode, resultData);
        if (resultCode == CODE_OK) {
            ReferrerInfo referrerInfo = null;
            try {
                referrerInfo = ReferrerInfo.parseFrom(resultData.getByteArray(KEY_REFERRER));
            } catch (Throwable e) {
                YLogger.error(TAG, e);
            }
            listener.onReferrerChosen(referrerInfo);
        }
    }

    public static void sendReferrer(@Nullable ResultReceiver receiver, @Nullable ReferrerInfo referrerInfo) {
        YLogger.info(TAG, "Send referrer %s to receiver %s", receiver, receiver);
        if (receiver != null) {
            Bundle bundle = new Bundle();
            bundle.putByteArray(KEY_REFERRER, referrerInfo == null ? null : referrerInfo.toProto());
            receiver.send(CODE_OK, bundle);
        }
    }
}
