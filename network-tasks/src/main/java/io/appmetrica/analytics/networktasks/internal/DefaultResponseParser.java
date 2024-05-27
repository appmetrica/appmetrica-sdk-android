package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import org.json.JSONObject;

public class DefaultResponseParser {

    public static class Response {
        @NonNull
        public final String mStatus;

        public Response(@NonNull final String status) {
            mStatus = status;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "mStatus='" + mStatus + '\'' +
                    '}';
        }
    }

    private static final String TAG = "[DefaultResponseParser]";

    private static class JsonKeys {
        static final String STATUS = "status";
    }

    @Nullable
    public Response parse(@Nullable byte[] responseBody) {
        Response response = null;

        try {
            if (responseBody != null && responseBody.length > 0) {
                String responseString = new String(responseBody, "UTF-8");
                JSONObject responseJson = new JSONObject(responseString);
                response = new Response(responseJson.optString(JsonKeys.STATUS));
            }
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e);
        }

        DebugLogger.INSTANCE.info(TAG, "Parsed result is %s", response);
        return response;
    }
}
