package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.logger.internal.YLogger;
import org.json.JSONObject;

class JsonObjectLogDumper implements IObjectLogDumper<JSONObject> {

    private static final String TAG = "[JsonObjectLogDumper]";

    static final int JSON_INDENT_SPACES = 2;
    private static final String DUMP_EXCEPTION_MESSAGE = "Exception during dumping JSONObject";

    @Override
    public String dumpObject(@NonNull JSONObject input) {
        try {
            return input.toString(JSON_INDENT_SPACES);
        } catch (Throwable e) {
            YLogger.error(TAG, e, DUMP_EXCEPTION_MESSAGE);
        }
        return DUMP_EXCEPTION_MESSAGE;
    }
}
