package io.appmetrica.analytics.impl;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.impl.adrevenue.AppMetricaAutoAdRevenueReporter;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.modules.ModulesSeeker;
import io.appmetrica.analytics.impl.modules.client.ClientContextFacade;
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper;
import io.appmetrica.analytics.impl.referrer.common.Constants;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.List;
import java.util.Map;

public class AppMetricaImpl implements IAppMetricaImpl {

    private static final String TAG = "[AppMetricaImplFull]";

    @NonNull
    private final Context mContext;
    @NonNull
    private final PreferencesClientDbStorage mClientPreferences;
    @NonNull
    private final StartupHelper mStartupHelper;
    @NonNull
    private final ReferrerHelper mReferrerHelper;
    @NonNull
    private final ProcessConfiguration mProcessConfiguration;
    @NonNull
    private final ReporterFactory mReporterFactory;
    @NonNull
    private final ReportsHandler mReportsHandler;
    @NonNull
    private final DefaultOneShotMetricaConfig mDefaultOneShotMetricaConfig;
    @NonNull
    private final AppOpenWatcher appOpenWatcher;
    @NonNull
    private final SessionsTrackingManager sessionsTrackingManager;
    @Nullable
    private volatile MainReporterApiConsumerProvider mainReporterApiConsumerProvider;
    @NonNull
    private final ModulesSeeker modulesSeeker = new ModulesSeeker();

    @WorkerThread
    AppMetricaImpl(@NonNull Context context, @NonNull IAppMetricaCore appMetricaCore) {
        this(
                context.getApplicationContext(),
                appMetricaCore,
                new PreferencesClientDbStorage(DatabaseStorageFactory
                        .getInstance(context.getApplicationContext()).getClientDbHelper()
                )
        );
    }

    @WorkerThread
    private AppMetricaImpl(@NonNull Context context,
                              @NonNull IAppMetricaCore appMetricaCore,
                              @NonNull PreferencesClientDbStorage preferences) {
        this(
                context,
                appMetricaCore,
                preferences,
                new AppMetricaImplFieldsProvider(),
                ClientServiceLocator.getInstance()
        );
    }

    @WorkerThread
    @VisibleForTesting
    AppMetricaImpl(@NonNull Context context,
                      @NonNull IAppMetricaCore appMetricaCore,
                      @NonNull PreferencesClientDbStorage preferences,
                      @NonNull AppMetricaImplFieldsProvider fieldsProvider,
                      @NonNull ClientServiceLocator clientServiceLocator) {
        mContext = context;
        modulesSeeker.discoverClientModules();
        ClientServiceLocator.getInstance().getModulesController().initClientSide(
            new ClientContextFacade(
                new AppMetricaAutoAdRevenueReporter()
            )
        );
        mClientPreferences = preferences;
        final Handler metricaHandler = appMetricaCore.getMetricaHandler();
        final DataResultReceiver dataResultReceiver = fieldsProvider.createDataResultReceiver(metricaHandler, this);
        mProcessConfiguration = fieldsProvider.createProcessConfiguration(mContext, dataResultReceiver);
        mDefaultOneShotMetricaConfig = clientServiceLocator.getDefaultOneShotConfig();
        mReportsHandler = fieldsProvider.createReportsHandler(
                mProcessConfiguration,
                mContext,
                appMetricaCore.getExecutor()
        );
        mDefaultOneShotMetricaConfig.setReportsHandler(mReportsHandler);

        mStartupHelper =
                fieldsProvider.createStartupHelper(mContext, mReportsHandler, mClientPreferences, metricaHandler);
        this.appOpenWatcher = appMetricaCore.getAppOpenWatcher();
        mReportsHandler.setStartupParamsProvider(mStartupHelper);
        mReferrerHelper = fieldsProvider.createReferrerHelper(mReportsHandler, mClientPreferences, metricaHandler);
        mReporterFactory = fieldsProvider.createReporterFactory(
                mContext,
                mProcessConfiguration,
                mReportsHandler,
                metricaHandler,
                mStartupHelper
        );
        sessionsTrackingManager = clientServiceLocator.getSessionsTrackingManager();
    }

    @Override
    @WorkerThread
    public void activate(@NonNull AppMetricaConfig originalConfig,
                         @NonNull final AppMetricaConfig config) {
        PublicLogger publicLogger = LoggerStorage.getOrCreatePublicLogger(config.apiKey);
        boolean needToClearEnvironment = mDefaultOneShotMetricaConfig.wasAppEnvironmentCleared();
        if (null == mainReporterApiConsumerProvider) {
            mReferrerHelper.maybeRequestReferrer();
            mStartupHelper.setPublicLogger(publicLogger);
            initStartup(config);
            mProcessConfiguration.update(config);
            initMainReporterForApiKey(config, needToClearEnvironment);
            Log.i(SdkUtils.APPMETRICA_TAG,
                    "Activate AppMetrica with APIKey " + Utils.createPartialApiKey(config.apiKey));
            if (BooleanUtils.isTrue(config.logs)) {
                publicLogger.setEnabled(true);
                LoggerStorage.getAnonymousPublicLogger().setEnabled(true);
            } else {
                publicLogger.setEnabled(false);
                LoggerStorage.getAnonymousPublicLogger().setEnabled(false);
            }
        } else {
            if (publicLogger.isEnabled()) {
                publicLogger.w("Appmetrica already has been activated!");
            }
        }
    }

