package io.appmetrica.analytics.impl.id.reflection;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.impl.id.AdvIdExtractor;
import io.appmetrica.analytics.impl.id.NoRetriesStrategy;
import io.appmetrica.analytics.impl.id.RetryStrategy;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionAdvIdExtractor implements AdvIdExtractor {

    private static final String CLASS = "io.appmetrica.analytics.identifiers.internal.AdvIdentifiersProvider";
    private static final String METHOD = "requestIdentifiers";
    private static final String TAG = "[ReflectionAdvIdProvider]";

    @NonNull
    private final String provider;
    @NonNull
    private final ReflectionAdvIdParser parser;

    public ReflectionAdvIdExtractor(@NonNull String provider) {
        this(provider, new ReflectionAdvIdParser());
    }

    @VisibleForTesting
    ReflectionAdvIdExtractor(@NonNull String provider, @NonNull ReflectionAdvIdParser parser) {
        this.provider = provider;
        this.parser = parser;
    }

    @NonNull
    @Override
    public AdTrackingInfoResult extractAdTrackingInfo(@NonNull Context context) {
        return extractAdTrackingInfo(context, new NoRetriesStrategy());
    }

    @NonNull
    @Override
    public AdTrackingInfoResult extractAdTrackingInfo(@NonNull Context context, @NonNull RetryStrategy retryStrategy) {
        DebugLogger.INSTANCE.info(TAG, "getAdTrackingInfo. Connecting to library for %s adv_id", provider);
        AdTrackingInfoResult result = null;
        if (ReflectionUtils.detectClassExists(CLASS)) {
            retryStrategy.reset();
            while (retryStrategy.nextAttempt()) {
                try {
                    return tryToGetAdTrackingInfo(context);
                } catch (InvocationTargetException ite) {
                    DebugLogger.INSTANCE.error(TAG, ite, "can't fetch adv id");
                    String message = ite.getTargetException() != null ? ite.getTargetException().getMessage() : null;
                    result = new AdTrackingInfoResult(null,
                        IdentifierStatus.UNKNOWN,
                        "exception while fetching " + provider + " adv_id: " + message
                    );
                } catch (Throwable e) {
                    DebugLogger.INSTANCE.error(TAG, e, "can't fetch adv id");
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
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                " [%s] Class %s not found. " +
                    "Module io.appmetrica.analytics:analytics-identifiers does not exist. " +
                    "So ignore attempts to retrieve identifier",
                provider,
                CLASS
            );
            result = new AdTrackingInfoResult(
                null,
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                "Module io.appmetrica.analytics:analytics-identifiers does not exist"
            );
        }
        return result == null ? new AdTrackingInfoResult() : result;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private AdTrackingInfoResult tryToGetAdTrackingInfo(@NonNull Context context) throws Throwable {
        Class clazz = Class.forName(CLASS);
        Method method = clazz.getMethod(METHOD, Context.class, Bundle.class);
        Bundle data = new Bundle();
        data.putString(Constants.PROVIDER, provider);
        return parser.fromBundle((Bundle) method.invoke(null, context, data));
    }

}
