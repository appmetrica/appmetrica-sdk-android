package io.appmetrica.analytics.impl.startup.parsing;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class HostsParser {

    private static final String TAG = "[HostsParser]";
    private static final Set<String> PREDEFINED_KEYS = new HashSet<>();

    static {
        PREDEFINED_KEYS.add(JsonResponseKey.GET_AD_HOST);
        PREDEFINED_KEYS.add(JsonResponseKey.SEND_REPORTS_HOSTS);
        PREDEFINED_KEYS.add(JsonResponseKey.REPORT_AD_HOST);
        PREDEFINED_KEYS.add(JsonResponseKey.STARTUP_HOSTS);
        PREDEFINED_KEYS.add(JsonResponseKey.DIAGNOSTIC_HOSTS);
    }

    void parse(@NonNull StartupResult startupResult, @NonNull JsonHelper.OptJSONObject response) {
        String value;
        List<String> values;
        try {
            final JSONObject queryHosts = (JSONObject) response.get(JsonResponseKey.QUERY_HOSTS, new JSONObject());
            final JSONObject hosts = queryHosts.optJSONObject(JsonResponseKey.LIST);

            if (hosts != null) {
                value = getDeepSingleUrlField(hosts, JsonResponseKey.GET_AD_HOST);
                if (TextUtils.isEmpty(value) == false) {
                    startupResult.setGetAdUrl(value);
                }

                values = getDeepMultipleUrlsField(hosts, JsonResponseKey.SEND_REPORTS_HOSTS);
                if (Utils.isNullOrEmpty(values) == false) {
                    startupResult.setReportHostUrls(values);
                }

                value = getDeepSingleUrlField(hosts, JsonResponseKey.REPORT_AD_HOST);
                if (TextUtils.isEmpty(value) == false) {
                    startupResult.setReportAdUrl(value);
                }

                values = getDeepMultipleUrlsField(hosts, JsonResponseKey.STARTUP_HOSTS);
                if (Utils.isNullOrEmpty(values) == false) {
                    startupResult.setStartupUrls(values);
                }

                values = getDeepMultipleUrlsField(hosts, JsonResponseKey.DIAGNOSTIC_HOSTS);
                if (Utils.isNullOrEmpty(values) == false) {
                    startupResult.setDiagnosticUrls(values);
                }

                startupResult.setCustomSdkHosts(parseCustomSdkHosts(hosts));
            }
        } catch (Throwable ex) {
            YLogger.e(ex, "%s", TAG);
        }
    }

    @NonNull
    private Map<String, List<String>> parseCustomSdkHosts(@NonNull JSONObject object) {
        Map<String, List<String>> result = new HashMap<>();
        Iterator<String> keyIterator = object.keys();
        while (keyIterator.hasNext()) {
            final String key = keyIterator.next();
            if (!PREDEFINED_KEYS.contains(key)) {
                List<String> hosts = getDeepMultipleUrlsField(object, key);
                if (hosts != null) {
                    result.put(key, hosts);
                }
            }
        }
        return result;
    }

    private String getDeepSingleUrlField(final JSONObject object, final String name) {
        try {
            return object.getJSONObject(name).getJSONArray(JsonResponseKey.URLS).getString(0);
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
            return StringUtils.EMPTY;
        }
    }

    private List<String> getDeepMultipleUrlsField(final JSONObject object, final String name) {
        List<String> result = null;
        try {
            JSONObject json = object.optJSONObject(name);
            if (json != null) {
                result = JsonHelper.toStringList(json.getJSONArray(JsonResponseKey.URLS));
            }
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return result;
    }
}
