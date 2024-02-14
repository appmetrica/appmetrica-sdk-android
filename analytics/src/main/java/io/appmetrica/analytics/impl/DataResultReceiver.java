package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.startup.StartupError;
import io.appmetrica.analytics.logger.internal.YLogger;

@SuppressLint("ParcelCreator")
public class DataResultReceiver extends ResultReceiver {

    private static final String TAG = "[DataResultReceiver]";

    @VisibleForTesting
    public static final int RESULT_CODE_STARTUP_PARAMS_UPDATED = 1;
    @VisibleForTesting
    public static final int RESULT_CODE_STARTUP_ERROR = 2;

    @NonNull
    private final Receiver mReceiver;

    public DataResultReceiver(Handler handler, @NonNull Receiver receiver) {
        super(handler);
        mReceiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, @NonNull Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        Bundle resultBundle = resultData == null ? new Bundle() : resultData;
        YLogger.d(TAG + " Receive result %d %s", resultCode, resultData);
        mReceiver.onReceiveResult(resultCode, resultBundle);
    }

    public static void notifyOnStartupUpdated(@Nullable final ResultReceiver receiver,
                                              @NonNull final ClientIdentifiersHolder clientData) {
        if (null != receiver) {
            Bundle resultData = new Bundle();
            clientData.toBundle(resultData);
            receiver.send(DataResultReceiver.RESULT_CODE_STARTUP_PARAMS_UPDATED, resultData);
        }
    }

    public static void notifyOnStartupError(final ResultReceiver receiver,
                                            final StartupError error,
                                            @Nullable ClientIdentifiersHolder clientData) {
        YLogger.debug(TAG, "Notify receiver %s with startup error %s with data %s", receiver, error, clientData);
        if (null != receiver) {
            Bundle data = new Bundle();
            error.toBundle(data);
            if (clientData != null) {
                clientData.toBundle(data);
            }
            receiver.send(DataResultReceiver.RESULT_CODE_STARTUP_ERROR, data);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Nullable
    public Receiver getReceiver() {
        return mReceiver;
    }
}
