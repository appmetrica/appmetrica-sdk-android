package io.appmetrica.analytics.impl.utils;

import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.Constants;
import io.appmetrica.analytics.impl.startup.FeaturesInternal;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonHelper {

    private static final String TAG = "[JsonHelper]";

    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_DPI = "dpi";
    private static final String KEY_SCALE_FACTOR = "scaleFactor";
    private static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String KEY_LIB_SSL_ENABLED = "libSslEnabled";

    public static class OptJSONObject extends JSONObject {

        public OptJSONObject() {
            super();
        }

        public OptJSONObject(String value) throws JSONException {
            super(value);
        }

        public String getStringOrEmpty(final String name) {
            String result = StringUtils.EMPTY;
            if (super.has(name)) {
                try {
                    result = super.getString(name);
                } catch (Throwable th) {
                    YLogger.e(th, th.getMessage());
                }
            } else {
                YLogger.e(new JSONException("No value for " + name), "%s", TAG);
            }
            return result;
        }

        public String getStringIfContainsOrEmpty(final String name) {
            return super.has(name) ? getStringOrEmpty(name) : StringUtils.EMPTY;
        }

        public Object get(final String name, final Object defValue) {
            try {
                return super.get(name);
            } catch (Throwable exception) {
                return defValue;
            }
        }

        public boolean hasNotNull(final String name) {
            try {
                return NULL != super.get(name);
            } catch (Throwable exception) {
                return false;
            }
        }

        @Nullable
        public Long getLongSilently(String key) {
            try {
                return getLong(key);
            } catch (Throwable e) {
                return null;
            }
        }

        @Nullable
        public Boolean getBooleanSilently(String key) {
            try {
                return getBoolean(key);
            } catch (Throwable e) {
                return null;
            }
        }
    }

    @VisibleForTesting
    public static Object prepareForJson(Object source) {
        if (source == null) {
            return null;
        }
        try {
            if (source.getClass().isArray()) {
                final int length = Array.getLength(source);
                ArrayList<Object> values = new ArrayList<Object>(length);
                for (int i = 0; i < length; ++i) {
                    values.add(prepareForJson(Array.get(source, i)));
                }
                return new JSONArray(values);
            }
            if (source instanceof Collection) {
                return prepareCollectionForJson((Collection<?>) source);
            }
            if (source instanceof Map) {
                return prepareMapForJson((Map) source);
            }
            if (source instanceof Number) {
                if (Double.isInfinite(((Number) source).doubleValue()) ||
                    Double.isNaN(((Number) source).doubleValue())) {
                    return null;
                }
            }
        } catch (Throwable ignored) {
            return null;
        }
        return source;
    }

    @Nullable
    private static JSONArray prepareCollectionForJson(@Nullable Collection<?> source) {
        if (source == null) {
            return null;
        }
        try {
            ArrayList<Object> values = new ArrayList<Object>(source.size());
            for (Object obj : source) {
                values.add(prepareForJson(obj));
            }
            return new JSONArray(values);
        } catch (Throwable ignored) {
        }
        return null;
    }

    @NonNull
    private static JSONObject prepareMapForJson(@NonNull Map map) {
        Map<?, ?> contentsTyped = (Map<?, ?>) map;
        LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<String, Object>();
        for (Map.Entry<?, ?> entry : contentsTyped.entrySet()) {
            String key = entry.getKey().toString();
            if (key != null) {
                jsonMap.put(key, prepareForJson(entry.getValue()));
            }
        }
        return new JSONObject(jsonMap);
    }

    @Nullable
    public static String clidsToString(@Nullable Map<String, String> clidsMap) {
        if (clidsMap == null) {
            return null;
        } else if (clidsMap.isEmpty()) {
            return StringUtils.EMPTY;
        } else {
            return mapToJsonString(clidsMap);
        }
    }

    @Nullable
    public static Map<String, String> clidsFromString(@Nullable String clidsStr) {
        if (clidsStr == null) {
            return null;
        } else if (clidsStr.isEmpty()) {
            return new HashMap<String, String>();
        } else {
            return jsonToMap(clidsStr);
        }
    }

    @Nullable
    public static JSONObject mapToJsonNullEmptyWise(@Nullable final Map map) {
        if (map == null) {
            return null;
        } else if (map.isEmpty()) {
            return new JSONObject();
        } else {
            return mapToJson(map);
        }
    }

    @Nullable
    public static JSONObject mapToJson(@Nullable final Map map) {
        if (Utils.isNullOrEmpty(map)) {
            return null;
        }
        return prepareMapForJson(map);
    }

    @Nullable
    public static String mapToJsonString(@Nullable final Map map) {
        if (Utils.isNullOrEmpty(map)) {
            return null;
        }
        String value;
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.KITKAT)) {
            value = new JSONObject(map).toString();
        } else {
            value = prepareForJson(map).toString();
        }
        return value;
    }

    @Nullable
    public static JSONArray setToJsonNullEmptyWise(@Nullable final Set<String> set) {
        List<String> list = set == null ? null : new ArrayList<String>(set);
        return listToJsonNullEmptyWise(list);
    }

    @Nullable
    public static JSONArray listToJsonNullEmptyWise(@Nullable final List<String> list) {
        if (list == null) {
            return null;
        } else if (list.isEmpty()) {
            return new JSONArray();
        } else {
            return listToJson(list);
        }
    }

    @Nullable
    public static JSONArray listToJson(@Nullable final List<?> list) {
        if (Utils.isNullOrEmpty(list)) {
            return null;
        }
        JSONArray value;
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.KITKAT)) {
            value = new JSONArray(list);
        } else {
            value = prepareCollectionForJson(list);
        }
        return value;
    }

    @Nullable
    public static String listToJsonString(@Nullable final List<String> list) {
        if (Utils.isNullOrEmpty(list)) {
            return null;
        }
        String value;
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.KITKAT)) {
            value = new JSONArray(list).toString();
        } else {
            value = prepareForJson(list).toString();
        }
        return value;
    }

    @Nullable
    public static HashMap<String, String> jsonToMap(final String json) {
        if (!TextUtils.isEmpty(json)) {
            try {
                return jsonToMap(new JSONObject(json));
            } catch (Throwable e) {
                YLogger.e(e, "Exception while parsing json");
            }
        }
        return null;
    }

    @NonNull
    public static HashMap<String, String> jsonToMapUnsafe(@NonNull String json) throws JSONException {
        return jsonToMap(new JSONObject(json));
    }

    @Nullable
    public static List<String> jsonToList(@Nullable final String json) {
        List<String> result = null;
        if (TextUtils.isEmpty(json) == false) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                result = new ArrayList<String>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    result.add(jsonArray.getString(i));
                }
            } catch (Throwable e) {
                YLogger.e(e, e.getMessage());
            }
        }
        return result;
    }

    @Nullable
    public static HashMap<String, String> jsonToMap(final JSONObject json) {
        HashMap<String, String> result = null;
        if (!JSONObject.NULL.equals(json)) {
            result = new HashMap<String, String>();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = json.optString(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
        }
        return result;
    }

    @Nullable
    public static Long optLongOrDefaultWithBackup(@Nullable JSONObject primary,
                                                  @Nullable JSONObject backup,
                                                  @NonNull String key,
                                                  @Nullable Long fallback) {
        return JsonUtils.optLongOrDefault(backup, key, JsonUtils.optLongOrDefault(primary, key, fallback));
    }

    public static String optStringOrDefaultWithBackup(@Nullable JSONObject primary,
                                                      @Nullable JSONObject backup,
                                                      @NonNull String key) {
        return JsonUtils.optStringOrNullable(backup, key, JsonUtils.optStringOrNull(primary, key));
    }

    @Nullable
    public static Integer optIntegerOrNull(@Nullable JSONObject jsonObject, @NonNull String key) {
        return optIntegerOrDefault(jsonObject, key, null);
    }

    @Nullable
    public static Integer optIntegerOrDefault(@Nullable JSONObject jsonObject,
                                              @NonNull String key,
                                              @Nullable Integer fallback) {
        Integer result = fallback;
        if (jsonObject != null && jsonObject.has(key)) {
            try {
                result = jsonObject.getInt(key);
            } catch (Throwable e) {
                YLogger.e(e, e.getMessage());
            }
        }
        return result;
    }

    @Nullable
    public static Integer optIntegerOrDefaultWithBackup(@Nullable JSONObject primary,
                                                        @Nullable JSONObject backup,
                                                        @NonNull String key,
                                                        @Nullable Integer fallback) {
        return optIntegerOrDefault(backup, key, optIntegerOrDefault(primary, key, fallback));
    }

    @Nullable
    public static Boolean optBooleanOrDefaultWithBackup(@Nullable JSONObject primary,
                                                        @Nullable JSONObject backup,
                                                        @NonNull String key,
                                                        @Nullable Boolean fallback) {
        return JsonUtils.optBooleanOrNullable(
            backup,
            key,
            JsonUtils.optBooleanOrNullable(primary, key, fallback)
        );
    }

    @Nullable
    public static byte[] optHexByteArray(@NonNull JSONObject jsonObject,
                                         @NonNull String key,
                                         byte[] fallback) {
        byte[] result = fallback;
        String stringToDecode = JsonUtils.optStringOrNull(jsonObject, key);
        if (stringToDecode != null) {
            try {
                result = StringUtils.hexToBytes(stringToDecode);
            } catch (Throwable e) {
                YLogger.e(e, "%sFail to decode hex string: %s", TAG, stringToDecode);
            }
        }

        return result;
    }

    @Nullable
    public static List<String> toStringList(@Nullable JSONArray jsonArray) throws JSONException {
        List<String> items = null;
        if (jsonArray != null && jsonArray.length() > 0) {
            items = new ArrayList<String>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                items.add(jsonArray.getString(i));
            }
        }
        return items;
    }

    public static JSONObject statSendingDisabledValue() throws JSONException {
        return new JSONObject()
            .put("stat_sending", new JSONObject()
                .put("disabled", true));
    }

    @NonNull
    public static JSONObject autoInappCollectingConfigToJson(@NonNull BillingConfig config) throws JSONException {
        return new JSONObject()
            .put(
                io.appmetrica.analytics.impl.billing.Constants.SEND_FREQUENCY_SECONDS,
                config.sendFrequencySeconds
            )
            .put(
                io.appmetrica.analytics.impl.billing.Constants.FIRST_COLLECTING_INAPP_MAX_AGE_SECONDS,
                config.firstCollectingInappMaxAgeSeconds
            );
    }

    @NonNull
    public static BillingConfig autoInappCollectingConfigFromJson(@NonNull JSONObject jsonObject) throws JSONException {
        StartupStateProtobuf.StartupState.AutoInappCollectingConfig defaultConfig =
            new StartupStateProtobuf.StartupState.AutoInappCollectingConfig();
        return new BillingConfig(
            jsonObject.optInt(
                io.appmetrica.analytics.impl.billing.Constants.SEND_FREQUENCY_SECONDS,
                defaultConfig.sendFrequencySeconds
            ),
            jsonObject.optInt(
                io.appmetrica.analytics.impl.billing.Constants.FIRST_COLLECTING_INAPP_MAX_AGE_SECONDS,
                defaultConfig.firstCollectingInappMaxAgeSeconds
            )
        );
    }

    @NonNull
    public static JSONObject advIdentifiersResultToJson(@Nullable IdentifiersResult identifiersResult) {
        JSONObject json = new JSONObject();
        if (identifiersResult != null) {
            try {
                json
                    .put(
                        Constants.AdvIdentifiersResultKeys.ID,
                        identifiersResult.id
                    )
                    .put(
                        Constants.AdvIdentifiersResultKeys.STATUS,
                        identifiersResult.status.getValue()
                    )
                    .put(
                        Constants.AdvIdentifiersResultKeys.ERROR_EXPLANATION,
                        identifiersResult.errorExplanation
                    );
            } catch (Throwable ex) {
                YLogger.e(ex, TAG);
            }
        }
        return json;
    }

    @NonNull
    public static IdentifiersResult advIdentifiersResultFromJson(@NonNull JSONObject json) {
        return new IdentifiersResult(
            JsonUtils.optStringOrNull(json, Constants.AdvIdentifiersResultKeys.ID),
            IdentifierStatus.from(JsonUtils.optStringOrNull(
                json,
                Constants.AdvIdentifiersResultKeys.STATUS
            )),
            JsonUtils.optStringOrNull(
                json,
                Constants.AdvIdentifiersResultKeys.ERROR_EXPLANATION
            )
        );
    }

    @NonNull
    public static ScreenInfo screenInfoFromJson(@NonNull JSONObject json) {
        return new ScreenInfo(
            json.optInt(KEY_WIDTH),
            json.optInt(KEY_HEIGHT),
            json.optInt(KEY_DPI),
            (float) json.optDouble(KEY_SCALE_FACTOR, 0)
        );
    }

    @Nullable
    public static ScreenInfo screenInfoFromJsonString(@Nullable String value) {
        try {
            if (!TextUtils.isEmpty(value)) {
                JSONObject jsonObject = new JSONObject(value);
                return JsonHelper.screenInfoFromJson(jsonObject);
            }
        } catch (Throwable e) {
            YLogger.error(TAG, e);
        }
        return null;
    }

    @Nullable
    public static JSONObject screenInfoToJson(@Nullable ScreenInfo screenInfo) {
        if (screenInfo == null) {
            return null;
        }
        JSONObject result = new JSONObject();
        try {
            result
                .put(KEY_WIDTH, screenInfo.getWidth())
                .put(KEY_HEIGHT, screenInfo.getHeight())
                .put(KEY_DPI, screenInfo.getDpi())
                .put(KEY_SCALE_FACTOR, screenInfo.getScaleFactor());
        } catch (Throwable ignored) {
        }
        return result;
    }

    @Nullable
    public static String screenInfoToJsonString(@Nullable ScreenInfo screenInfo) {
        JSONObject jsonObject = screenInfoToJson(screenInfo);
        return jsonObject == null ? null : jsonObject.toString();
    }

    @Nullable
    public static String customSdkHostsToString(@Nullable Map<String, List<String>> input) {
        if (input == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, List<String>> entry : input.entrySet()) {
                JSONArray array = JsonHelper.listToJson(entry.getValue());
                if (array != null) {
                    json.put(entry.getKey(), array.toString());
                }
            }
        } catch (Throwable ex) {
            YLogger.error(TAG, ex);
        }
        return json.toString();
    }

    @Nullable
    public static Map<String, List<String>> customSdkHostsFromString(@Nullable String input) {
        if (input == null) {
            return null;
        }
        Map<String, List<String>> result = new HashMap<>();
        try {
            final JSONObject json = new JSONObject(input);
            Iterator<String> keyIterator = json.keys();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                try {
                    final List<String> list = JsonHelper.toStringList(new JSONArray(json.optString(key)));
                    if (list != null) {
                        result.put(key, list);
                    }
                } catch (Throwable ex) {
                    YLogger.error(TAG, ex);
                }
            }
        } catch (Throwable ex) {
            YLogger.error(TAG, ex);
        }
        return result;
    }

    @Nullable
    public static List<String> toStringList(@Nullable String stringArray) {
        if (stringArray == null) {
            return null;
        }
        try {
            return toStringList(new JSONArray(stringArray));
        } catch (Throwable ex) {
            YLogger.error(TAG, ex);
        }
        return null;
    }

    @NonNull
    public static FeaturesInternal featuresFromJson(@Nullable String value) {
        try {
            if (!TextUtils.isEmpty(value)) {
                JSONObject json = new JSONObject(value);
                return new FeaturesInternal(
                    JsonUtils.optBooleanOrNull(json, KEY_LIB_SSL_ENABLED),
                    IdentifierStatus.from(JsonUtils.optStringOrNull(
                        json,
                        Constants.AdvIdentifiersResultKeys.STATUS
                    )),
                    JsonUtils.optStringOrNull(
                        json,
                        Constants.AdvIdentifiersResultKeys.ERROR_EXPLANATION
                    )
                );
            }
        } catch (Throwable ex) {
            YLogger.error(TAG, ex);
        }
        return new FeaturesInternal();
    }

    @NonNull
    public static String featuresToJson(@NonNull FeaturesInternal features) {
        JSONObject json = new JSONObject();
        try {
            json
                .putOpt(KEY_LIB_SSL_ENABLED, features.getSslPinning())
                .put(
                    Constants.AdvIdentifiersResultKeys.STATUS,
                    features.getStatus().getValue()
                )
                .putOpt(
                    Constants.AdvIdentifiersResultKeys.ERROR_EXPLANATION,
                    features.getErrorExplanation()
                );
        } catch (Throwable ignored) {
        }
        return json.toString();
    }
}
