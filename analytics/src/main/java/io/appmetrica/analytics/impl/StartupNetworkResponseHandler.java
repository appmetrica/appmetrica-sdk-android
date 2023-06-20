package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.db.state.converter.BodyDecoder;
import io.appmetrica.analytics.impl.network.Constants;
import io.appmetrica.analytics.impl.startup.parsing.StartupParser;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import io.appmetrica.analytics.networktasks.internal.NetworkResponseHandler;
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

public class StartupNetworkResponseHandler implements NetworkResponseHandler<StartupResult> {

    private static final String TAG = "[StartupNetworkResponseHandler]";

    @NonNull
    private final StartupParser mStartupParser;
    @NonNull
    private final BodyDecoder mBodyDecoder;

    public StartupNetworkResponseHandler() {
        this(new StartupParser(), new BodyDecoder());
    }

    @Override
    @Nullable
    public StartupResult handle(@NonNull ResponseDataHolder responseDataHolder) {
        YLogger.info(
            TAG, "handle response with code: %d; responseHeaders: %s",
            responseDataHolder.getResponseCode(),
            responseDataHolder.getResponseHeaders()
        );
        if (HttpsURLConnection.HTTP_OK == responseDataHolder.getResponseCode()) {
            byte[] bodyToParse = responseDataHolder.getResponseData();

            List<String> contentEncodingHeaderValues = null;
            Map<String, List<String>> headers = responseDataHolder.getResponseHeaders();
            if (headers != null) {
                contentEncodingHeaderValues =
                    CollectionUtils.getFromMapIgnoreCase(headers, Constants.Headers.CONTENT_ENCODING);
            }
            if (Utils.isNullOrEmpty(contentEncodingHeaderValues) == false) {
                if (Constants.Config.ENCODING_ENCRYPTED.equals(contentEncodingHeaderValues.get(0))) {
                    YLogger.info(TAG, "Detect `Content-Encoding=encrypted` so try to decrypt body");
                    bodyToParse = mBodyDecoder.decode(responseDataHolder.getResponseData(), "hBnBQbZrmjPXEWVJ");
                }
            } else {
                YLogger.info(
                    TAG, "Can't detect `Content-Encoding=encrypted`. Actual value: %s. Ignore decrypting",
                    contentEncodingHeaderValues
                );
            }

            if (bodyToParse != null) {
                YLogger.d("%sParse startup response", TAG);
                final StartupResult parseResult = mStartupParser.parseStartupResponse(bodyToParse);

                if (StartupResult.Result.OK == parseResult.getResult()) {
                    return parseResult;
                }
            }
        } else {
            YLogger.warning(TAG, "Response code != 200: %s. Ignore parsing.", responseDataHolder.getResponseCode());
        }

        return null;
    }

    @VisibleForTesting
    StartupNetworkResponseHandler(@NonNull final StartupParser startupParser,
                                  @NonNull final BodyDecoder bodyDecoder) {
        mStartupParser = startupParser;
        mBodyDecoder = bodyDecoder;
    }
}
