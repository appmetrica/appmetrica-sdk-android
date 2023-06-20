package io.appmetrica.analytics.impl.db.state.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.StartupStateModel;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import java.util.Arrays;

public class StartupStateConverter implements ProtobufConverter<StartupStateModel, StartupStateProtobuf.StartupState> {

    private PermissionsCollectingConfigConverter mPermissionsCollectingConfigConverter =
            new PermissionsCollectingConfigConverter();
    private FlagsConverter mFlagsConverter = new FlagsConverter();
    private StatSendingConverter mStatSendingConverter = new StatSendingConverter();
    private AutoInappCollectingConfigConverter autoInappCollectingConfigConverter =
            new AutoInappCollectingConfigConverter();
    private CacheControlConverter mCacheControlConverter = new CacheControlConverter();
    private AttributionConfigConverter attributionConfigConverter =
            new AttributionConfigConverter();
    private CustomSdkHostsConverter customSdkHostsConverter = new CustomSdkHostsConverter();
    private StartupUpdateConfigConverter startupUpdateConfigConverter = new StartupUpdateConfigConverter();
    private ModulesRemoteConfigsConverter modulesRemoteConfigsConverter = new ModulesRemoteConfigsConverter();

    @SuppressWarnings("checkstyle:methodLength")
    @NonNull
    @Override
    public StartupStateProtobuf.StartupState fromModel(@NonNull StartupStateModel value) {
        StartupStateProtobuf.StartupState state = new StartupStateProtobuf.StartupState();
        state.obtainServerTime = value.obtainServerTime;
        state.firstStartupServerTime = value.firstStartupServerTime;
        if (value.uuid != null) { state.uuid = value.uuid; }
        if (value.deviceID != null) { state.deviceID = value.deviceID; }
        if (value.deviceIDHash != null) { state.deviceIDHash = value.deviceIDHash; }
        if (value.hostUrlsFromStartup != null) {
            state.hostUrlsFromStartup = value.hostUrlsFromStartup.toArray(new String[value.hostUrlsFromStartup.size()]);
        }
        if (value.hostUrlsFromClient != null) {
            state.hostUrlsFromClient = value.hostUrlsFromClient.toArray(new String[value.hostUrlsFromClient.size()]);
        }
        if (value.reportUrls != null) {
            state.reportUrls = value.reportUrls.toArray(new String[value.reportUrls.size()]);
        }
        if (value.diagnosticUrls != null) {
            state.diagnosticUrls = value.diagnosticUrls.toArray(new String[value.diagnosticUrls.size()]);
        }
        if (value.customSdkHosts != null) {
            state.customSdkHosts = customSdkHostsConverter.fromModel(value.customSdkHosts);
        }
        if (value.permissionsCollectingConfig != null) {
            state.permissionsCollectingConfig = mPermissionsCollectingConfigConverter.fromModel(
                    value.permissionsCollectingConfig
            );
        }
        if (value.encodedClidsFromResponse != null) { state.encodedClidsFromResponse = value.encodedClidsFromResponse; }
        if (value.getAdUrl != null) { state.getAdUrl = value.getAdUrl; }
        if (value.reportAdUrl != null) { state.reportAdUrl = value.reportAdUrl; }
        if (value.certificateUrl != null) { state.certificateUrl = value.certificateUrl; }
        state.flags = mFlagsConverter.fromModel(value.collectingFlags);
        if (value.lastClientClidsForStartupRequest != null) {
            state.lastClientClidsForStartupRequest = value.lastClientClidsForStartupRequest;
        }
        if (value.lastChosenForRequestClids != null) {
            state.lastChosenForRequestClids = value.lastChosenForRequestClids;
        }
        state.startupDidNotOverrideClids = value.startupDidNotOverrideClids;
        state.obtainTime = value.obtainTime;
        state.hadFirstStartup = value.hadFirstStartup;
        state.maxRetryIntervalSeconds = value.retryPolicyConfig.maxIntervalSeconds;
        state.retryExponentialMultiplier = value.retryPolicyConfig.exponentialMultiplier;
        if (value.countryInit != null) { state.countryInit = value.countryInit; }
        if (value.statSending != null) { state.statSending = mStatSendingConverter.fromModel(value.statSending); }
        state.outdated = value.outdated;
        if (value.autoInappCollectingConfig != null) {
            state.autoInappCollectingConfig =
                    autoInappCollectingConfigConverter.fromModel(value.autoInappCollectingConfig);
        }
        if (value.cacheControl != null) {
            state.cacheControl = mCacheControlConverter.fromModel(value.cacheControl);
        }
        if (value.attributionConfig != null) {
            state.attribution = attributionConfigConverter.fromModel(value.attributionConfig);
        }
        state.startupUpdateConfig = startupUpdateConfigConverter.fromModel(value.startupUpdateConfig);
        state.modulesRemoteConfigs = modulesRemoteConfigsConverter.fromModel(value.modulesRemoteConfigs);

        return state;
    }

