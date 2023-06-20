package io.appmetrica.analytics.impl.preloadinfo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.SatelliteDataProvider;
import io.appmetrica.analytics.impl.SdkUtils;
import io.appmetrica.analytics.impl.Utils;
import org.json.JSONObject;

public class PreloadInfoFromSatelliteProvider implements SatelliteDataProvider<PreloadInfoState> {

    private static final class Constants {
        private static final String URI = "content://com.yandex.preinstallsatellite.appmetrica.provider/preload_info";
        private static final String COL_TRACKING_ID = "tracking_id";
        private static final String COL_ADDITIONAL_PARAMETERS = "additional_parameters";
    }

    private static final String TAG = "[PreloadInfoFromSatelliteProvider]";
    private final Context context;

    public PreloadInfoFromSatelliteProvider(@NonNull Context context) {
        this.context = context;
    }

    @Override
    @Nullable
    public PreloadInfoState invoke() {
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(Uri.parse(Constants.URI), null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String trackingId = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_TRACKING_ID));
                    int additionalParamsColumn = cursor.getColumnIndexOrThrow(Constants.COL_ADDITIONAL_PARAMETERS);
                    String additionalParamsString = cursor.getString(additionalParamsColumn);
                    JSONObject additionalParams = new JSONObject();
                    try {
                        if (!TextUtils.isEmpty(additionalParamsString)) {
                            additionalParams = new JSONObject(additionalParamsString);
                        }
                    } catch (Throwable ex) {
                        YLogger.error(TAG, ex, "Could not parse additional parameters");
                    }
                    YLogger.info(TAG, "Parsed tracking id: %s, additional parameters: %s",
                            trackingId, additionalParams);
                    SdkUtils.logAttribution("Preload info from Satellite: {tracking id = %s, " +
                            "additional parameters = %s}", trackingId, additionalParams);
                    return new PreloadInfoState(
                            trackingId,
                            additionalParams,
                            !TextUtils.isEmpty(trackingId),
                            false,
                            DistributionSource.SATELLITE
                    );
                } else {
                    SdkUtils.logAttribution("No Preload Info data in Satellite content provider");
                }
            } else {
                YLogger.info(TAG, "Failed to retrieve cursor");
                SdkUtils.logAttribution("No Satellite content provider found");
            }
        } catch (Throwable ex) {
            YLogger.error(TAG, ex);
        } finally {
            Utils.closeCursor(cursor);
        }
        return null;
    }
}
