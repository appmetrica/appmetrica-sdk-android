package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.DistributionInfo;
import io.appmetrica.analytics.impl.DistributionSource;
import java.util.List;
import org.json.JSONObject;

public class PreloadInfoData implements DistributionInfo<PreloadInfoData.Candidate, PreloadInfoState> {

    public static class Candidate {

        @Nullable
        public final String trackingId;
        @NonNull
        public final JSONObject additionalParams;
        @NonNull
        public final DistributionSource source;

        public Candidate(@Nullable String trackingId,
                         @NonNull JSONObject additionalParams,
                         @NonNull DistributionSource source) {
            this.trackingId = trackingId;
            this.additionalParams = additionalParams;
            this.source = source;
        }

        @Override
        public String toString() {
            return "Candidate{" +
                    "trackingId='" + trackingId + '\'' +
                    ", additionalParams=" + additionalParams +
                    ", source=" + source +
                    '}';
        }
    }

    @NonNull
    public final PreloadInfoState chosenPreloadInfo;
    @NonNull
    public final List<Candidate> candidates;

    public PreloadInfoData(@NonNull PreloadInfoState chosenPreloadInfo,
                           @NonNull List<Candidate> candidates) {
        this.chosenPreloadInfo = chosenPreloadInfo;
        this.candidates = candidates;
    }

    @Nullable
    @Override
    public PreloadInfoState getChosen() {
        return chosenPreloadInfo;
    }

    @Override
    @NonNull
    public List<Candidate> getCandidates() {
        return candidates;
    }

    @Override
    public String toString() {
        return "PreloadInfoData{" +
                "chosenPreloadInfo=" + chosenPreloadInfo +
                ", candidates=" + candidates +
                '}';
    }
}
