package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.CommutationComponentId;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.IBaseComponent;
import io.appmetrica.analytics.impl.network.NetworkTaskFactory;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.parsing.StartupParser;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.utils.DeviceIdGenerator;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.impl.utils.TimeUtils;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;
import java.util.List;
import java.util.Map;
import kotlin.jvm.functions.Function0;

@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public class StartupUnit implements IBaseComponent, IStartupUnit {

    private static final String TAG = "[StartupUnit]";

    @NonNull
    private final Context mContext;
    @NonNull
    private final ComponentId mComponentId;
    @NonNull
    private final StartupResultListener mListener;
    @NonNull
    private final StartupState.Storage mStorage;
    @Nullable
    private volatile NetworkTask mCurrentTask;
    @NonNull
    private StartupConfigurationHolder mConfigHolder;
    @NonNull
    private final TimeProvider systemTimeProvider;
    @NonNull
    private final ClidsInfoStorage clidsStorage;
    @NonNull
    private final ClidsStateChecker clidsStateChecker;

    public StartupUnit(@NonNull Context context,
                       @NonNull String packageName,
                       @NonNull StartupRequestConfig.Arguments arguments,
                       @NonNull StartupResultListener listener) {
        this(
            context,
            new CommutationComponentId(packageName),
            arguments,
            listener,
            new StartupState.Storage(context),
            new DeviceIdGenerator(context),
            new SystemTimeProvider(),
            GlobalServiceLocator.getInstance().getClidsStorage(),
            new ClidsStateChecker()
        );
    }

    @VisibleForTesting
    StartupUnit(@NonNull Context context,
                @NonNull String packageName,
                @NonNull StartupRequestConfig.Arguments arguments,
                @NonNull StartupResultListener listener,
                @NonNull StartupState.Storage stateStorage,
                @NonNull TimeProvider timeProvider,
                @NonNull ClidsInfoStorage clidsStorage,
                @NonNull ClidsStateChecker clidsStateChecker) {
        this(
            context,
            new CommutationComponentId(packageName),
            arguments,
            listener,
            stateStorage,
            new DeviceIdGenerator(context),
            timeProvider,
            clidsStorage,
            clidsStateChecker
        );
    }

    private StartupUnit(@NonNull Context context,
                        @NonNull ComponentId componentId,
                        @NonNull StartupRequestConfig.Arguments clientConfiguration,
                        @NonNull StartupResultListener listener,
                        @NonNull StartupState.Storage storage,
                        @NonNull DeviceIdGenerator deviceIdGenerator,
                        @NonNull TimeProvider timeProvider,
                        @NonNull ClidsInfoStorage clidsStorage,
                        @NonNull ClidsStateChecker clidsStateChecker) {
        this(
            context,
            componentId,
            clientConfiguration,
            listener,
            storage,
            storage.read(),
            deviceIdGenerator,
            timeProvider,
            clidsStorage,
            clidsStateChecker
        );
    }

    private StartupUnit(@NonNull Context context,
                        @NonNull ComponentId componentId,
                        @NonNull StartupRequestConfig.Arguments clientConfiguration,
                        @NonNull StartupResultListener listener,
                        @NonNull StartupState.Storage storage,
                        @NonNull StartupState startupState,
                        @NonNull DeviceIdGenerator deviceIdGenerator,
                        @NonNull TimeProvider timeProvider,
                        @NonNull ClidsInfoStorage clidsStorage,
                        @NonNull ClidsStateChecker clidsStateChecker) {
        this(
            context,
            componentId,
            listener,
            storage,
            startupState,
            deviceIdGenerator,
            new StartupConfigurationHolder(
                new StartupRequestConfig.Loader(context, componentId.getPackage()),
                startupState,
                clientConfiguration
            ),
            timeProvider,
            clidsStorage,
            clidsStateChecker,
            GlobalServiceLocator.getInstance().getMultiProcessSafeUuidProvider()
        );
    }

    @VisibleForTesting
    StartupUnit(@NonNull Context context,
                @NonNull ComponentId componentId,
                @NonNull StartupResultListener listener,
                @NonNull StartupState.Storage storage,
                @NonNull StartupState startupState,
                @NonNull DeviceIdGenerator deviceIdGenerator,
                @NonNull StartupConfigurationHolder startupConfigurationHolder,
                @NonNull TimeProvider timeProvider,
                @NonNull ClidsInfoStorage clidsStorage,
                @NonNull ClidsStateChecker clidsStateChecker,
                @NonNull MultiProcessSafeUuidProvider uuidProvider) {
        mContext = context;
        mComponentId = componentId;
        mListener = listener;
        mStorage = storage;
        mConfigHolder = startupConfigurationHolder;
        systemTimeProvider = timeProvider;
        this.clidsStorage = clidsStorage;
        this.clidsStateChecker = clidsStateChecker;
        YLogger.info(TAG, "Create startup unit for componentId = %s", componentId);
        generateIdentifiersIfNeeded(
            deviceIdGenerator,
            uuidProvider,
            startupState
        );
    }

    private void generateIdentifiersIfNeeded(@NonNull DeviceIdGenerator deviceIdGenerator,
                                             @NonNull MultiProcessSafeUuidProvider multiProcessSafeUuidProvider,
                                             @NonNull StartupState startupState) {
        YLogger.info(TAG, "Generate identifier if needed");
        StartupState.Builder startupStateBuilder = startupState.buildUpon();
        if (TextUtils.isEmpty(startupState.getUuid())) {
            String uuid = multiProcessSafeUuidProvider.readUuid().id;
            startupStateBuilder = startupStateBuilder.withUuid(uuid);
            YLogger.info(TAG, "Extracted new uuid from storage = %s", uuid);
        }
        final String deviceIdCandidate = deviceIdGenerator.generateDeviceId();
        if (TextUtils.isEmpty(startupState.getDeviceId())) {
            YLogger.info(TAG, "Apply new deviceIdCandidate: %s", deviceIdCandidate);
            startupStateBuilder = startupStateBuilder
                .withDeviceId(deviceIdCandidate)
                .withDeviceIdHash(StringUtils.EMPTY);
        }
        StartupState updatedStartupState = startupStateBuilder.build();
        YLogger.info(
            TAG,
            "generate identifiers if needed - uuid: %s -> %s; deviceId: %s -> %s.",
            startupState.getUuid(),
            updatedStartupState.getUuid(),
            startupState.getDeviceId(),
            updatedStartupState.getDeviceId()
        );
        updateCurrentStartupDataAndNotifyListener(updatedStartupState);
    }

    @NonNull
    @VisibleForTesting
    StartupConfigurationHolder getConfigHolder() {
        return mConfigHolder;
    }

    @VisibleForTesting
    void setConfigHolder(@NonNull StartupConfigurationHolder configHolder) {
        mConfigHolder = configHolder;
    }

    @VisibleForTesting
    void setStartupState(@NonNull StartupState startupState) {
        mConfigHolder.updateStartupState(startupState);
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    public ComponentId getComponentId() {
        return mComponentId;
    }

    @Nullable
    public synchronized NetworkTask getOrCreateStartupTaskIfRequired() {
        if (isStartupRequired()) {
            YLogger.info(TAG, "getOrCreateStartupTaskIfRequired - startupRequired");
            if (mCurrentTask == null) {
                YLogger.info(TAG, "getOrCreateStartupTaskIfRequired - create startup task");
                mCurrentTask = NetworkTaskFactory.createStartupTask(this, getRequestConfig());
            }
            return mCurrentTask;
        } else {
            return null;
        }
    }

    public synchronized boolean isStartupRequired(@Nullable List<String> identifiers,
                                                  @NonNull Map<String, String> clidsFromClientForVerification) {
        return !StartupRequiredUtils.containsIdentifiers(
            getStartupState(),
            identifiers,
            clidsFromClientForVerification,
            new Function0<ClidsInfoStorage>() {
                @Override
                public ClidsInfoStorage invoke() {
                    return clidsStorage;
                }
            });
    }

    public synchronized boolean isStartupRequired() {
        // TODO (avitenko) this method can be rewritten in 'lazy-reactive' approach.
        // Because now it works with two immutable states.
        final StartupState startupState = getStartupState();
        boolean required = StartupRequiredUtils.isOutdated(startupState);
        YLogger.info(TAG, "isStartupOutdated: " + required);
        if (!required) {
            required = !StartupRequiredUtils.areMainIdentifiersValid(startupState);
            YLogger.info(TAG, " is startup required because of main identifiers being empty? %b", required);
            if (!required && !clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                mConfigHolder.get().getClidsFromClient(),
                startupState,
                clidsStorage
            )) {
                YLogger.info(TAG, "Startup is required because of clids");
                required = true;
            }
        } else {
            YLogger.info(TAG, "Startup required because it's outdated.");
        }
        return required;
    }

    @NonNull
    public StartupRequestConfig getRequestConfig() {
        return mConfigHolder.get();
    }

    private synchronized void removeCurrentStartupTask() {
        mCurrentTask = null;
    }

    public void onRequestComplete(@NonNull StartupResult result,
                                  @NonNull StartupRequestConfig requestConfig,
                                  @Nullable Map<String, List<String>> responseHeaders) {
        StartupState newState;
        synchronized (this) {
            Long serverTime = WrapUtils.getOrDefault(StartupParser.parseServerTime(responseHeaders), 0L);
            updateServerTime(result.getValidTimeDifference(), serverTime);

            newState = parseStartupResult(result, requestConfig, serverTime);
            YLogger.d(TAG + " new state %s", newState);
            removeCurrentStartupTask();
            updateCurrentStartupData(newState);
        }
        notifyListener(newState);
    }

    @VisibleForTesting
    @SuppressWarnings("checkstyle:methodlength")
    @NonNull
    protected StartupState parseStartupResult(@NonNull StartupResult result,
                                              @NonNull StartupRequestConfig requestConfig,
                                              @NonNull Long serverTime) {
        String clientClidsForRequest = StartupUtils.encodeClids(requestConfig.getClidsFromClient());
        Map<String, String> chosenForRequestClids = requestConfig.getChosenClids().getClids();
        String validClidsFromResponse = chooseValidClids(result.getEncodedClids(),
            getStartupState().getEncodedClidsFromResponse()
        );
        String deviceID = getStartupState().getDeviceId();
        if (TextUtils.isEmpty(deviceID)) {
            deviceID = result.getDeviceId();
        }
        StartupState startupState = getStartupState();
        return new StartupState.Builder(new StartupStateModel.StartupStateBuilder(result.getCollectionFlags()))
            .withDeviceId(deviceID)
            .withDeviceIdHash(result.getDeviceIDHash())
            .withObtainTime(systemTimeProvider.currentTimeSeconds())
            .withUuid(startupState.getUuid())
            .withGetAdUrl(result.getGetAdUrl())
            .withHostUrlsFromStartup(result.getStartupUrls())
            .withHostUrlsFromClient(requestConfig.getStartupHostsFromClient())
            .withReportUrls(result.getReportHostUrls())
            .withReportAdUrl(result.getReportAdUrl())
            .withCertificateUrl(result.getCertificateUrl())
            .withDiagnosticUrls(result.getDiagnosticUrls())
            .withCustomSdkHosts(result.getCustomSdkHosts())
            .withEncodedClidsFromResponse(validClidsFromResponse)
            .withLastClientClidsForStartupRequest(clientClidsForRequest)
            .withStartupDidNotOverrideClids(clidsStateChecker.doRequestClidsMatchResponseClids(
                chosenForRequestClids,
                validClidsFromResponse
            ))
            .withLastChosenForRequestClids(StartupUtils.encodeClids(chosenForRequestClids))
            .withCountryInit(result.getCountryInit())
            .withPermissionsCollectingConfig(result.getPermissionsCollectingConfig())
            .withStatSending(result.getStatSending())
            .withHadFirstStartup(true)
            .withObtainServerTime(WrapUtils.getOrDefault(serverTime, TimeUtils.currentDeviceTimeSec() * 1000))
            .withFirstStartupServerTime(mConfigHolder.get().getOrSetFirstStartupTime(serverTime))
            .withOutdated(false)
            .withRetryPolicyConfig(result.getRetryPolicyConfig())
            .withCacheControl(result.getCacheControl())
            .withAutoInappCollectingConfig(result.getAutoInappCollectingConfig())
            .withAttributionConfig(result.getAttributionConfig())
            .withStartupUpdateConfig(result.getStartupUpdateConfig())
            .withModulesRemoteConfigs(result.getModulesRemoteConfigs())
            .withExternalAttributionConfig(result.getExternalAttributionConfig())
            .build();
    }

    private void updateServerTime(@Nullable Long validTimeDifference,
                                  @NonNull Long serverTime) {
        ServerTime.getInstance().updateServerTime(serverTime, validTimeDifference);
    }

    private void updateCurrentStartupDataAndNotifyListener(@NonNull StartupState state) {
        updateCurrentStartupData(state);
        notifyListener(state);
    }

    private synchronized void updateCurrentStartupData(@NonNull StartupState state) {
        mConfigHolder.updateStartupState(state);
        mStorage.save(state);
        GlobalServiceLocator.getInstance().getStartupStateHolder().onStartupStateChanged(state);

        YLogger.i("[StartupTask] Startup was updated for package: %s", mComponentId.getPackage());
    }

    private void notifyListener(@NonNull StartupState state) {
        mListener.onStartupChanged(mComponentId.getPackage(), state);
    }

    @Nullable
    private static String chooseValidClids(@Nullable String newClids, @Nullable String oldClids) {
        String clidsToSave = null;
        if (StartupUtils.isValidClids(newClids)) {
            clidsToSave = newClids;
        } else {
            if (StartupUtils.isValidClids(oldClids)) {
                clidsToSave = oldClids;
            }
        }
        YLogger.d(TAG + " valid clids: %s", clidsToSave);
        return clidsToSave;
    }

    @NonNull
    public StartupState getStartupState() {
        return mConfigHolder.getStartupState();
    }

    public void onRequestError(@NonNull StartupError cause) {
        removeCurrentStartupTask();
        mListener.onStartupError(getComponentId().getPackage(), cause, getStartupState());
    }

    public synchronized void updateConfiguration(@NonNull StartupRequestConfig.Arguments arguments) {
        YLogger.d(TAG + " update configuration for %s. New configuration %s", mComponentId.toString(), arguments);
        mConfigHolder.updateArguments(arguments);
        findHosts(mConfigHolder.get());
    }

    private void findHosts(StartupRequestConfig config) {
        if (config.hasNewCustomHosts()) {
            boolean changed = false;
            StartupState.Builder builder = null;
            List<String> customHostUrls = config.getNewCustomHosts();
            if (Utils.isNullOrEmpty(customHostUrls)) {
                if (Utils.isNullOrEmpty(config.getStartupHostsFromClient()) == false) {
                    changed = true;
                    builder = getStartupState().buildUpon().withHostUrlsFromClient(null);
                }
            }
            if (Utils.isNullOrEmpty(customHostUrls) == false) {
                if (Utils.areEqual(customHostUrls, config.getStartupHostsFromClient()) == false) {
                    changed = true;
                    builder = getStartupState().buildUpon().withHostUrlsFromClient(customHostUrls);
                }
            }
            if (changed) {
                updateCurrentStartupDataAndNotifyListener(builder.build());
            }
        }
    }
}
