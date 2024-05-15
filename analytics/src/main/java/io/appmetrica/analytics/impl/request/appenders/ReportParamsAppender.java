package io.appmetrica.analytics.impl.request.appenders;

import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.request.DbNetworkTaskConfig;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.request.UrlParts;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.internal.AdvIdWithLimitedAppender;
import io.appmetrica.analytics.networktasks.internal.CommonUrlParts;
import io.appmetrica.analytics.networktasks.internal.IParamsAppender;
import io.appmetrica.analytics.networktasks.internal.NetworkTaskForSendingDataParamsAppender;
import io.appmetrica.analytics.networktasks.internal.RequestBodyEncrypter;

public class ReportParamsAppender implements IParamsAppender<ReportRequestConfig> {

    private static final String TAG = "[ReportParamsAppender]";

    @NonNull
    private final AdvIdWithLimitedAppender advIdAppender;
    @NonNull
    private final NetworkTaskForSendingDataParamsAppender sendingDataParamsAppender;
    @Nullable
    private DbNetworkTaskConfig mDbReportRequestConfig;
    private long mRequestId;

    public ReportParamsAppender(@NonNull RequestBodyEncrypter requestBodyEncrypter) {
        this(new AdvIdWithLimitedAppender(), new NetworkTaskForSendingDataParamsAppender(requestBodyEncrypter));
    }

    @VisibleForTesting
    ReportParamsAppender(@NonNull AdvIdWithLimitedAppender advIdAppender,
                         @NonNull NetworkTaskForSendingDataParamsAppender sendingDataParamsAppender) {
        this.advIdAppender = advIdAppender;
        this.sendingDataParamsAppender = sendingDataParamsAppender;
    }

    public void setDbReportRequestConfig(@NonNull DbNetworkTaskConfig config) {
        DebugLogger.info(TAG, "set db report request config to %s", config);
        mDbReportRequestConfig = config;
    }

    public void setRequestId(final long requestId) {
        DebugLogger.info(TAG, "set request id to %d", requestId);
        mRequestId = requestId;
    }

    @Override
    public void appendParams(@NonNull Uri.Builder uriBuilder, @NonNull ReportRequestConfig requestConfig) {
        DebugLogger.info(TAG, "append params with requestId: %d, config: %s, db config: %s",
                mRequestId, requestConfig, mDbReportRequestConfig);
        uriBuilder.path(UrlParts.REPORT_PATH);
        sendingDataParamsAppender.appendEncryptedData(uriBuilder);

        appendParamsFromDatabase(uriBuilder, requestConfig);
        appendParamsFromCurrentConfiguration(uriBuilder, requestConfig);
        uriBuilder.appendQueryParameter(CommonUrlParts.REQUEST_ID,
            String.valueOf(mRequestId));
    }

