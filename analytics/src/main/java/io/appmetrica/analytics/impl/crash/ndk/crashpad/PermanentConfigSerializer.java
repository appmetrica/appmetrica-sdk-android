package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import org.json.JSONObject;

class PermanentConfigSerializer {

    private static final String TAG = "[PermanentConfigSerializer]";

    public static final String ARGUMENT_CLIENT_DESCRIPTION = "arg_cd";

    public static final String ARGUMENT_API_KEY = "arg_ak";
    public static final String ARGUMENT_PACKAGE_NAME = "arg_pn";
    public static final String ARGUMENT_PID = "arg_pd";
    public static final String ARGUMENT_PSID = "arg_ps";
    public static final String ARGUMENT_REPORTER_TYPE = "arg_rt";

    private static String[] CONFIG_KEYS = new String[] {
            ARGUMENT_API_KEY,
            ARGUMENT_PACKAGE_NAME,
            ARGUMENT_PID,
            ARGUMENT_PSID,
            ARGUMENT_REPORTER_TYPE
    };

    @Nullable
    public String serialize(@NonNull String apiKey, @NonNull ProcessConfiguration processConfiguration) {
        try {
            JSONObject object = new JSONObject().put(ARGUMENT_CLIENT_DESCRIPTION, new JSONObject()
                    .put(ARGUMENT_API_KEY, apiKey)
                    .put(ARGUMENT_PACKAGE_NAME, processConfiguration.getPackageName())
                    .put(ARGUMENT_PID, processConfiguration.getProcessID())
                    .put(ARGUMENT_PSID, processConfiguration.getProcessSessionID())
                    .put(ARGUMENT_REPORTER_TYPE, CounterConfigurationReporterType.MAIN.getStringValue())
            );
            return Base64.encodeToString(object.toString().getBytes(), Base64.DEFAULT);
        } catch (Throwable ignored) {}
        return null;
    }

    @Nullable
    ClientDescription deserialize(@NonNull String data) {
        try {
            JSONObject crashData = new JSONObject(new String(Base64.decode(data, Base64.DEFAULT)));
            return deserializeClientDescription(crashData.getJSONObject(ARGUMENT_CLIENT_DESCRIPTION));
        } catch (Exception e) { // for any typing problems
            YLogger.error(TAG, e, "not able deserialize config");
            return null;
        }
    }

    @Nullable
    private ClientDescription deserializeClientDescription(@NonNull JSONObject description) {
        try {
            for (String key: CONFIG_KEYS) {
                if (!description.has(key)) {
                    YLogger.debug(TAG, "can't find key %s for config", key);
                    return null;
                }
            }
            int pid = description.getInt(ARGUMENT_PID);
            if (pid == 0) {
                pid = Integer.parseInt(description.getString(ARGUMENT_PID));
            }
            return new ClientDescription(
                    description.getString(ARGUMENT_API_KEY),
                    description.getString(ARGUMENT_PACKAGE_NAME),
                    pid,
                    description.getString(ARGUMENT_PSID),
                    CounterConfigurationReporterType.fromStringValue(description.getString(ARGUMENT_REPORTER_TYPE))
            );
        } catch (Exception e) { // for any typing problems
            YLogger.error(TAG, e, "not able deserialize config");
            return null;
        }
    }
}
