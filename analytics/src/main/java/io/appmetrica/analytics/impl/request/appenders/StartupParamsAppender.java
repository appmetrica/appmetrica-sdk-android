package io.appmetrica.analytics.impl.request.appenders;

import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.modules.ModulesRemoteConfigArgumentsCollector;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.request.Obfuscator;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.request.UrlParts;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.internal.CommonUrlParts;
import io.appmetrica.analytics.networktasks.internal.IParamsAppender;
import java.util.Map;

public class StartupParamsAppender implements IParamsAppender<StartupRequestConfig> {

    private static final String TAG = "[StartupParamsAppender]";

    @NonNull
    private final Obfuscator mObfuscator;
    @NonNull
    private final ModulesRemoteConfigArgumentsCollector modulesArgumentsCollector;
    @NonNull
    private final LiveConfigProvider liveConfigProvider;

    public StartupParamsAppender(@NonNull Obfuscator obfuscator,
                                 @NonNull ModulesRemoteConfigArgumentsCollector modulesArgumentsCollector) {
        mObfuscator = obfuscator;
        this.modulesArgumentsCollector = modulesArgumentsCollector;
        this.liveConfigProvider = new LiveConfigProvider();
    }

    @SuppressWarnings("checkstyle:methodLength")
    @Override
    public void appendParams(@NonNull Uri.Builder uriBuilder, @NonNull StartupRequestConfig requestConfig) {
        DebugLogger.INSTANCE.info(TAG, "append params with config: %s", requestConfig);
        uriBuilder.path(UrlParts.STARTUP_PATH);

        //deviceid parameter must be present even if empty. Without it no device id will be returned.
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(CommonUrlParts.DEVICE_ID),
            requestConfig.getDeviceId());

