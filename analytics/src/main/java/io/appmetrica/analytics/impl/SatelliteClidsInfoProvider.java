package io.appmetrica.analytics.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import java.util.HashMap;
import java.util.Map;

public class SatelliteClidsInfoProvider implements SatelliteDataProvider<ClidsInfo.Candidate> {

    private static final class Constants {
        private static final String URI = "content://com.yandex.preinstallsatellite.appmetrica.provider/clids";
        private static final String COL_CLID_KEY = "clid_key";
        private static final String COL_CLID_VALUE = "clid_value";
    }

    private static final String TAG = "[SatelliteClidsInfoProvider]";

    private final Context context;

    public SatelliteClidsInfoProvider(@NonNull Context context) {
        this.context = context;
    }

    @Override
    @Nullable
    public ClidsInfo.Candidate invoke() {
        Map<String, String> clidsFromSatellite = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(
                    Uri.parse(Constants.URI),
                    null,
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                clidsFromSatellite = new HashMap<String, String>();
                while (cursor.moveToNext()) {
                    try {
                        String clidKey = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CLID_KEY));
                        String clidValue = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CLID_VALUE));
                        if (!TextUtils.isEmpty(clidKey) && !TextUtils.isEmpty(clidValue)) {
                            clidsFromSatellite.put(clidKey, clidValue);
                        } else {
                            YLogger.warning(TAG, "Invalid clid {%s : %s}", clidKey, clidValue);
                            SdkUtils.logAttribution("Invalid clid {%s : %s}", clidKey, clidValue);
                        }
                    } catch (Throwable ex) {
                        YLogger.error(TAG, ex);
                    }
                }
                YLogger.info(TAG, "Parsed clids: %s", clidsFromSatellite);
                SdkUtils.logAttribution("Clids from satellite: %s", clidsFromSatellite);
                return new ClidsInfo.Candidate(clidsFromSatellite, DistributionSource.SATELLITE);
            } else {
                YLogger.info(TAG, "Failed to retrieve cursor");
                SdkUtils.logAttribution("No Satellite content provider found");
            }
        } catch (Throwable ex) {
            YLogger.error(TAG, ex);
            SdkUtils.logAttributionE(ex, "Error while getting satellite clids");
        } finally {
            Utils.closeCursor(cursor);
        }
        return null;
    }
}
