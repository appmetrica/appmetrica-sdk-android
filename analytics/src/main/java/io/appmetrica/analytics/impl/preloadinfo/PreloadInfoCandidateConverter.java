package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class PreloadInfoCandidateConverter implements
        ProtobufConverter<PreloadInfoData.Candidate, PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate> {

    private static final String TAG = "[PreloadInfoCandidateConverter]";

    @NonNull
    private final PreloadInfoSourceConverter sourceConverter;

    public PreloadInfoCandidateConverter() {
        this(new PreloadInfoSourceConverter());
    }

    @VisibleForTesting
    PreloadInfoCandidateConverter(@NonNull PreloadInfoSourceConverter sourceConverter) {
        this.sourceConverter = sourceConverter;
    }

    @NonNull
    @Override
    public PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate fromModel(@NonNull PreloadInfoData.Candidate value) {
        PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate nano =
                new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate();
        if (!StringUtils.isNullOrEmpty(value.trackingId)) {
            nano.trackingId = value.trackingId;
        }
        nano.additionalParameters = value.additionalParams.toString();
        nano.source = sourceConverter.fromModel(value.source);
        return nano;
    }

    @NonNull
    @Override
    public PreloadInfoData.Candidate toModel(@NonNull PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate nano) {
        return new PreloadInfoData.Candidate(
                nano.trackingId,
                additionalParametersToJson(nano.additionalParameters),
                sourceConverter.toModel(nano.source)
        );
    }

    @NonNull
    private JSONObject additionalParametersToJson(@Nullable String params) {
        if (!StringUtils.isNullOrEmpty(params)) {
            try {
                return new JSONObject(params);
            } catch (Throwable ex) {
                DebugLogger.INSTANCE.error(TAG, ex);
            }
        }
        return new JSONObject();
    }
}