    private void appendParamsFromDatabase(@NonNull Uri.Builder uriBuilder, @NonNull ReportRequestConfig requestConfig) {
        if (mDbReportRequestConfig != null) {
            appendIfEmptyToDef(uriBuilder, CommonUrlParts.DEVICE_ID,
                mDbReportRequestConfig.deviceId, requestConfig.getDeviceId());
            appendIfEmptyToDef(uriBuilder, CommonUrlParts.UUID,
                mDbReportRequestConfig.uuid, requestConfig.getUuid());
            appendIfNotEmpty(
                    uriBuilder,
                    CommonUrlParts.ANALYTICS_SDK_VERSION_NAME,
                    mDbReportRequestConfig.analyticsSdkVersionName
            );
            appendIfEmptyToDef(uriBuilder, CommonUrlParts.APP_VERSION,
                mDbReportRequestConfig.appVersion, requestConfig.getAppVersion());
            appendIfEmptyToDef(uriBuilder, CommonUrlParts.APP_VERSION_CODE,
                mDbReportRequestConfig.appBuildNumber, requestConfig.getAppBuildNumber());
            appendIfEmptyToDef(uriBuilder, CommonUrlParts.OS_VERSION,
                mDbReportRequestConfig.osVersion, requestConfig.getOsVersion());
            appendIfNotEmpty(uriBuilder, CommonUrlParts.OS_API_LEVEL,
                mDbReportRequestConfig.osApiLevel);
            appendIfNotEmpty(uriBuilder, CommonUrlParts.ANALYTICS_SDK_BUILD_NUMBER,
                mDbReportRequestConfig.kitBuildNumber);
            appendIfNotEmpty(uriBuilder, CommonUrlParts.ANALYTICS_SDK_BUILD_TYPE,
                mDbReportRequestConfig.kitBuildType);
            appendIfNotEmpty(uriBuilder, UrlParts.APP_DEBUGGABLE, mDbReportRequestConfig.appDebuggable);
            appendIfEmptyToDef(uriBuilder, CommonUrlParts.LOCALE,
                mDbReportRequestConfig.locale, requestConfig.getLocale());
            appendIfEmptyToDef(uriBuilder, CommonUrlParts.ROOT_STATUS,
                mDbReportRequestConfig.deviceRootStatus, requestConfig.getDeviceRootStatus());
            appendIfEmptyToDef(uriBuilder, CommonUrlParts.APP_FRAMEWORK,
                mDbReportRequestConfig.appFramework, requestConfig.getAppFramework());
            appendIfNotEmpty(uriBuilder, UrlParts.ATTRIBUTION_ID, mDbReportRequestConfig.attributionId);
        } else {
            DebugLogger.warning(TAG, "dbReportRequestConfig is null.");
        }
    }

    private void appendParamsFromCurrentConfiguration(@NonNull Uri.Builder uriBuilder,
                                                      @NonNull ReportRequestConfig requestConfig) {
        uriBuilder.appendQueryParameter(UrlParts.API_KEY_128, requestConfig.getApiKey());
        uriBuilder.appendQueryParameter(CommonUrlParts.APP_ID,
            requestConfig.getPackageName());
        uriBuilder.appendQueryParameter(CommonUrlParts.APP_PLATFORM,
            requestConfig.getAppPlatform());
        uriBuilder.appendQueryParameter(CommonUrlParts.MODEL,
            requestConfig.getModel());
        uriBuilder.appendQueryParameter(CommonUrlParts.MANUFACTURER,
            requestConfig.getManufacturer());
        uriBuilder.appendQueryParameter(CommonUrlParts.SCREEN_WIDTH,
            String.valueOf(requestConfig.getScreenWidth()));
        uriBuilder.appendQueryParameter(CommonUrlParts.SCREEN_HEIGHT,
            String.valueOf(requestConfig.getScreenHeight()));
        uriBuilder.appendQueryParameter(CommonUrlParts.SCREEN_DPI,
            String.valueOf(requestConfig.getScreenDpi()));
        uriBuilder.appendQueryParameter(CommonUrlParts.SCALE_FACTOR,
            String.valueOf(requestConfig.getScaleFactor()));
        uriBuilder.appendQueryParameter(CommonUrlParts.DEVICE_TYPE,
            requestConfig.getDeviceType());
        appendIfNotEmpty(uriBuilder, UrlParts.CLIDS_SET, requestConfig.getClidsFromStartupResponse());
        uriBuilder.appendQueryParameter(CommonUrlParts.APP_SET_ID,
            requestConfig.getAppSetId());
        uriBuilder.appendQueryParameter(CommonUrlParts.APP_SET_ID_SCOPE,
            requestConfig.getAppSetIdScope());
        advIdAppender.appendParams(uriBuilder, requestConfig.getAdvertisingIdsHolder());
    }

    private void appendIfEmptyToDef(Uri.Builder builder, String key, String value, String fallbackValue) {
        String paramValue = StringUtils.ifIsEmptyToDef(value, fallbackValue);
        builder.appendQueryParameter(key, paramValue);
    }

    private void appendIfNotEmpty(Uri.Builder builder, String key, String value) {
        if (TextUtils.isEmpty(value) == false) {
            builder.appendQueryParameter(key, value);
        }
    }
}
