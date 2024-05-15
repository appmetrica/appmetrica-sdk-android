package io.appmetrica.analytics.impl;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.utils.MainProcessDetector;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.io.File;
import java.io.FileOutputStream;
import org.json.JSONObject;

public class AppMetricaServiceDelayHandler {

    private static final String TAG = "[AppMetricaServiceDelayHandler]";
    private static final String KEY_DELAY = "delay";
    private static final String METRICA_SERVICE_SETTING_FILENAME = "metrica_service_settings.dat";

    @NonNull
    private final MainProcessDetector processDetector;
    @NonNull
    private final FileProvider fileProvider;
    private boolean delayHappened = false;
    @Nullable
    private Long cachedDelay;

    public AppMetricaServiceDelayHandler(@NonNull MainProcessDetector processDetector) {
        this(processDetector, new FileProvider());
    }

    @VisibleForTesting
    AppMetricaServiceDelayHandler(@NonNull MainProcessDetector processDetector, @NonNull FileProvider fileProvider) {
        this.processDetector = processDetector;
        this.fileProvider = fileProvider;
    }

    @AnyThread
    public synchronized void setDelay(@NonNull Context context, long timeout) {
        if (processDetector.isMainProcess()) {
            try {
                cachedDelay = timeout;
                String value = new JSONObject().put(KEY_DELAY, timeout).toString();
                File metricaServiceSettingsFile = fileProvider
                        .getFileFromStorage(context, METRICA_SERVICE_SETTING_FILENAME);
                if (metricaServiceSettingsFile != null) {
                    IOUtils.writeStringFileLocked(
                            value,
                            METRICA_SERVICE_SETTING_FILENAME,
                            new FileOutputStream(metricaServiceSettingsFile)
                    );
                }
            } catch (Throwable ex) {
                DebugLogger.error(TAG, ex);
            }
        } else {
            DebugLogger.info(TAG, "not setting Metrica service delay from non-main process");
        }
    }

    @AnyThread
    public synchronized void removeDelay(@NonNull Context context) {
        if (processDetector.isMainProcess()) {
            try {
                cachedDelay = 0L;
                File metricaServiceSettingsFile = fileProvider
                        .getFileFromStorage(context, METRICA_SERVICE_SETTING_FILENAME);
                boolean deleted = metricaServiceSettingsFile != null && metricaServiceSettingsFile.delete();
                DebugLogger.info(TAG, "Removing Metrics service delay. Success? %b", deleted);
            } catch (Throwable ex) {
                DebugLogger.error(TAG, ex);
            }
        } else {
            DebugLogger.info(TAG, "not removing Metrica service delay from non-main process");
        }
    }

    public void maybeDelay(@NonNull Context context) {
        // wait before set delay or remove delay finish
        synchronized (this) {}
        if (!delayHappened) {
            long timeout = getTimeout(context);
            DebugLogger.info(TAG, "delay connection for %d milliseconds", timeout);
            if (timeout > 0) {
                try {
                    Thread.sleep(timeout);
                } catch (Throwable ex) {
                    DebugLogger.error(TAG, ex);
                }
            }
            delayHappened = true;
        } else {
            DebugLogger.info(TAG, "not delaying connection because it is not the first one");
        }
    }

    private synchronized long getTimeout(@NonNull Context context) {
        if (cachedDelay != null) {
            return cachedDelay;
        }
        long result = 0;
        try {
            String value = IOUtils.getStringFileLocked(
                    fileProvider.getFileFromStorage(context, METRICA_SERVICE_SETTING_FILENAME)
            );
            if (!TextUtils.isEmpty(value)) {
                JSONObject json = new JSONObject(value);
                result = json.optLong(KEY_DELAY);
            }
        } catch (Throwable ex) {
            DebugLogger.error(TAG, ex);
        }
        return result;
    }
}
