package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.impl.utils.JsonHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StartupJsonMock extends JsonHelper.OptJSONObject {

    final String testDeviceId = generateId();
    final String testDeviceHash = generateId();
    final String testGetAdHost = generateHost();
    final List<String> testReportHosts = Arrays.asList(generateHost(), generateHost(), generateHost());
    final String testReportAdHost = generateHost();
    final List<String> testStartupHosts = Arrays.asList(generateHost(), generateHost(), generateHost());

    final Map<String, String> testClids = new HashMap<String, String>() {
        {
            put("clid1", "value1");
            put("clid5", "value22");
            put("clid9", "value333");
        }
    };

    public StartupJsonMock() throws JSONException {
        super("{}");
        addTestData();
    }

    private void addTestData() throws JSONException {
        setDeviceId(testDeviceId, testDeviceHash);

        addHosts();
        addDistributionCustomization();
    }

    public void addEmptyFeaturesList() throws JSONException {
        JSONObject features = optJSONObject(JsonResponseKey.FEATURES);
        JSONObject list;
        if (features == null) {
            features = new JSONObject();
            list = new JSONObject();
            features.put(JsonResponseKey.LIST, list);
            put(JsonResponseKey.FEATURES, features);
        }
    }

    public void setDeviceId(String deviceId, String deviceIdHash) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt(JsonResponseKey.VALUE, deviceId);
        jsonObject.putOpt(JsonResponseKey.HASH, deviceIdHash);
        put(JsonResponseKey.DEVICE_ID, jsonObject);
    }

    public void removeDeviceIdBlock() {
        remove(JsonResponseKey.DEVICE_ID);
    }

    public void addGoogleAid(boolean enabled) throws JSONException {
        addBooleanFeature(JsonResponseKey.FEATURE_GOOGLE_AID, enabled);
    }

    public void addSimInfo(boolean enabled) throws JSONException {
        addBooleanFeature(JsonResponseKey.FEATURE_SIM_INFO, enabled);
    }

    public void addPermissionsCollectingEnabled(boolean enabled) throws JSONException {
        addBooleanFeature(JsonResponseKey.FEATURE_PERMISSION_COLLECTING, enabled);
    }

    public void addFeaturesCollecting(boolean enabled) throws JSONException {
        addBooleanFeature(JsonResponseKey.FEATURE_FEATURES_COLLECTING, enabled);
    }

    public void addHuaweiOaid(boolean enabled) throws JSONException {
        addBooleanFeature(JsonResponseKey.FEATURE_HUAWEI_OAID, enabled);
    }

    public void addSslPinning(boolean enabled) throws JSONException {
        addBooleanFeature(JsonResponseKey.FEATURE_SSL_PINNING, enabled);
    }

    public void addBooleanFeature(String name, boolean enabled) throws JSONException {
        JSONObject featureInfo = new JSONObject();
        featureInfo.put(JsonResponseKey.FEATURE_ENABLED, enabled);
        addFeature(name, featureInfo);
    }

    public void addFeature(String name, JSONObject value) throws JSONException {
        addEmptyFeaturesList();
        JSONObject list = getJSONObject(JsonResponseKey.FEATURES).getJSONObject(JsonResponseKey.LIST);
        list.put(name, value);
    }

    public void setCertificateUrl(String url) throws JSONException {
        putQuery(JsonResponseKey.CERTIFICATE, url);
    }

    private void putQuery(String key, String url) throws JSONException {
        getOrCreateQueriesListJson().put(key, new JSONObject().put(JsonResponseKey.URL, url));
    }

    private void addHosts() throws JSONException {
        final JSONObject list = new JSONObject();

        list.put(JsonResponseKey.GET_AD_HOST, buildOneFieldArrayJson(JsonResponseKey.URLS, testGetAdHost));
        list.put(JsonResponseKey.SEND_REPORTS_HOSTS, buildArrayFieldJson(JsonResponseKey.URLS, testReportHosts));
        list.put(JsonResponseKey.REPORT_AD_HOST, buildOneFieldArrayJson(JsonResponseKey.URLS, testReportAdHost));
        list.put(JsonResponseKey.STARTUP_HOSTS, buildArrayFieldJson(JsonResponseKey.URLS, testStartupHosts));

        final JSONObject queryHosts = new JSONObject().put(JsonResponseKey.LIST, list);
        put(JsonResponseKey.QUERY_HOSTS, queryHosts);
    }

    public void removeHostsJson() throws JSONException {
        remove(JsonResponseKey.QUERY_HOSTS);
    }

    public void removeHostsListJson() throws JSONException {
        getJSONObject(JsonResponseKey.QUERY_HOSTS).remove(JsonResponseKey.LIST);
    }

    public void setStartupHosts(List<String> hosts) throws JSONException {
        setHosts(JsonResponseKey.STARTUP_HOSTS, hosts);
    }

    public void setReportHosts(List<String> hosts) throws JSONException {
        setHosts(JsonResponseKey.SEND_REPORTS_HOSTS, hosts);
    }

    public void setDiagnosticHosts(List<String> hosts) throws JSONException {
        setHosts(JsonResponseKey.DIAGNOSTIC_HOSTS, hosts);
    }

    public void setReportAdHost(String host) throws JSONException {
        setHosts(JsonResponseKey.REPORT_AD_HOST, host == null ? null : Collections.singletonList(host));
    }

    public void setGetAdHost(String host) throws JSONException {
        setHosts(JsonResponseKey.GET_AD_HOST, host == null ? null : Collections.singletonList(host));
    }

    public void addEmptyRetryPolicyConfig() throws JSONException {
        put(JsonResponseKey.RETRY_POLICY_CONFIG, new JSONObject());
    }

    public void addRetryPolicyConfig(int maxInterval, int exponentialMultiplier) throws JSONException {
        put(
            JsonResponseKey.RETRY_POLICY_CONFIG,
            new JSONObject()
                .put(JsonResponseKey.RETRY_POLICY_MAX_INTERVAL, maxInterval)
                .put(JsonResponseKey.RETRY_POLICY_EXPONENTIAL_MULTIPLIER, exponentialMultiplier)
        );
    }

    public void addEmptyAutoInappCollectingConfig() throws JSONException {
        put(JsonResponseKey.AUTO_INAPP_COLLECTING, new JSONObject());
    }

    public void addAutoInappCollectingConfig(int sendFrequencySeconds, int firstCollectingInappMaxAgeSeconds) throws JSONException {
        put(
            JsonResponseKey.AUTO_INAPP_COLLECTING,
            new JSONObject()
                .put(JsonResponseKey.SEND_FREQUENCY_SECONDS, sendFrequencySeconds)
                .put(JsonResponseKey.FIRST_COLLECTING_INAPP_MAX_AGE_SECONDS, firstCollectingInappMaxAgeSeconds)
        );
    }

    public void addEmptyExternalAttributionConfig() throws JSONException {
        put(JsonResponseKey.EXTERNAL_ATTRIBUTION, new JSONObject());
    }

    public void addExternalAttributionConfig(long collectingIntervalSeconds) throws JSONException {
        put(
            JsonResponseKey.EXTERNAL_ATTRIBUTION,
            new JSONObject()
                .put(JsonResponseKey.COLLECTING_INTERVAL_SECONDS, collectingIntervalSeconds)
        );
    }

    public void setHosts(String type, List<String> hosts) throws JSONException {
        getJSONObject(JsonResponseKey.QUERY_HOSTS).getJSONObject(JsonResponseKey.LIST).put(type, buildArrayFieldJson(JsonResponseKey.URLS, hosts));
    }

    private void addDistributionCustomization() throws JSONException {
        final JSONObject distribution = new JSONObject();

        final JSONObject clidsJson = new JSONObject();
        for (Map.Entry<String, String> pair : testClids.entrySet()) {
            clidsJson.put(pair.getKey(), buildOneFieldJson(JsonResponseKey.VALUE, pair.getValue()));
        }
        distribution.put(JsonResponseKey.QUERY_CLIDS, clidsJson);
        put(JsonResponseKey.QUERY_DISTRIBUTION_CUSTOMIZATION, distribution);
    }

    private JSONObject buildOneFieldJson(final String key, final Object value) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(key, value);
        return json;
    }

    private JSONObject buildOneFieldArrayJson(final String key, final Object value) throws JSONException {
        final JSONArray jsonArray = new JSONArray();
        jsonArray.put(value);
        final JSONObject json = new JSONObject();
        json.put(key, jsonArray);
        return json;
    }

    private JSONObject buildArrayFieldJson(final String key, final List<String> list) throws JSONException {
        if (list == null) {
            return null;
        }
        final JSONArray jsonArray = new JSONArray(list);
        final JSONObject json = new JSONObject();
        json.put(key, jsonArray);
        return json;
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateHost() {
        return "http://" + generateId() + ".yandex.ru";
    }

    public void addMaxValidDifferenceSeconds(Object value) throws JSONException {
        JSONObject timeJson = new JSONObject();
        timeJson.put("max_valid_difference_seconds", value);
        addTimeBlock(timeJson);
    }

    public void addTimeBlock(JSONObject timeJson) throws JSONException {
        put("time", timeJson);
    }

    public void addStatSendingDisabledReportingInterval(Long intervalSeconds) throws JSONException {
        JSONObject jsonObject = new JSONObject()
            .put(JsonResponseKey.DISABLED_REPORTING_INTERVAL_SECONDS, intervalSeconds);
        put(JsonResponseKey.STAT_SENDING, jsonObject);
    }

    private JSONObject getOrCreateQueriesListJson() throws JSONException {
        JSONObject queriesList = null;
        JSONObject queries = null;
        if (has(JsonResponseKey.QUERIES)) {
            queries = getJSONObject(JsonResponseKey.QUERIES);
            if (queries.has(JsonResponseKey.LIST)) {
                queriesList = queries.getJSONObject(JsonResponseKey.LIST);
            } else {
                queriesList = new JSONObject();
                queries.put(JsonResponseKey.LIST, queries);
            }
        } else {
            queries = new JSONObject();
            queriesList = new JSONObject();
            queries.put(JsonResponseKey.LIST, queriesList);
            put(JsonResponseKey.QUERIES, queries);
        }
        return queriesList;
    }
}
