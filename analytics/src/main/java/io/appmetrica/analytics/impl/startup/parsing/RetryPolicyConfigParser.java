package io.appmetrica.analytics.impl.startup.parsing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import org.json.JSONObject;

public class RetryPolicyConfigParser {

    void parse(@NonNull StartupResult result, @NonNull JsonHelper.OptJSONObject response) {
        StartupStateProtobuf.StartupState emptyProto =
                new StartupStateProtobuf.StartupState();
        JSONObject configBlock = response.optJSONObject(JsonResponseKey.RETRY_POLICY_CONFIG);
        int maxIntervalSeconds = emptyProto.maxRetryIntervalSeconds;
        int exponentialMultiplier = emptyProto.retryExponentialMultiplier;
        if (configBlock != null) {
            maxIntervalSeconds = configBlock.optInt(JsonResponseKey.RETRY_POLICY_MAX_INTERVAL,
                    emptyProto.maxRetryIntervalSeconds);
            exponentialMultiplier = configBlock.optInt(JsonResponseKey.RETRY_POLICY_EXPONENTIAL_MULTIPLIER,
                    emptyProto.retryExponentialMultiplier);
        }
        result.setRetryPolicyConfig(new RetryPolicyConfig(maxIntervalSeconds, exponentialMultiplier));
    }
}