    @Override
    @AnyThread
    @Nullable
    public MainReporterApiConsumerProvider getMainReporterApiConsumerProvider() {
        return mainReporterApiConsumerProvider;
    }

    @AnyThread
    @Override
    public void onReceiveResult(int resultCode, @NonNull Bundle resultData) {
        DebugLogger.info(TAG, "On receive data, result code: %d", resultCode);
        mStartupHelper.processResultFromResultReceiver(resultData);
    }

    @Override
    @WorkerThread
    public void requestDeferredDeeplinkParameters(DeferredDeeplinkParametersListener listener) {
        mReferrerHelper.requestDeferredDeeplinkParameters(listener);
    }

    @Override
    @WorkerThread
    public void requestDeferredDeeplink(DeferredDeeplinkListener listener) {
        mReferrerHelper.requestDeferredDeeplink(listener);
    }

    @Override
    @WorkerThread
    public void activateReporter(@NonNull ReporterConfig config) {
        mReporterFactory.activateReporter(config);
    }

    @Override
    @WorkerThread
    @NonNull
    public IReporterExtended getReporter(@NonNull ReporterConfig config) {
        return mReporterFactory.getOrCreateReporter(config);
    }

    @Override
    @AnyThread
    @Nullable
    public String getDeviceId() {
        return mStartupHelper.getDeviceId();
    }

    @Override
    @AnyThread
    @NonNull
    public AdvIdentifiersResult getCachedAdvIdentifiers() {
        return mStartupHelper.getCachedAdvIdentifiers();
    }

    @Override
    @AnyThread
    @NonNull
    public FeaturesResult getFeatures() {
        return mStartupHelper.getFeatures();
    }

    @Override
    @AnyThread
    @Nullable
    public Map<String, String> getClids() {
        return mStartupHelper.getClids();
    }

    @WorkerThread
    @Override
    public void requestStartupParams(
            @NonNull final StartupParamsCallback callback,
            @NonNull final List<String> params
    ) {
        mStartupHelper.requestStartupParams(callback, params, mProcessConfiguration.getClientClids());
    }

    @WorkerThread
    private void initMainReporterForApiKey(AppMetricaConfig config, final boolean needToClearEnvironment) {
        mReportsHandler.updatePreActivationConfig(config.locationTracking, config.dataSendingEnabled);
        MainReporter reporter = mReporterFactory.buildMainReporter(config, needToClearEnvironment);
        mainReporterApiConsumerProvider = new MainReporterApiConsumerProvider(reporter);
        appOpenWatcher.setDeeplinkConsumer(mainReporterApiConsumerProvider.getDeeplinkConsumer());
        sessionsTrackingManager.setReporter(reporter);
        mStartupHelper.sendStartupIfNeeded();
    }

    @WorkerThread
    private void initStartup(@Nullable final AppMetricaConfig config) {
        if (config != null) {
            mStartupHelper.setCustomHosts(config.customHosts);
            mStartupHelper.setClids(AppMetricaInternalConfigExtractor.getClids(config));
            final String distributionReferrer = AppMetricaInternalConfigExtractor.getDistributionReferrer(config);
            mStartupHelper.setDistributionReferrer(distributionReferrer);
            if (distributionReferrer != null) {
                mStartupHelper.setInstallReferrerSource(Constants.INSTALL_REFERRER_SOURCE_API);
            }
        }
    }

    @WorkerThread
    @Override
    public void setLocation(@Nullable Location location) {
        getMainReporter().setLocation(location);
    }

    @WorkerThread
    @Override
    public void setLocationTracking(boolean enabled) {
        getMainReporter().setLocationTracking(enabled);
    }

    @WorkerThread
    @Override
    public void setDataSendingEnabled(boolean value) {
        getMainReporter().setDataSendingEnabled(value);
    }

    @WorkerThread
    @Override
    public void putAppEnvironmentValue(String key, String value) {
        getMainReporter().putAppEnvironmentValue(key, value);
    }

    @WorkerThread
    @Override
    public void clearAppEnvironment() {
        getMainReporter().clearAppEnvironment();
    }

    @WorkerThread
    @Override
    public void putErrorEnvironmentValue(String key, String value) {
        getMainReporter().putErrorEnvironmentValue(key, value);
    }

    @WorkerThread
    @Override
    public void setUserProfileID(@Nullable String userProfileID) {
        getMainReporter().setUserProfileID(userProfileID);
    }

    @AnyThread
    @Override
    @NonNull
    public ReporterFactory getReporterFactory() {
        return mReporterFactory;
    }

    @NonNull
    private IMainReporter getMainReporter() {
        return mainReporterApiConsumerProvider.getMainReporter();
    }
}
