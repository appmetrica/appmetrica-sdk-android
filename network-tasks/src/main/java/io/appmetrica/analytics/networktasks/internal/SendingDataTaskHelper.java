package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.io.Compressor;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.networktasks.impl.Constants;
import java.io.IOException;

public class SendingDataTaskHelper {

    private static final String TAG = "[SendingDataTaskHelper]";

    @NonNull
    private final RequestBodyEncrypter requestBodyEncrypter;
    @NonNull
    private final Compressor compressor;
    @NonNull
    private final TimeProvider timeProvider;
    @NonNull
    private final RequestDataHolder requestDataHolder;
    @NonNull
    private final ResponseDataHolder responseDataHolder;
    @NonNull
    private final NetworkResponseHandler<DefaultResponseParser.Response> responseHandler;

    public SendingDataTaskHelper(@NonNull RequestBodyEncrypter requestBodyEncrypter,
                                 @NonNull Compressor compressor,
                                 @NonNull RequestDataHolder requestDataHolder,
                                 @NonNull ResponseDataHolder responseDataHolder,
                                 @NonNull NetworkResponseHandler<DefaultResponseParser.Response> responseHandler) {
        this(
                requestBodyEncrypter,
                compressor,
                new SystemTimeProvider(),
                requestDataHolder,
                responseDataHolder,
                responseHandler
        );
    }

    public SendingDataTaskHelper(@NonNull RequestBodyEncrypter requestBodyEncrypter,
                                 @NonNull Compressor compressor,
                                 @NonNull TimeProvider timeProvider,
                                 @NonNull RequestDataHolder requestDataHolder,
                                 @NonNull ResponseDataHolder responseDataHolder,
                                 @NonNull NetworkResponseHandler<DefaultResponseParser.Response> responseHandler) {
        this.requestBodyEncrypter = requestBodyEncrypter;
        this.compressor = compressor;
        this.timeProvider = timeProvider;
        this.requestDataHolder = requestDataHolder;
        this.responseDataHolder = responseDataHolder;
        this.responseHandler = responseHandler;
    }

    public void onPerformRequest() {
        YLogger.info(TAG, "onPerformRequest");
        requestDataHolder.applySendTime(timeProvider.currentTimeMillis());
    }

    public boolean isResponseValid() {
        DefaultResponseParser.Response response = responseHandler.handle(responseDataHolder);
        boolean result = response != null && Constants.STATUS_ACCEPTED.equals(response.mStatus);
        YLogger.info(TAG, "is response valid? %b. Response: %s", result, response);
        return result;
    }

    public boolean prepareAndSetPostData(@NonNull final byte[] postData) {
        boolean processingPostDataStatus = false;
        try {
            byte[] compressedBytes = compressor.compress(postData);
            if (compressedBytes != null) {

                byte[] encryptedBytes = requestBodyEncrypter.encrypt(compressedBytes);
                YLogger.info(TAG, "Request raw data length: %d; after GZIP: %d; after encoding: %d",
                        postData.length,
                        compressedBytes == null ? null : compressedBytes.length,
                        encryptedBytes == null ? null : encryptedBytes.length);
                if (encryptedBytes != null) {
                    requestDataHolder.setPostData(encryptedBytes);
                    processingPostDataStatus = true;
                }
            }
            return processingPostDataStatus;
        } catch (IOException e) {
            YLogger.error(TAG, e);
            return false;
        }
    }
}
