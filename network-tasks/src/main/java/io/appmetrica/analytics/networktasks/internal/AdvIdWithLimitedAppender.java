package io.appmetrica.analytics.networktasks.internal;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;

public class AdvIdWithLimitedAppender implements IParamsAppender<AdvertisingIdsHolder> {

    private static final String TAG = "[AdvIdWithLimitedAppender]";

    @Override
    public void appendParams(
            @NonNull Uri.Builder uriBuilder,
            @NonNull AdvertisingIdsHolder advertisingIdsHolder
    ) {
        YLogger.info(TAG, "append adv id params with holder: %s", advertisingIdsHolder);
        appendAdvIdInfo(
                uriBuilder,
                CommonUrlParts.ADV_ID,
                CommonUrlParts.LIMIT_AD_TRACKING,
                advertisingIdsHolder.getGoogle().mAdTrackingInfo
        );
        appendAdvIdInfo(
                uriBuilder,
                CommonUrlParts.HUAWEI_OAID,
                CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING,
                advertisingIdsHolder.getHuawei().mAdTrackingInfo
        );
        appendAdvIdInfo(
                uriBuilder,
                CommonUrlParts.YANDEX_ADV_ID,
                CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING,
                advertisingIdsHolder.getYandex().mAdTrackingInfo
        );
    }

    private void appendAdvIdInfo(@NonNull Uri.Builder uriBuilder,
                                 @NonNull String idParameter,
                                 @NonNull String limitedParameter,
                                 @Nullable AdTrackingInfo info) {
        if (info == null)  {
            uriBuilder.appendQueryParameter(idParameter, "");
            uriBuilder.appendQueryParameter(limitedParameter, "");
        } else {
            uriBuilder.appendQueryParameter(idParameter, StringUtils.emptyIfNull(info.advId));
            uriBuilder.appendQueryParameter(limitedParameter, toGetParam(info.limitedAdTracking));
        }
    }

    @VisibleForTesting
    static String toGetParam(Boolean value) {
        return value == null ? "" :
                value ? CommonUrlParts.Values.TRUE_INTEGER : CommonUrlParts.Values.FALSE_INTEGER;
    }
}
