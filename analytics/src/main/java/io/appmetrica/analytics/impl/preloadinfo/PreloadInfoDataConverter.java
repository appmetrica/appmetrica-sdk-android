package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import java.util.ArrayList;
import java.util.List;

public class PreloadInfoDataConverter implements ProtobufConverter<PreloadInfoData, PreloadInfoProto.PreloadInfoData> {

    @NonNull
    private final PreloadInfoStateConverter stateConverter;
    @NonNull
    private final PreloadInfoCandidateConverter candidateConverter;

    public PreloadInfoDataConverter() {
        this(new PreloadInfoStateConverter(), new PreloadInfoCandidateConverter());
    }

    @VisibleForTesting
    PreloadInfoDataConverter(@NonNull PreloadInfoStateConverter stateConverter,
                             @NonNull PreloadInfoCandidateConverter candidateConverter) {
        this.stateConverter = stateConverter;
        this.candidateConverter = candidateConverter;
    }

    @NonNull
    @Override
    public PreloadInfoProto.PreloadInfoData fromModel(@NonNull PreloadInfoData value) {
        PreloadInfoProto.PreloadInfoData nano = new PreloadInfoProto.PreloadInfoData();
        nano.chosenPreloadInfo = stateConverter.fromModel(value.chosenPreloadInfo);
        nano.candidates = new PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate[value.candidates.size()];
        int index = 0;
        for (PreloadInfoData.Candidate candidate : value.candidates) {
            nano.candidates[index] = candidateConverter.fromModel(candidate);
            index++;
        }
        return nano;
    }

    @NonNull
    @Override
    public PreloadInfoData toModel(@NonNull PreloadInfoProto.PreloadInfoData nano) {
        List<PreloadInfoData.Candidate> candidates = new ArrayList<PreloadInfoData.Candidate>(nano.candidates.length);
        for (PreloadInfoProto.PreloadInfoData.PreloadInfoCandidate candidate : nano.candidates) {
            candidates.add(candidateConverter.toModel(candidate));
        }
        return new PreloadInfoData(stateToModel(nano.chosenPreloadInfo), candidates);
    }

    @NonNull
    private PreloadInfoState stateToModel(@Nullable PreloadInfoProto.PreloadInfoData.PreloadInfo nano) {
        if (nano == null) {
            return stateConverter.toModel(new PreloadInfoProto.PreloadInfoData.PreloadInfo());
        } else {
            return stateConverter.toModel(nano);
        }
    }
}
