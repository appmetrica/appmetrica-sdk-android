package io.appmetrica.analytics.impl.id;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.id.reflection.Constants;
import io.appmetrica.analytics.impl.id.reflection.ReflectionAdvIdProvider;
import io.appmetrica.analytics.impl.startup.StartupState;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class AdvertisingIdGetter implements IAdvertisingIdGetter {

    private static final String NO_STARTUP = "startup has not been received yet";
    private static final String FEATURE_DISABLED = "startup forbade advertising identifiers collecting";
    private static final String CANNOT_TRACK_AID = "identifiers collecting is forbidden for unknown reason";

    interface RestrictionsProvider {

        boolean canTrackAid(@Nullable StartupState startupState);

    }

    public static class AlwaysAllowedRestrictionsProvider implements RestrictionsProvider {

        @Override
        public boolean canTrackAid(@Nullable StartupState startupState) {
            return true;
        }

    }

    public static class NeverAllowedRestrictionsProvider implements RestrictionsProvider {

        @Override
        public boolean canTrackAid(@Nullable StartupState startupState) {
            return false;
        }

    }

    public static class ServiceInternalGaidRestrictionProvider implements RestrictionsProvider {

        @Override
        public boolean canTrackAid(@Nullable StartupState startupState) {
            return startupState != null &&
                    (startupState.getCollectingFlags().googleAid || startupState.getHadFirstStartup() == false);
        }
    }

    public static class ServicePublicGaidRestrictionProvider implements RestrictionsProvider {

        @Override
        public boolean canTrackAid(@Nullable StartupState startupState) {
            return startupState != null && startupState.getCollectingFlags().googleAid;
        }
    }

    public static class InternalHoaidRestrictionProvider implements RestrictionsProvider {

        @Override
        public boolean canTrackAid(@Nullable StartupState startupState) {
            return startupState != null &&
                    (startupState.getCollectingFlags().huaweiOaid || startupState.getHadFirstStartup() == false);
        }
    }

    public static class PublicHoaidRestrictionProvider implements RestrictionsProvider {

        @Override
        public boolean canTrackAid(@Nullable StartupState startupState) {
            return startupState != null && startupState.getCollectingFlags().huaweiOaid;
        }
    }

    private final String logTag;

    private final Object mInitLock = new Object();

    @Nullable
    private StartupState mStartupState;

    private volatile FutureTask<Void> mInitFuture;
    @NonNull
    private final RestrictionsProvider gaidRestrictionsProvider;
    @NonNull
    private final RestrictionsProvider hoaidRestrictionsProvider;
    @NonNull
    private final RestrictionsProvider yandexRestrictionsProvider;
    @NonNull
    private final AdvIdProvider mGoogleProvider;
    @NonNull
    private final AdvIdProvider mHuaweiProvider;
    @NonNull
    private final AdvIdProvider yandexProvider;
    @Nullable
    private Context mContext;
    @NonNull
    private ICommonExecutor mExecutor;
    @NonNull
    private volatile AdvertisingIdsHolder mAdvertisingIdsHolder;

    public AdvertisingIdGetter(@NonNull RestrictionsProvider gaidRestrictionsProvider,
                               @NonNull RestrictionsProvider hoaidRestrictionsProvider,
                               @NonNull RestrictionsProvider yandexRestrictionProvider,
                               @NonNull ICommonExecutor executor,
                               final String logSuffix) {
        this(
                gaidRestrictionsProvider,
                hoaidRestrictionsProvider,
                yandexRestrictionProvider,
                executor,
                new AdvIdProviderWrapper(new ReflectionAdvIdProvider(Constants.Providers.GOOGLE)),
                new AdvIdProviderWrapper(new ReflectionAdvIdProvider(Constants.Providers.HUAWEI)),
                new AdvIdProviderWrapper(new ReflectionAdvIdProvider(Constants.Providers.YANDEX)),
                logSuffix
        );
    }

    @VisibleForTesting
    AdvertisingIdGetter(@NonNull RestrictionsProvider gaidRestrictionsProvider,
                        @NonNull RestrictionsProvider hoaidRestrictionsProvider,
                        @NonNull RestrictionsProvider yandexRestrictionProvider,
                        @NonNull ICommonExecutor executor,
                        @NonNull AdvIdProvider googleProvider,
                        @NonNull AdvIdProvider huaweiProvider,
                        @NonNull AdvIdProvider yandexProvider,
                        final String logSuffix) {
        this.gaidRestrictionsProvider = gaidRestrictionsProvider;
        this.hoaidRestrictionsProvider = hoaidRestrictionsProvider;
        this.yandexRestrictionsProvider = yandexRestrictionProvider;
        mGoogleProvider = googleProvider;
        mHuaweiProvider = huaweiProvider;
        this.yandexProvider = yandexProvider;
        mExecutor = executor;
        mAdvertisingIdsHolder = new AdvertisingIdsHolder();
        logTag = "[AdvertisingIdGetter" + logSuffix + "]";
    }

    @Override
    public void lazyInit(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void init(@NonNull final Context context) {
        mContext = context.getApplicationContext();
        if (mInitFuture == null) {
            synchronized (mInitLock) {
                if (mInitFuture == null) {
                    mInitFuture = new FutureTask<Void>(
                            new Callable<Void>() {
                                @Override
                                public Void call() {
                                    YLogger.d("%s init advertising identifiers", logTag);
                                    mAdvertisingIdsHolder = new AdvertisingIdsHolder(
                                            getGaidIfAllowed(mContext),
                                            getHoaidIfAllowed(mContext),
                                            getYandexAdvIdIfAllowed(mContext, new NoRetriesStrategy())
                                    );
                                    return null;
                                }
                            });
                    mExecutor.execute(mInitFuture);
                }
            }
        }
    }

    @Override
    public void init(@NonNull final Context context, @Nullable StartupState startupState) {
        mStartupState = startupState;
        init(context);
    }

    @Override
    public void onStartupStateChanged(@NonNull StartupState startupState) {
        mStartupState = startupState;
    }

    private void getValue(@NonNull FutureTask<Void> future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            YLogger.e("can't get adv_id. Error: %s", e.getMessage());
        } catch (ExecutionException e) {
            YLogger.e("can't get adv_id. Error: %s", e.getMessage());
        }
    }

    @Override
    @NonNull
    public AdvertisingIdsHolder getIdentifiers(@NonNull final Context context) {
        init(context);
        getValue(mInitFuture);
        return mAdvertisingIdsHolder;
    }

    @Override
    @NonNull
    public AdvertisingIdsHolder getIdentifiersForced(@NonNull final Context context) {
        return getIdentifiersForced(context, new NoRetriesStrategy());
    }

    @Override
    @NonNull
    public AdvertisingIdsHolder getIdentifiersForced(@NonNull final Context context,
                                                     @NonNull final RetryStrategy yandexRetryStrategy) {
        final Context applicationContext = context.getApplicationContext();
        final FutureTask<Void> getIdentifiersWithRetriesFuture = new FutureTask<Void>(
                new Callable<Void>() {
                    @Override
                    public Void call() {
                        YLogger.d("%s get advertising identifiers forced", logTag);
                        AdvertisingIdsHolder advertisingIdsHolderFixed = mAdvertisingIdsHolder;
                        mAdvertisingIdsHolder = new AdvertisingIdsHolder(
                                mergeIdentifierData(
                                        getGaidIfAllowed(applicationContext),
                                        advertisingIdsHolderFixed.getGoogle()
                                ),
                                mergeIdentifierData(
                                        getHoaidIfAllowed(applicationContext),
                                        advertisingIdsHolderFixed.getHuawei()
                                ),
                                mergeIdentifierData(
                                        getYandexAdvIdIfAllowed(applicationContext, yandexRetryStrategy),
                                        advertisingIdsHolderFixed.getYandex()
                                )
                        );
                        return null;
                    }
                });
        mExecutor.execute(getIdentifiersWithRetriesFuture);
        getValue(getIdentifiersWithRetriesFuture);
        return mAdvertisingIdsHolder;
    }

    private AdTrackingInfoResult mergeIdentifierData(@NonNull AdTrackingInfoResult newData,
                                                     @NonNull AdTrackingInfoResult cachedData) {
        if (newData.mStatus != IdentifierStatus.OK) {
            return new AdTrackingInfoResult(cachedData.mAdTrackingInfo, newData.mStatus, newData.mErrorExplanation);
        } else {
            return newData;
        }
    }

    @NonNull
    private AdTrackingInfoResult getGaidIfAllowed(@NonNull Context context) {
        AdTrackingInfoResult result;
        if (gaidRestrictionsProvider.canTrackAid(mStartupState)) {
            result = mGoogleProvider.getAdTrackingInfo(context);
        } else if (mStartupState == null || mStartupState.getHadFirstStartup() == false) {
            result = new AdTrackingInfoResult(null, IdentifierStatus.NO_STARTUP, NO_STARTUP);
        } else if (mStartupState.getCollectingFlags().googleAid == false) {
            result = new AdTrackingInfoResult(null, IdentifierStatus.FEATURE_DISABLED, FEATURE_DISABLED);
        } else {
            result = new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, CANNOT_TRACK_AID);
        }
        return result;
    }

    @NonNull
    private AdTrackingInfoResult getHoaidIfAllowed(@NonNull Context context) {
        AdTrackingInfoResult result;
        if (hoaidRestrictionsProvider.canTrackAid(mStartupState)) {
            result = mHuaweiProvider.getAdTrackingInfo(context);
        } else if (mStartupState == null || mStartupState.getHadFirstStartup() == false) {
            result = new AdTrackingInfoResult(null, IdentifierStatus.NO_STARTUP, NO_STARTUP);
        } else if (mStartupState.getCollectingFlags().huaweiOaid == false) {
            result = new AdTrackingInfoResult(null, IdentifierStatus.FEATURE_DISABLED, FEATURE_DISABLED);
        } else {
            result = new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, CANNOT_TRACK_AID);
        }
        return result;
    }

    @NonNull
    private AdTrackingInfoResult getYandexAdvIdIfAllowed(@NonNull Context context,
                                                         @NonNull RetryStrategy retryStrategy) {
        AdTrackingInfoResult result;
        if (yandexRestrictionsProvider.canTrackAid(mStartupState)) {
            result = yandexProvider.getAdTrackingInfo(context, retryStrategy);
        } else {
            result = new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, CANNOT_TRACK_AID);
        }
        return result;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public ICommonExecutor getExecutor() {
        return mExecutor;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public RestrictionsProvider getGaidRestrictionsProvider() {
        return gaidRestrictionsProvider;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public RestrictionsProvider getHoaidRestrictionsProvider() {
        return hoaidRestrictionsProvider;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public RestrictionsProvider getYandexRestrictionsProvider() {
        return yandexRestrictionsProvider;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public String getLogTag() {
        return logTag;
    }
}
