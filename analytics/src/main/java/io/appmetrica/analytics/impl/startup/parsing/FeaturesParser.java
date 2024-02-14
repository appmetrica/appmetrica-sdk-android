package io.appmetrica.analytics.impl.startup.parsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.logger.internal.YLogger;
import org.json.JSONException;
import org.json.JSONObject;

public class FeaturesParser {

    private static final String TAG = "[FeaturesParser]";

    void parse(@NonNull StartupResult startupResult, @NonNull JsonHelper.OptJSONObject response) {

        StartupStateProtobuf.StartupState.Flags protoFeatures = new StartupStateProtobuf.StartupState.Flags();

        try {
            JSONObject features = (JSONObject) response.get(JsonResponseKey.FEATURES, new JSONObject());
            JSONObject featuresList = features.optJSONObject(JsonResponseKey.LIST);

            if (featuresList != null) {
                CollectingFlags collectingFlags = new CollectingFlags.CollectingFlagsBuilder()
                        .withPermissionsCollectingEnabled(extractFeature(
                                featuresList,
                                JsonResponseKey.FEATURE_PERMISSION_COLLECTING,
                                protoFeatures.permissionsCollectingEnabled
                        ))
                        .withFeaturesCollectingEnabled(extractFeature(
                                featuresList,
                                JsonResponseKey.FEATURE_FEATURES_COLLECTING,
                                protoFeatures.featuresCollectingEnabled
                        ))
                        .withGoogleAid(extractFeature(featuresList,
                                JsonResponseKey.FEATURE_GOOGLE_AID, protoFeatures.googleAid))
                        .withSimInfo(extractFeature(featuresList,
                                JsonResponseKey.FEATURE_SIM_INFO, protoFeatures.simInfo))
                        .withHuaweiOaid(extractFeature(featuresList,
                                JsonResponseKey.FEATURE_HUAWEI_OAID, protoFeatures.huaweiOaid))
                        .withSslPinning(extractNullableFeature(featuresList, JsonResponseKey.FEATURE_SSL_PINNING))
                        .build();

                startupResult.setCollectingFlags(collectingFlags);
            }
        } catch (Throwable ex) {
            YLogger.e(ex, "%s", TAG);
        }
    }

    private boolean extractFeature(JSONObject featuresList, String key, boolean defValue) throws JSONException {
        return WrapUtils.getOrDefault(extractNullableFeature(featuresList, key), defValue);
    }

    @Nullable
    private Boolean extractNullableFeature(@NonNull JSONObject featuresList, @NonNull String key) throws JSONException {
        if (featuresList.has(key)) {
            JSONObject feature = featuresList.getJSONObject(key);
            return feature.getBoolean(JsonResponseKey.FEATURE_ENABLED);
        }
        return null;
    }
}
