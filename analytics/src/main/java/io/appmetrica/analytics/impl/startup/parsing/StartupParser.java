package io.appmetrica.analytics.impl.startup.parsing;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.db.state.converter.StatSendingConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class StartupParser {

    private static final String TAG = "[StartupParser]";

    private static final String HEADER_SERVER_TIME = "Date";

    private static final String SERVER_TIME_FORMAT = "E, d MMM yyyy HH:mm:ss z";

    @NonNull
    private final HostsParser mHostsParser;
    @NonNull
    private final FeaturesParser mFeaturesParser;
    @NonNull
    private final RetryPolicyConfigParser mRetryPolicyConfigParser;
    @NonNull
    private final PermissionsCollectingConfigParser mPermissionsCollectingConfigParser;
    @NonNull
    private final StatSendingConverter mStatSendingConverter;
    @NonNull
    private final AutoInappCollectingConfigParser autoInappCollectingConfigParser;
    @NonNull
    private final CacheControlParser mCacheControlParser;
    @NonNull
    private final AttributionConfigParser attributionConfigParser;
    @NonNull
    private final JsonResponseProvider jsonResponseProvider;
    @NonNull
    private final StartupUpdateConfigParser startupUpdateConfigParser;
    @NonNull
    private final ModulesRemoteConfigsParser modulesRemoteConfigsParser;
    @NonNull
    private final ExternalAttributionConfigParser externalAttributionConfigParser;

    public StartupParser() {
        this(
            new JsonResponseProvider(),
            new HostsParser(),
            new FeaturesParser(),
            new RetryPolicyConfigParser(),
            new PermissionsCollectingConfigParser(),
            new StatSendingConverter(),
            new CacheControlParser(),
            new AutoInappCollectingConfigParser(),
            new AttributionConfigParser(),
            new StartupUpdateConfigParser(),
            new ModulesRemoteConfigsParser(),
            new ExternalAttributionConfigParser()
        );
    }

    @VisibleForTesting
    public StartupParser(
        @NonNull JsonResponseProvider jsonResponseProvider,
        @NonNull HostsParser hostsParser,
        @NonNull FeaturesParser featuresParser,
        @NonNull RetryPolicyConfigParser retryPolicyConfigParser,
        @NonNull PermissionsCollectingConfigParser permissionsCollectingConfigParser,
        @NonNull StatSendingConverter statSendingConverter,
        @NonNull CacheControlParser cacheControlParser,
        @NonNull AutoInappCollectingConfigParser autoInappCollectingConfigParser,
        @NonNull AttributionConfigParser attributionConfigParser,
        @NonNull StartupUpdateConfigParser startupUpdateConfigParser,
        @NonNull ModulesRemoteConfigsParser modulesRemoteConfigsParser,
        @NonNull ExternalAttributionConfigParser externalAttributionConfigParser
    ) {
        mHostsParser = hostsParser;
        mFeaturesParser = featuresParser;
        mRetryPolicyConfigParser = retryPolicyConfigParser;
        mPermissionsCollectingConfigParser = permissionsCollectingConfigParser;
        mStatSendingConverter = statSendingConverter;
        mCacheControlParser = cacheControlParser;
        this.autoInappCollectingConfigParser = autoInappCollectingConfigParser;
        this.attributionConfigParser = attributionConfigParser;
        this.jsonResponseProvider = jsonResponseProvider;
        this.startupUpdateConfigParser = startupUpdateConfigParser;
        this.modulesRemoteConfigsParser = modulesRemoteConfigsParser;
        this.externalAttributionConfigParser = externalAttributionConfigParser;
    }

    public StartupResult parseStartupResponse(final byte[] rawResponse) {

        StartupResult result = new StartupResult();

        try {
            final JsonHelper.OptJSONObject response = jsonResponseProvider.jsonFromBytes(rawResponse);

            DebugLogger.INSTANCE.info(TAG, "Full response: " + response);
            DebugLogger.INSTANCE.dumpJson(TAG, response);

            parseDeviceId(result, response);
            parseComplexBlocks(result, response);
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(
                TAG,
                exception,
                "Smth was wrong while parsing startup answer.\n%s",
                exception
            );
            result = new StartupResult();
            result.setResult(StartupResult.Result.BAD);
            return result;
        }
        result.setResult(calculateResult(result));
        return result;
    }

    private StartupResult.Result calculateResult(@NonNull StartupResult result) {
        return (TextUtils.isEmpty(result.getDeviceIDHash())) ?
            StartupResult.Result.BAD : StartupResult.Result.OK;
    }

    private void parseComplexBlocks(StartupResult result, JsonHelper.OptJSONObject response) throws JSONException {
        parseQueries(result, response);
        parseClids(result, response);
        parseLocale(result, response);
        parseValidTimeDifference(result, response);
        parseStatSending(result, response);

        mFeaturesParser.parse(result, response);
        mHostsParser.parse(result, response);
        mRetryPolicyConfigParser.parse(result, response);
        mPermissionsCollectingConfigParser.parseIfEnabled(result, response);
        autoInappCollectingConfigParser.parse(result, response);
        result.setCacheControl(mCacheControlParser.parseFromJson(response));
        attributionConfigParser.parse(result, response);
        startupUpdateConfigParser.parse(result, response);
        modulesRemoteConfigsParser.parse(result, response);
        externalAttributionConfigParser.parse(result, response);
    }

    private void parseDeviceId(@NonNull StartupResult result,
                               @NonNull JsonHelper.OptJSONObject response) {
        String deviceId = "";
        String deviceIdHash = "";
        JSONObject deviceIdJson = response.optJSONObject(JsonResponseKey.DEVICE_ID);
        if (deviceIdJson != null) {
            deviceIdHash = deviceIdJson.optString(JsonResponseKey.HASH);
            deviceId = deviceIdJson.optString(JsonResponseKey.VALUE);
        }
        result.setDeviceId(deviceId);
        result.setDeviceIDHash(deviceIdHash);
    }

    private void parseLocale(@NonNull StartupResult result, @NonNull JsonHelper.OptJSONObject response) {
        JSONObject locale = response.optJSONObject(JsonResponseKey.LOCALE);
        String countryInit = "";
        if (locale != null) {
            DebugLogger.INSTANCE.info(TAG, "locale %s", locale.toString());
            JSONObject country = locale.optJSONObject(JsonResponseKey.COUNTRY);
            if (country != null) {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "reliable %b",
                    country.optBoolean(JsonResponseKey.RELIABLE, false)
                );
                if (country.optBoolean(JsonResponseKey.RELIABLE, false)) {
                    countryInit = country.optString(JsonResponseKey.VALUE, "");
                }
            }
        }
        result.setCountryInit(countryInit);
    }

    private void parseQueries(@NonNull StartupResult result, @NonNull JsonHelper.OptJSONObject response) {
        JSONObject queries = response.optJSONObject(JsonResponseKey.QUERIES);
        if (queries != null) {
            DebugLogger.INSTANCE.info(TAG, "queries %s", queries);
            JSONObject list = queries.optJSONObject(JsonResponseKey.LIST);
            if (list != null) {
                JSONObject certificate = list.optJSONObject(JsonResponseKey.CERTIFICATE);
                if (certificate != null) {
                    result.setCertificateUrl(certificate.optString(JsonResponseKey.URL, null));
                }
            }
        }
    }

    private void parseStatSending(@NonNull StartupResult result, @NonNull JsonHelper.OptJSONObject response) {
        StartupStateProtobuf.StartupState.StatSending statSending = new StartupStateProtobuf.StartupState.StatSending();
        JSONObject statSendingJson = response.optJSONObject(JsonResponseKey.STAT_SENDING);
        if (statSendingJson != null) {
            statSending.disabledReportingInterval = RemoteConfigJsonUtils.extractMillisFromSecondsOrDefault(
                statSendingJson,
                JsonResponseKey.DISABLED_REPORTING_INTERVAL_SECONDS,
                statSending.disabledReportingInterval
            );
        }
        result.setStatSending(mStatSendingConverter.toModel(statSending));
    }

    private void parseClids(final StartupResult startupResult,
                            final JsonHelper.OptJSONObject response) throws JSONException {
        final JSONObject queryDistribution =
            (JSONObject) response.get(JsonResponseKey.QUERY_DISTRIBUTION_CUSTOMIZATION, new JSONObject());

        final JSONObject clidsJson = queryDistribution.optJSONObject(JsonResponseKey.QUERY_CLIDS);
        if (clidsJson != null) {
            parseClids(startupResult, clidsJson);
        }
    }

    private void parseClids(final StartupResult startupResult, final JSONObject clidsJson) throws JSONException {
        Map<String, String> clidsMap = new HashMap<String, String>();

        Iterator<String> keys = clidsJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject clid = clidsJson.optJSONObject(key);

            if (clid != null && clid.has(JsonResponseKey.VALUE)) {
                clidsMap.put(key, clid.getString(JsonResponseKey.VALUE));
            }
        }
        startupResult.setEncodedClids(StartupUtils.encodeClids(clidsMap));
    }

    private void parseValidTimeDifference(final StartupResult startupResult, final JSONObject jsonObject) {
        JSONObject timeJson = jsonObject.optJSONObject(JsonResponseKey.TIME);
        if (timeJson != null) {
            try {
                long validTimeDifference = timeJson.getLong(JsonResponseKey.MAX_VALID_DIFFERENCE_SECONDS);
                startupResult.setValidTimeDifference(validTimeDifference);
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(
                    TAG,
                    e,
                    "Couldn't parse %s from startup response",
                    JsonResponseKey.MAX_VALID_DIFFERENCE_SECONDS
                );
            }
        }
    }

    public static Long parseServerTime(@Nullable Map<String, List<String>> responseHeaders) {
        Long serverTime = null;

        if (!Utils.isNullOrEmpty(responseHeaders)) {
            List<String> headerValues = CollectionUtils.getFromMapIgnoreCase(responseHeaders, HEADER_SERVER_TIME);

            if (!Utils.isNullOrEmpty(headerValues)) {
                try {
                    String headerValue = headerValues.get(0);
                    DateFormat dt = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.US);

                    serverTime = dt.parse(headerValue).getTime();
                    DebugLogger.INSTANCE.info(TAG, "ParserServerTime = %s", String.valueOf(serverTime));
                } catch (Throwable e) {
                    DebugLogger.INSTANCE.error(
                        TAG,
                        "Smth was wrong while parsing startup response header.\n%s",
                        e
                    );
                }
            }
        }
        return serverTime;
    }
}
