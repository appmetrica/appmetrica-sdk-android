package io.appmetrica.analytics.impl.preloadinfo;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import org.json.JSONObject;

public class PreloadInfoStateConverter implements
        ProtobufConverter<PreloadInfoState, PreloadInfoProto.PreloadInfoData.PreloadInfo> {

    private static final String TAG = "[PreloadInfoStateConverter]";

    @NonNull
    private final PreloadInfoSourceConverter sourceConverter = new PreloadInfoSourceConverter();

    @NonNull
    @Override
    public PreloadInfoProto.PreloadInfoData.PreloadInfo fromModel(@NonNull PreloadInfoState value) {
        PreloadInfoProto.PreloadInfoData.PreloadInfo nano = new PreloadInfoProto.PreloadInfoData.PreloadInfo();
        if (TextUtils.isEmpty(value.trackingId) == false) {
            nano.trackingId = value.trackingId;
        }
        nano.additionalParameters = value.additionalParameters.toString();
        nano.wasSet = value.wasSet;
        nano.preloadInfoAutoTracking = value.autoTrackingEnabled;
        nano.source = sourceConverter.fromModel(value.source);
        return nano;
    }

    @NonNull
    @Override
    public PreloadInfoState toModel(@NonNull PreloadInfoProto.PreloadInfoData.PreloadInfo nano) {
        return new PreloadInfoState(
                nano.trackingId,
                additionalParametersToJson(nano.additionalParameters),
                nano.wasSet,
                nano.preloadInfoAutoTracking,
                sourceConverter.toModel(nano.source)
        );
    }

    @NonNull
    private JSONObject additionalParametersToJson(@Nullable String params) {
        if (TextUtils.isEmpty(params) == false) {
            try {
                return new JSONObject(params);
            } catch (Throwable ex) {
                YLogger.error(TAG, ex);
            }
        }
        return new JSONObject();
    }
}
