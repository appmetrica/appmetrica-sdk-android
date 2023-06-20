package io.appmetrica.analytics.impl.id.reflection;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.id.AdvIdProvider;
import io.appmetrica.analytics.impl.id.NoRetriesStrategy;
import io.appmetrica.analytics.impl.id.RetryStrategy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionAdsIdProvider implements AdvIdProvider {

    private static final String CLASS = "io.appmetrica.analytics.identifiers.internal.AdsIdentifiersProvider";
    private static final String METHOD = "requestIdentifiers";
    private static final String TAG = "[ReflectionAdsIdProvider]";

    @NonNull
    private final String provider;
    @NonNull
    private final ReflectionAdsIdParser parser;

    public ReflectionAdsIdProvider(@NonNull String provider) {
        this(provider, new ReflectionAdsIdParser());
    }

    @VisibleForTesting
    ReflectionAdsIdProvider(@NonNull String provider, @NonNull ReflectionAdsIdParser parser) {
        this.provider = provider;
        this.parser = parser;
    }

    @NonNull
    @Override
    public AdTrackingInfoResult getAdTrackingInfo(@NonNull Context context) {
        return getAdTrackingInfo(context, new NoRetriesStrategy());
    }

    @NonNull
    @Override
    public AdTrackingInfoResult getAdTrackingInfo(@NonNull Context context, @NonNull RetryStrategy retryStrategy) {
        YLogger.info(TAG, "getAdTrackingInfo. Connecting to library for %s adv_id", provider);
        retryStrategy.reset();
        AdTrackingInfoResult result = null;
        while (retryStrategy.nextAttempt()) {
            try {
                return tryToGetAdTrackingInfo(context);
            } catch (InvocationTargetException ite) {
                YLogger.error(TAG, ite, "can't fetch adv id");
                String message = ite.getTargetException() != null ? ite.getTargetException().getMessage() : null;
                result = new AdTrackingInfoResult(null,
                        IdentifierStatus.UNKNOWN,
                        "exception while fetching " + provider + " adv_id: " + message
                );
            } catch (Throwable e) {
                YLogger.error(TAG, e, "can't fetch adv id");
                result = new AdTrackingInfoResult(null,
                        IdentifierStatus.UNKNOWN,
                        "exception while fetching " + provider + " adv_id: " + e.getMessage()
                );
            }
            try {
                Thread.sleep(retryStrategy.getTimeout());
            } catch (InterruptedException ignored) {
            }
        }
        return result == null ? new AdTrackingInfoResult() : result;
    }

    @Nullable
    private AdTrackingInfoResult tryToGetAdTrackingInfo(@NonNull Context context) throws Throwable {
        Class clazz = Class.forName(CLASS);
        Method method = clazz.getMethod(METHOD, Context.class, Bundle.class);
        Bundle data = new Bundle();
        data.putString(Constants.PROVIDER, provider);
        return parser.fromBundle((Bundle) method.invoke(null, context, data));
    }

}
