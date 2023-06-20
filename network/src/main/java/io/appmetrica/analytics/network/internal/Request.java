package io.appmetrica.analytics.network.internal;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.network.impl.utils.Utils;
import java.util.HashMap;
import java.util.Map;

public class Request {

    @NonNull
    private final String url;
    @NonNull
    private final String method;
    @NonNull
    private final byte[] body;
    @NonNull
    private final Map<String, String> headers;

    private Request(@NonNull String url,
                    @Nullable String method,
                    @NonNull byte[] body,
                    @NonNull Map<String, String> headers) {
        this.url = url;
        this.method = TextUtils.isEmpty(method) ? "GET" : method;
        this.body = body;
        this.headers = Utils.unmodifiableMapCopy(headers);
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public Map<String, String> getHeaders() {
        return headers;
    }

    @NonNull
    public String getMethod() {
        return method;
    }

    @NonNull
    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Request{" +
                "url=" + url +
                ", method='" + method + '\'' +
                ", bodyLength=" + body.length +
                ", headers=" + headers +
                '}';
    }

    public static class Builder {

        @NonNull
        private final String url;
        @Nullable
        private String method;
        @NonNull
        private byte[] body = new byte[0];
        @NonNull
        private final Map<String, String> headers = new HashMap<>();

        public Builder(@NonNull String url) {
            this.url = url;
        }

        @NonNull
        public Builder addHeader(@NonNull String key, @Nullable String value) {
            headers.put(key, value);
            return this;
        }

        @NonNull
        public Builder post(@NonNull byte[] body) {
            this.body = body;
            return withMethod("POST");
        }

        @NonNull
        public Builder withMethod(@NonNull String method) {
            this.method = method;
            return this;
        }

        public Request build() {
            return new Request(url, method, body, headers);
        }
    }
}
