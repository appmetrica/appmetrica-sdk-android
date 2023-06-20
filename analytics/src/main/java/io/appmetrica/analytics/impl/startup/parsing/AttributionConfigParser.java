package io.appmetrica.analytics.impl.startup.parsing;

import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AttributionConfigParser {

    public static final String KEY_ATTRIBUTION = "attribution";
    public static final String KEY_DEEPLINK_CONDITIONS = "deeplink_conditions";
    public static final String KEY_KEY = "key";
    public static final String KEY_VALUE = "value";

    void parse(@NonNull StartupResult result, @NonNull JSONObject response) {
        JSONObject attributionJson = response.optJSONObject(KEY_ATTRIBUTION);
        if (attributionJson != null) {
            List<Pair<String, AttributionConfig.Filter>> deeplinkConditions =
                    new ArrayList<Pair<String, AttributionConfig.Filter>>();
            JSONArray deeplinkConditionsJson = attributionJson.optJSONArray(KEY_DEEPLINK_CONDITIONS);
            if (deeplinkConditionsJson != null) {
                for (int i = 0; i < deeplinkConditionsJson.length(); i++) {
                    JSONObject deeplinkConditionJson = deeplinkConditionsJson.optJSONObject(i);
                    String key = deeplinkConditionJson.optString(KEY_KEY, null);
                    if (!TextUtils.isEmpty(key)) {
                        deeplinkConditions.add(
                                new Pair<String, AttributionConfig.Filter>(key, getFilter(deeplinkConditionJson))
                        );
                    }
                }
            }
            result.setAttributionConfig(new AttributionConfig(deeplinkConditions));
        }
    }

    @Nullable
    private AttributionConfig.Filter getFilter(@NonNull JSONObject deeplinkConditionJson) {
        String filterValue = deeplinkConditionJson.optString(KEY_VALUE,null);
        final AttributionConfig.Filter filter;
        if (filterValue == null) {
            filter = null;
        } else {
            filter = new AttributionConfig.Filter(filterValue);
        }
        return filter;
    }
}
