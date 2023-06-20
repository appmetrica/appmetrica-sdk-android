package io.appmetrica.analytics.network.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.network.impl.CallImpl;
import javax.net.ssl.SSLSocketFactory;

public class NetworkClient {

    @Nullable
    private final Integer connectTimeout;
    @Nullable
    private final Integer readTimeout;
    @Nullable
    private final SSLSocketFactory sslSocketFactory;
    @Nullable
    private final Boolean useCaches;
    @Nullable
    private final Boolean instanceFollowRedirects;
    private final int maxResponseSize;

    private NetworkClient(@Nullable Integer connectTimeout,
                          @Nullable Integer readTimeout,
                          @Nullable SSLSocketFactory sslSocketFactory,
                          @Nullable Boolean useCaches,
                          @Nullable Boolean instanceFollowRedirects,
                          @Nullable Integer maxResponseSize) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.sslSocketFactory = sslSocketFactory;
        this.useCaches = useCaches;
        this.instanceFollowRedirects = instanceFollowRedirects;
        this.maxResponseSize = maxResponseSize == null ? Integer.MAX_VALUE : maxResponseSize;
    }

    @NonNull
    public Call newCall(@NonNull Request request) {
        return new CallImpl(this, request);
    }

    @Nullable
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Nullable
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Nullable
    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    @Nullable
    public Boolean getUseCaches() {
        return useCaches;
    }

    @Nullable
    public Boolean getInstanceFollowRedirects() {
        return instanceFollowRedirects;
    }

    public int getMaxResponseSize() {
        return maxResponseSize;
    }

    @Override
    public String toString() {
        return "NetworkClient{" +
                "connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", sslSocketFactory=" + sslSocketFactory +
                ", useCaches=" + useCaches +
                ", instanceFollowRedirects=" + instanceFollowRedirects +
                ", maxResponseSize=" + maxResponseSize +
                '}';
    }

    public static class Builder {

        @Nullable
        private Integer connectTimeout;
        @Nullable
        private Integer readTimeout;
        @Nullable
        private SSLSocketFactory sslSocketFactory;
        @Nullable
        private Boolean useCaches;
        @Nullable
        private Boolean instanceFollowRedirects;
        @Nullable
        private Integer maxResponseSize;

        @NonNull
        public Builder withConnectTimeout(final int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        @NonNull
        public Builder withReadTimeout(final int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        @NonNull
        public Builder withSslSocketFactory(@Nullable final SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        @NonNull
        public Builder withUseCaches(final boolean useCaches) {
            this.useCaches = useCaches;
            return this;
        }

        @NonNull
        public Builder withInstanceFollowRedirects(final boolean instanceFollowRedirects) {
            this.instanceFollowRedirects = instanceFollowRedirects;
            return this;
        }

        @NonNull
        public Builder withMaxResponseSize(final int maxResponseSize) {
            this.maxResponseSize = maxResponseSize;
            return this;
        }

        @NonNull
        public NetworkClient build() {
            return new NetworkClient(
                    connectTimeout,
                    readTimeout,
                    sslSocketFactory,
                    useCaches,
                    instanceFollowRedirects,
                    maxResponseSize
            );
        }
    }
}
