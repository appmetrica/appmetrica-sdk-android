package io.appmetrica.analytics.impl.features;

import android.annotation.TargetApi;
import android.content.pm.FeatureInfo;
import android.os.Build;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;

public abstract class FeatureAdapter {

    protected abstract FeatureDescription adoptFeature(FeatureInfo feature);

    public FeatureDescription adapt(@NonNull FeatureInfo info) {
        if (info.name == null) {
            if (info.reqGlEsVersion == FeatureInfo.GL_ES_VERSION_UNDEFINED) {
                return adoptFeature(info);
            } else {
                return new FeatureDescription(
                        FeatureDescription.OPEN_GL_FEATURE,
                        info.reqGlEsVersion,
                        isRequired(info)
                    );
            }
        } else {
            return adoptFeature(info);
        }
    }

    boolean isRequired(FeatureInfo feature) {
        return (feature.flags & FeatureInfo.FLAG_REQUIRED) != 0;
    }

    public static class Factory {

        public static FeatureAdapter create() {
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)) {
                return new FeatureAdapterWithVersion();
            } else {
                return new FeatureAdapterWithoutVersion();
            }
        }

    }

    public static class FeatureAdapterWithVersion extends FeatureAdapter {
        @TargetApi(Build.VERSION_CODES.N)
        public FeatureDescription adoptFeature(@NonNull FeatureInfo feature) {
            return new FeatureDescription(feature.name, feature.version, isRequired(feature));
        }
    }

    public static class FeatureAdapterWithoutVersion extends FeatureAdapter {
        public FeatureDescription adoptFeature(@NonNull FeatureInfo feature) {
            return new FeatureDescription(feature.name, isRequired(feature));
        }
    }

}
