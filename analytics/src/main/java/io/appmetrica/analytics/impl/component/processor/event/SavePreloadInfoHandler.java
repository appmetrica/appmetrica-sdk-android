package io.appmetrica.analytics.impl.component.processor.event;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class SavePreloadInfoHandler extends ReportComponentHandler {

    private static final String TAG = "[SavePreloadInfoHandler]";

    @NonNull
    private final PreloadInfoStorage mPreloadInfoStorage;

    public SavePreloadInfoHandler(@NonNull ComponentUnit component) {
        this(
                component,
                GlobalServiceLocator.getInstance().getPreloadInfoStorage()
        );
    }

    @VisibleForTesting
    SavePreloadInfoHandler(@NonNull ComponentUnit component, @NonNull PreloadInfoStorage preloadInfoStorage) {
        super(component);
        mPreloadInfoStorage = preloadInfoStorage;
    }

    @Override
    public boolean process(@NonNull CounterReport reportData) {
        final String reportValue = reportData.getValue();
        JSONObject preloadInfoJson = null;
        try {
            JSONObject valueJson = new JSONObject(reportValue);
            preloadInfoJson = valueJson.optJSONObject(PreloadInfoState.JsonKeys.PRELOAD_INFO);
        } catch (Throwable ex) {
            DebugLogger.INSTANCE.error(TAG, ex);
        }
        mPreloadInfoStorage.updateIfNeeded(PreloadInfoState.fromJson(preloadInfoJson));
        return false;
    }
}
