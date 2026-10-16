package io.appmetrica.analytics.networktasks.internal;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.network.internal.NetworkClientBuilder;
import io.appmetrica.analytics.networkapi.NetworkClient;
import io.appmetrica.analytics.networkapi.NetworkClientSettings;
import io.appmetrica.analytics.networkapi.Request;
import io.appmetrica.analytics.networkapi.Response;
import io.appmetrica.analytics.networktasks.impl.Constants;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class CacheControlHttpsConnectionPerformer {

    private static final String TAG = "[CacheControlHttpsConnectionPerformer]";

    public interface Client {

        @Nullable
        String getOldETag();

        void onResponse(@NonNull String newETag, @NonNull byte[] response);

        void onNotModified();

        void onError();
    }

    static class RequestExecutor {

        @NonNull
        public Response execute(@Nullable String prevEtag,
                                @NonNull String url,
                                @Nullable SSLSocketFactory sslSocketFactory,
                                @Nullable X509TrustManager trustManager) {
            Request.Builder requestBuilder = new Request.Builder(url)
                .withMethod(Request.Method.GET);
            if (TextUtils.isEmpty(prevEtag) == false) {
                requestBuilder.addHeader("If-None-Match", prevEtag);
            }
            NetworkClient client = new NetworkClientBuilder()
                .withSettings(
                    new NetworkClientSettings.Builder()
                        .withInstanceFollowRedirects(true)
                        .withSslSocketFactory(sslSocketFactory)
                        .withTrustManager(trustManager)
                        .withConnectTimeout(Constants.Config.REQUEST_TIMEOUT)
                        .withReadTimeout(Constants.Config.REQUEST_TIMEOUT)
                        .build()
                )
                .build();
            DebugLogger.INSTANCE.info(TAG, "Connecting to %s", url);
            return client.newCall(requestBuilder.build()).execute();
        }
    }

    private final RequestExecutor requestExecutor;
    @Nullable
    private final SSLSocketFactory sslSocketFactory;
    @Nullable
    private final X509TrustManager trustManager;

    public CacheControlHttpsConnectionPerformer(
        @Nullable SSLSocketFactory sslSocketFactory,
        @Nullable X509TrustManager trustManager
    ) {
        this(new RequestExecutor(), sslSocketFactory, trustManager);
    }

    @VisibleForTesting
    CacheControlHttpsConnectionPerformer(@NonNull RequestExecutor requestExecutor,
                                         @Nullable SSLSocketFactory sslSocketFactory,
                                         @Nullable X509TrustManager trustManager) {
        this.requestExecutor = requestExecutor;
        this.sslSocketFactory = sslSocketFactory;
        this.trustManager = trustManager;
    }

    public void performConnection(@NonNull String url, @NonNull Client client) {
        boolean success = false;
        try {
            Response response = requestExecutor.execute(
                client.getOldETag(),
                url,
                sslSocketFactory,
                trustManager
            );
            success = handleResponse(response, client);
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e, "Failed request with url = %s with reason = %s",
                    url, e.getMessage());
        }
        if (!success) {
            client.onError();
        }
    }

    private boolean handleResponse(@NonNull Response response, @NonNull Client client) {
        int responseCode = response.getCode();
        DebugLogger.INSTANCE.info(TAG, "handleResponse with code %d", responseCode);
        switch (responseCode) {
            case HttpsURLConnection.HTTP_OK:
                List<String> etagValues = CollectionUtils.getFromMapIgnoreCase(response.getHeaders(), "ETag");
                final String newETag;
                if (etagValues != null && etagValues.size() > 0) {
                    String eTagCandidate = etagValues.get(0);
                    newETag = eTagCandidate == null ? "" : eTagCandidate;
                } else {
                    newETag = "";
                }
                client.onResponse(newETag, response.getResponseData());
                return true;
            case HttpsURLConnection.HTTP_NOT_MODIFIED:
                client.onNotModified();
                return true;
            default:
                DebugLogger.INSTANCE.warning(TAG, "%s Http request finished with code %d", responseCode);
                return false;
        }
    }

}
