package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ResponseDataHolder {

    private int responseCode;
    @Nullable
    private byte[] responseData;
    @Nullable
    private Map<String, List<String>> responseHeaders;
    @NonNull
    private final ResponseValidityChecker responseValidityChecker;

    public ResponseDataHolder(@NonNull ResponseValidityChecker responseValidityChecker) {
        this.responseValidityChecker = responseValidityChecker;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Nullable
    public byte[] getResponseData() {
        return responseData;
    }

    public void setResponseData(@Nullable byte[] responseData) {
        this.responseData = responseData;
    }

    @Nullable
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(@Nullable Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public boolean isValidResponse() {
        return responseValidityChecker.isResponseValid(responseCode);
    }
}