    @SuppressWarnings("checkstyle:methodLength")
    @NonNull
    @Override
    public StartupStateModel toModel(@NonNull StartupStateProtobuf.StartupState nano) {
        StartupStateModel.StartupStateBuilder builder =
                new StartupStateModel.StartupStateBuilder(mFlagsConverter.toModel(nano.flags))
                .withUuid(nano.uuid)
                .withDeviceID(nano.deviceID)
                .withDeviceIDHash(nano.deviceIDHash)
                .withEncodedClidsFromResponse(nano.encodedClidsFromResponse)
                .withGetAdUrl(nano.getAdUrl)
                .withReportUrls(Arrays.asList(nano.reportUrls))
                .withHostUrlsFromClient(Arrays.asList(nano.hostUrlsFromClient))
                .withHostUrlsFromStartup(Arrays.asList(nano.hostUrlsFromStartup))
                .withReportAdUrl(nano.reportAdUrl)
                .withCertificateUrl(nano.certificateUrl)
                .withDiagnosticUrls(Arrays.asList(nano.diagnosticUrls))
                .withLastClientClidsForStartupRequest(nano.lastClientClidsForStartupRequest)
                .withLastChosenForRequestClids(nano.lastChosenForRequestClids)
                .withStartupDidNotOverrideClids(nano.startupDidNotOverrideClids)
                .withObtainTime(nano.obtainTime)
                .withHadFirstStartup(nano.hadFirstStartup)
                .withObtainServerTime(nano.obtainServerTime)
                .withFirstStartupServerTime(nano.firstStartupServerTime)
                .withCountryInit(nano.countryInit)
                .withOutdated(nano.outdated)
                .withRetryPolicyConfig(
                        new RetryPolicyConfig(nano.maxRetryIntervalSeconds, nano.retryExponentialMultiplier)
                )
                .withCustomSdkHosts(customSdkHostsConverter.toModel(nano.customSdkHosts));
        if (nano.permissionsCollectingConfig != null) {
            builder.withPermissionsCollectingConfig(
                    mPermissionsCollectingConfigConverter.toModel(nano.permissionsCollectingConfig)
            );
        }
        if (nano.statSending != null) {
            builder.withStatSending(mStatSendingConverter.toModel(nano.statSending));
        }
        if (nano.autoInappCollectingConfig != null) {
            builder.withAutoInappCollectingConfig(
                    autoInappCollectingConfigConverter.toModel(nano.autoInappCollectingConfig)
            );
        }
        if (nano.cacheControl != null) {
            builder.withCacheControl(
                    mCacheControlConverter.toModel(nano.cacheControl)
            );
        }
        if (nano.attribution != null) {
            builder.withAttributionConfig(attributionConfigConverter.toModel(nano.attribution));
        }
        if (nano.startupUpdateConfig != null) {
            builder.withStartupUpdateConfig(startupUpdateConfigConverter.toModel(nano.startupUpdateConfig));
        }
        builder.withModulesRemoteConfigs(modulesRemoteConfigsConverter.toModel(nano.modulesRemoteConfigs));

        return builder.build();
    }
}
