package io.appmetrica.analytics.network.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.network.impl.utils.Utils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {

    private final boolean completed;
    private final int code;
    @NonNull
    private final byte[] responseData;
    @NonNull
    private final byte[] errorData;
    @NonNull
    private final Map<String, List<String>> headers;
    @Nullable
    private final Throwable exception;

    public Response(@Nullable Throwable exception) {
        this(false, 0, new byte[0], new byte[0], new HashMap<String, List<String>>(), exception);
    }

    public Response(boolean completed,
                    int code,
                    @NonNull byte[] responseData,
                    @NonNull byte[] errorData,
                    @Nullable Map<String, List<String>> headers,
                    @Nullable Throwable exception) {
        this.completed = completed;
        this.code = code;
        this.responseData = responseData;
        this.errorData = errorData;
        this.headers = headers == null ? Collections.<String, List<String>>emptyMap() :
                Utils.unmodifiableMapCopy(headers);
        this.exception = exception;
    }

    public int getCode() {
        return code;
    }

    @NonNull
    public byte[] getResponseData() {
        return responseData;
    }

    @NonNull
    public byte[] getErrorData() {
        return errorData;
    }

    @NonNull
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Nullable
    public Throwable getException() {
        return exception;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return "Response{" +
                "completed=" + completed +
                ", code=" + code +
                ", responseDataLength=" + responseData.length +
                ", errorDataLength=" + errorData.length +
                ", headers=" + headers +
                ", exception=" + exception +
                '}';
    }
}
