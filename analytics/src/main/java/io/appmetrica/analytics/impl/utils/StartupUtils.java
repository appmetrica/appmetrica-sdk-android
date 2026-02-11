package io.appmetrica.analytics.impl.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.parsing.ParseUtils;
import io.appmetrica.analytics.impl.Utils;
import java.util.HashMap;
import java.util.Map;

public class StartupUtils {

    public static final String KEY_VALUE_SEPARATOR = ":";
    public static final String PAIR_SEPARATOR = ",";

    @NonNull
    public static String encodeClids(final Map<String, String> clids) {
        String param = StringUtils.EMPTY;
        if (!Utils.isNullOrEmpty(clids)) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> clid : clids.entrySet()) {
                sb.append(clid.getKey()).append(KEY_VALUE_SEPARATOR).append(clid.getValue()).append(PAIR_SEPARATOR);
            }
            sb.setLength(sb.length() - 1);
            param = sb.toString();
        }
        return param;
    }

    @NonNull
    public static Map<String, String> decodeClids(@Nullable final String encoded) {
        Map<String, String> result = new HashMap<String, String>();
        if (!StringUtils.isNullOrEmpty(encoded)) {
            String[] pairs = encoded.split(PAIR_SEPARATOR);
            for (String pair : pairs) {
                int i = pair.indexOf(KEY_VALUE_SEPARATOR);
                if (i != -1) {
                    String key = pair.substring(0, i);
                    result.put(key, pair.substring(i + 1));
                }
            }
        }
        return result;
    }

    public static boolean isValidClids(final String encodedClids) {
        Map<String, String> clids = decodeClids(encodedClids);
        return isValidClids(clids);
    }

    public static boolean isValidClids(@Nullable Map<String, String> clids) {
        if (clids == null || clids.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> clidPair : clids.entrySet()) {
            try {
                Integer.parseInt(clidPair.getValue());
            } catch (Throwable e) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, String> validateClids(final Map<String, String> clids) {
        Map<String, String> result = new HashMap<String, String>();
        if (clids != null) {
            for (Map.Entry<String, String> clid : clids.entrySet()) {
                if (isValidClidKey(clid.getKey()) && isValidClidValue(clid.getValue())) {
                    result.put(clid.getKey(), clid.getValue());
                }
            }
        }
        return result;
    }

    private static boolean isValidClidValue(final String value) {
        return !StringUtils.isNullOrEmpty(value) && ParseUtils.parseLong(value, -1) != -1;
    }

    private static boolean isValidClidKey(final String key) {
        return !StringUtils.isNullOrEmpty(key)
                && !key.contains(":") && !key.contains(",") && !key.contains("&");
    }
}
