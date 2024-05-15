package io.appmetrica.analytics.impl.preloadinfo;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import org.json.JSONObject;

public class PreloadInfoWrapper {

    private static final String TAG = "[PreloadInfoWrapper]";

    @Nullable
    private PreloadInfoState mPreloadInfoState;

    public PreloadInfoWrapper(@Nullable PreloadInfo preloadInfo,
                              @NonNull PublicLogger logger,
                              final boolean autoTracking) {
        if (preloadInfo != null) {
            if (TextUtils.isEmpty(preloadInfo.getTrackingId())) {
                if (logger.isEnabled()) {
                    logger.e("Required field \"PreloadInfo.trackingId\" is empty!\n" +
                            "This preload info will be skipped.");
                }
            } else {
                mPreloadInfoState = new PreloadInfoState(
                        preloadInfo.getTrackingId(),
                        new JSONObject(preloadInfo.getAdditionalParams()),
                        true,
                        autoTracking,
                        DistributionSource.APP
                );
            }
        }
    }

    @NonNull
    public JSONObject addToEventValue(@NonNull JSONObject eventValue) {
        if (mPreloadInfoState != null) {
            try {
                eventValue.put(PreloadInfoState.JsonKeys.PRELOAD_INFO, mPreloadInfoState.toInternalJson());
            } catch (Throwable e) {
                DebugLogger.error(TAG, e, e.getMessage());
            }
        }
        return eventValue;
    }
}