        appendAdvIdIfAllowed(
            uriBuilder,
            GlobalServiceLocator.getInstance().getDataSendingRestrictionController(),
            liveConfigProvider
        );
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(CommonUrlParts.APP_SET_ID),
            requestConfig.getAppSetId());
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.APP_SET_ID_SCOPE),
            requestConfig.getAppSetIdScope()
        );

        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(CommonUrlParts.APP_PLATFORM),
            requestConfig.getAppPlatform());
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.PROTOCOL_VERSION),
            requestConfig.getProtocolVersion()
        );
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.ANALYTICS_SDK_VERSION_NAME),
            requestConfig.getAnalyticsSdkVersionName()
        );
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.MODEL),
            requestConfig.getModel()
        );
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.MANUFACTURER),
            requestConfig.getManufacturer()
        );
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.OS_VERSION),
            requestConfig.getOsVersion()
        );
        uriBuilder.appendQueryParameter(
                mObfuscator.obfuscate(CommonUrlParts.SCREEN_WIDTH),
            String.valueOf(requestConfig.getScreenWidth())
        );
        uriBuilder.appendQueryParameter(
                mObfuscator.obfuscate(CommonUrlParts.SCREEN_HEIGHT),
            String.valueOf(requestConfig.getScreenHeight())
        );
        uriBuilder.appendQueryParameter(
                mObfuscator.obfuscate(CommonUrlParts.SCREEN_DPI),
            String.valueOf(requestConfig.getScreenDpi())
        );
        uriBuilder.appendQueryParameter(
                mObfuscator.obfuscate(CommonUrlParts.SCALE_FACTOR),
            String.valueOf(requestConfig.getScaleFactor())
        );
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.LOCALE),
            requestConfig.getLocale()
        );
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.DEVICE_TYPE),
            requestConfig.getDeviceType()
        );
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.QUERIES), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.QUERY_HOSTS), String.valueOf(2));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.FEATURES),
                StringUtils.wrapFeatures(
                        Utils.joinToArray(
                                //modulesArgumentsCollector provides obfuscated features
                                modulesArgumentsCollector.collectFeatures(),
                                mObfuscator.obfuscate(UrlParts.PERMISSIONS_COLLECTING),
                                mObfuscator.obfuscate(UrlParts.FEATURES_COLLECTING),
                                mObfuscator.obfuscate(UrlParts.FEATURE_GOOGLE_AID),
                                mObfuscator.obfuscate(UrlParts.FEATURE_HUAWEI_OAID),
                                mObfuscator.obfuscate(UrlParts.FEATURE_SIM_INFO),
                                mObfuscator.obfuscate(UrlParts.FEATURE_SSL_PINNING)
                        )
                )
        );
        uriBuilder.appendQueryParameter(
            mObfuscator.obfuscate(CommonUrlParts.APP_ID),
            requestConfig.getPackageName()
        );
        uriBuilder.appendQueryParameter(
                mObfuscator.obfuscate(UrlParts.APP_DEBUGGABLE), requestConfig.isAppDebuggable()
        );

        if (requestConfig.hasSuccessfulStartup()) {
            String countryInit = requestConfig.getCountryInit();
            if (TextUtils.isEmpty(countryInit) == false) {
                uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.COUNTRY_INIT), countryInit);
            }
        } else {
            uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.DETECT_LOCALE), String.valueOf(1));
        }

        ClidsInfo.Candidate clidsInfo = requestConfig.getChosenClids();
        if (!Utils.isNullOrEmpty(clidsInfo.getClids())) {
            uriBuilder.appendQueryParameter(
                    mObfuscator.obfuscate(UrlParts.DISTRIBUTION_CUSTOMIZATION), String.valueOf(1)
            );
            appendParam(uriBuilder, UrlParts.CLIDS_SET, StartupUtils.encodeClids(clidsInfo.getClids()));
            appendParam(uriBuilder, UrlParts.CLIDS_SET_SOURCE, getStringSource(clidsInfo.getSource()));
            appendReferrer(uriBuilder, requestConfig);
        }

        appendParamIfNotEmpty(uriBuilder, CommonUrlParts.UUID, requestConfig.getUuid());
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.TIME), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.STAT_SENDING), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.RETRY_POLICY), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.CACHE_CONTROL), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.PERMISSIONS_COLLECTING), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.APP_SYSTEM), requestConfig.isAppSystem());
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.AUTO_INAPP_COLLECTING), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.ATTRIBUTION), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.STARTUP_UPDATE), String.valueOf(1));
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.EXTERNAL_ATTRIBUTION), String.valueOf(1));
        //modulesArgumentsCollector provides obfuscated blocks
        Map<String, Integer> modulesBlocks = modulesArgumentsCollector.collectBlocks();
        for (String block : modulesBlocks.keySet()) {
            uriBuilder.appendQueryParameter(block, String.valueOf(modulesBlocks.get(block)));
        }
    }

    protected void appendAdvIdIfAllowed(@NonNull Uri.Builder uriBuilder,
                                        @NonNull DataSendingRestrictionController controller,
                                        @NonNull LiveConfigProvider liveConfigProvider) {
        AdvertisingIdsHolder advertisingIdsHolder = liveConfigProvider.getAdvertisingIdentifiers();
        if (advertisingIdsHolder == null || controller.isRestrictedForSdk()) {
            uriBuilder.appendQueryParameter(
                mObfuscator.obfuscate(CommonUrlParts.ADV_ID), "");
            uriBuilder.appendQueryParameter(
                mObfuscator.obfuscate(CommonUrlParts.HUAWEI_OAID), "");
            uriBuilder.appendQueryParameter(
                mObfuscator.obfuscate(CommonUrlParts.YANDEX_ADV_ID), "");
        } else {
            appendAdvId(uriBuilder, advertisingIdsHolder.getGoogle(), CommonUrlParts.ADV_ID);
            appendAdvId(
                uriBuilder,
                advertisingIdsHolder.getHuawei(),
                CommonUrlParts.HUAWEI_OAID
            );
            appendAdvId(
                uriBuilder,
                advertisingIdsHolder.getYandex(),
                CommonUrlParts.YANDEX_ADV_ID
            );
        }
    }

    private void appendAdvId(
            @NonNull Uri.Builder uriBuilder,
            @NonNull AdTrackingInfoResult adTrackingInfoResult,
            @NonNull String parameter
    ) {
        if (adTrackingInfoResult.isValid() == false) {
            uriBuilder.appendQueryParameter(mObfuscator.obfuscate(parameter), "");
        } else {
            uriBuilder.appendQueryParameter(
                    mObfuscator.obfuscate(parameter),
                    adTrackingInfoResult.mAdTrackingInfo.advId
            );
        }
    }

    private void appendReferrer(@NonNull Uri.Builder uriBuilder, @NonNull StartupRequestConfig requestConfig) {
        String referrer = requestConfig.getDistributionReferrer();
        String installReferrerSource = requestConfig.getInstallReferrerSource();
        DebugLogger.INSTANCE.info(
            TAG,
            "append referrer. Referrer from config: %s, source: %s",
            referrer,
            installReferrerSource
        );
        if (TextUtils.isEmpty(referrer)) {
            final ReferrerInfo referrerInfo = requestConfig.getReferrerHolder().getReferrerInfo();
            DebugLogger.INSTANCE.info(TAG, "referrer from ReferrerHolder: %s", referrerInfo);
            if (referrerInfo != null) {
                referrer = referrerInfo.installReferrer;
                installReferrerSource = referrerInfo.source.value;
            }
        }
        if (TextUtils.isEmpty(referrer) == false) {
            uriBuilder.appendQueryParameter(mObfuscator.obfuscate(UrlParts.DISTRIBUTION_REFERRER), referrer);
            if (installReferrerSource == null) {
                installReferrerSource = "null";
            }
            uriBuilder.appendQueryParameter(
                    mObfuscator.obfuscate(UrlParts.INSTALL_REFERRER_SOURCE),
                    installReferrerSource
            );
        }
    }

    private void appendParamIfNotEmpty(@NonNull final Uri.Builder uriBuilder,
                                       @NonNull final String key,
                                       final String value) {
        if (!TextUtils.isEmpty(value)) {
            appendParam(uriBuilder, key, value);
        }
    }

    private void appendParam(@NonNull final Uri.Builder uriBuilder,
                             @NonNull final String key,
                             @NonNull final String value) {
        uriBuilder.appendQueryParameter(mObfuscator.obfuscate(key), value);
    }

    @NonNull
    private String getStringSource(@NonNull DistributionSource source) {
        switch (source) {
            case APP:
                return "api";
            case SATELLITE:
                return "satellite";
            case RETAIL:
                return "retail";
            default:
                return StringUtils.EMPTY;
        }
    }

}
