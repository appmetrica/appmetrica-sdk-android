package io.appmetrica.analytics.impl.crash.jvm;

import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils;
import io.appmetrica.analytics.impl.ClientCounterReport;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.utils.JSONable;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class JvmCrash implements JSONable {

    private static final String TAG = "[JvmCrash]";

    private static final String EVENT = "event";
    private static final String BYTES_TRUNCATED = "bytes_truncated";
    private static final String TRIMMED_FIELDS = "trimmed_fields";
    private static final String JVM_CRASH = "jvm_crash";
    private static final String EVENT_NAME = "name";
    private static final String PROCESS_CONFIGURATION = "process_configuration";
    private static final String PACKAGE_NAME = "package_name";
    private static final String PID = "pid";
    private static final String PSID = "psid";
    private static final String REPORTER_CONFIGURATION = "reporter_configuration";
    private static final String API_KEY = "api_key";
    private static final String REPORTER_TYPE = "reporter_type";
    private static final String ENVIRONMENT = "environment";

    private final byte[] crash;
    private final String name;
    private final int bytesTruncated;
    @NonNull
    private final HashMap<ClientCounterReport.TrimmedField, Integer> trimmedFields;

    private final String packageName;
    private final Integer pid;
    private final String psid;
    private final String apiKey;
    @NonNull
    private final CounterConfigurationReporterType reporterType;
    @Nullable
    private final String errorEnvironment;

    public JvmCrash(@NonNull String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject event = jsonObject.getJSONObject(EVENT);
        crash = Base64.decode(event.getString(JVM_CRASH), 0);
        name = event.getString(EVENT_NAME);
        bytesTruncated = event.getInt(BYTES_TRUNCATED);
        errorEnvironment = JsonUtils.optStringOrNull(event, ENVIRONMENT);
        final String trimmedJsonString = event.optString(TRIMMED_FIELDS);
        trimmedFields = new HashMap<ClientCounterReport.TrimmedField, Integer>();
        if (trimmedJsonString != null) {
            try {
                HashMap<String, String> trimmedFieldsJson = JsonHelper.jsonToMap(trimmedJsonString);
                if (trimmedFieldsJson != null) {
                    for (HashMap.Entry<String, String> entry : trimmedFieldsJson.entrySet()) {
                        trimmedFields.put(
                                ClientCounterReport.TrimmedField.valueOf(entry.getKey()),
                                Integer.parseInt(entry.getValue())
                        );

                    }
                }
            } catch (Throwable th) {
                YLogger.e(th, TAG);
            }
        }
        JSONObject processConfiguration = jsonObject.getJSONObject(PROCESS_CONFIGURATION);
        packageName = processConfiguration.getString(PACKAGE_NAME);
        pid = processConfiguration.getInt(PID);
        psid = processConfiguration.getString(PSID);
        JSONObject reporterConfiguration = jsonObject.getJSONObject(REPORTER_CONFIGURATION);
        apiKey = reporterConfiguration.getString(API_KEY);
        reporterType = readReporterType(reporterConfiguration);
    }

    public JvmCrash(@NonNull CounterReport report,
                    @NonNull ClientConfiguration clientConfiguration,
                    @Nullable HashMap<ClientCounterReport.TrimmedField, Integer> trimmedFields) {
        crash = report.getValueBytes();
        name = report.getName();
        bytesTruncated = report.getBytesTruncated();
        if (trimmedFields != null) {
            this.trimmedFields = trimmedFields;
        } else {
            this.trimmedFields = new HashMap<ClientCounterReport.TrimmedField, Integer>();
        }
        ProcessConfiguration processConfiguration = clientConfiguration.getProcessConfiguration();
        packageName = processConfiguration.getPackageName();
        pid = processConfiguration.getProcessID();
        psid = processConfiguration.getProcessSessionID();
        CounterConfiguration counterConfiguration = clientConfiguration.getReporterConfiguration();
        apiKey = counterConfiguration.getApiKey();
        reporterType = counterConfiguration.getReporterType();
        errorEnvironment = report.getEventEnvironment();
    }

    public byte[] getCrashValue() {
        return crash;
    }

    public String getName() {
        return name;
    }

    public int getBytesTruncated() {
        return bytesTruncated;
    }

    @NonNull
    public HashMap<ClientCounterReport.TrimmedField, Integer> getTrimmedFields() {
        return trimmedFields;
    }

    public Integer getPid() {
        return pid;
    }

    public String getPsid() {
        return psid;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getApiKey() {
        return apiKey;
    }

    @NonNull
    public CounterConfigurationReporterType getReporterType() {
        return reporterType;
    }

    @Nullable
    public String getEnvironment() {
        return errorEnvironment;
    }

    @Override
    public String toJSONString() throws JSONException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (HashMap.Entry<ClientCounterReport.TrimmedField, Integer> entry : trimmedFields.entrySet()) {
            map.put(entry.getKey().name(), entry.getValue());
        }
        return new JSONObject().put(
                PROCESS_CONFIGURATION,
                new JSONObject().put(PID, pid).put(PSID, psid).put(PACKAGE_NAME, packageName)
        ).put(
                REPORTER_CONFIGURATION,
                new JSONObject().put(API_KEY, apiKey).put(REPORTER_TYPE, reporterType.getStringValue())
        ).put(
                EVENT, new JSONObject()
                        .put(JVM_CRASH, Base64.encodeToString(crash, 0))
                        .put(EVENT_NAME, name)
                        .put(BYTES_TRUNCATED, bytesTruncated)
                        .put(TRIMMED_FIELDS, JsonHelper.mapToJsonString(map))
                        .putOpt(ENVIRONMENT, errorEnvironment)
        ).toString();
    }

    @NonNull
    private CounterConfigurationReporterType readReporterType(@NonNull JSONObject json) throws JSONException {
        if (json.has(REPORTER_TYPE)) {
            return CounterConfigurationReporterType.fromStringValue(json.getString(REPORTER_TYPE));
        }
        return CounterConfigurationReporterType.MAIN;
    }
}
