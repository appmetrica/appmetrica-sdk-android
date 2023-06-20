package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import javax.net.ssl.HttpsURLConnection;

public class DefaultNetworkResponseHandler implements NetworkResponseHandler<DefaultResponseParser.Response> {

    private static final String TAG = "[DefaultNetworkResponseHandler]";

    @NonNull
    private final DefaultResponseParser mDefaultResponseParser;

    public DefaultNetworkResponseHandler() {
        this(new DefaultResponseParser());
    }

    @VisibleForTesting
    DefaultNetworkResponseHandler(@NonNull final DefaultResponseParser defaultResponseParser) {
        mDefaultResponseParser = defaultResponseParser;
    }

    @Override
    @Nullable
    public DefaultResponseParser.Response handle(@NonNull ResponseDataHolder responseDataHolder) {
        DefaultResponseParser.Response defaultResponse = null;
        if (HttpsURLConnection.HTTP_OK == responseDataHolder.getResponseCode()) {
            defaultResponse = mDefaultResponseParser.parse(responseDataHolder.getResponseData());
        }
        YLogger.info(TAG, "Response result for code %d is %s", responseDataHolder.getResponseCode(), defaultResponse);
        return defaultResponse;
    }
}
