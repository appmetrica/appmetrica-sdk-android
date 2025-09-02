package io.appmetrica.analytics.impl.component;

import android.location.Location;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.networktasks.internal.ArgumentsMerger;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CommonArguments {

    public final StartupRequestConfig.Arguments startupArguments;
    public final ReporterArguments componentArguments;
    @Nullable
    public final ResultReceiver dataResultReceiver;

    public CommonArguments(@NonNull ClientConfiguration clientConfiguration) {
        this(
            new StartupRequestConfig.Arguments(clientConfiguration),
            new ReporterArguments(
                clientConfiguration.getReporterConfiguration(),
                clientConfiguration.getProcessConfiguration().getClientClids()
            ),
            clientConfiguration.getProcessConfiguration().getDataResultReceiver()
        );
    }

    public CommonArguments(@NonNull StartupRequestConfig.Arguments startupArguments,
                           @NonNull ReporterArguments componentArguments,
                           @Nullable ResultReceiver dataReceiver) {
        this.startupArguments = startupArguments;
        this.componentArguments = componentArguments;
        this.dataResultReceiver = dataReceiver;
    }

    public static class ReporterArguments implements ArgumentsMerger<ReporterArguments, ReporterArguments> {

        @Nullable
        public final String apiKey;
        @Nullable
        public final Boolean locationTracking;
        @Nullable
        public final Location manualLocation;
        @Nullable
        public final Boolean firstActivationAsUpdate;
        @Nullable
        public final Integer sessionTimeout;
        @Nullable
        public final Integer maxReportsCount;
        @Nullable
        public final Integer dispatchPeriod;
        @Nullable
        public final Boolean logEnabled;
        @Nullable
        public final Boolean dataSendingEnabled;
        @Nullable
        public final Map<String, String> clidsFromClient;
        @Nullable
        public final Integer maxReportsInDbCount;
        @Nullable
        public final Boolean nativeCrashesEnabled;
        @Nullable
        public final Boolean revenueAutoTrackingEnabled;
        @Nullable
        public final Boolean advIdentifiersTrackingEnabled;
        @NonNull
        public final Set<String> autoCollectedDataSubscribers;

        ReporterArguments(@Nullable String apiKey,
                          @Nullable Boolean locationTracking,
                          @Nullable Location manualLocation,
                          @Nullable Boolean firstActivationAsUpdate,
                          @Nullable Integer sessionTimeout,
                          @Nullable Integer maxReportsCount,
                          @Nullable Integer dispatchPeriod,
                          @Nullable Boolean logEnabled,
                          @Nullable Boolean dataSendingEnabled,
                          @Nullable Map<String, String> clidsFromClient,
                          @Nullable Integer maxReportsInDbCount,
                          @Nullable Boolean nativeCrashesEnabled,
                          @Nullable Boolean revenueAutoTrackingEnabled,
                          @Nullable Boolean advIdentifiersTrackingEnabled,
                          @NonNull Set<String> autoCollectedDataSubscribers) {
            this.apiKey = apiKey;
            this.locationTracking = locationTracking;
            this.manualLocation = manualLocation;
            this.firstActivationAsUpdate = firstActivationAsUpdate;
            this.sessionTimeout = sessionTimeout;
            this.maxReportsCount = maxReportsCount;
            this.dispatchPeriod = dispatchPeriod;
            this.logEnabled = logEnabled;
            this.dataSendingEnabled = dataSendingEnabled;
            this.clidsFromClient = clidsFromClient;
            this.maxReportsInDbCount = maxReportsInDbCount;
            this.nativeCrashesEnabled = nativeCrashesEnabled;
            this.revenueAutoTrackingEnabled = revenueAutoTrackingEnabled;
            this.advIdentifiersTrackingEnabled = advIdentifiersTrackingEnabled;
            this.autoCollectedDataSubscribers = autoCollectedDataSubscribers;
        }

        public ReporterArguments(@NonNull CounterConfiguration reporterConfiguration,
                                 @Nullable Map<String, String> clidsFromClient) {
            this(
                reporterConfiguration.getApiKey(),
                reporterConfiguration.isLocationTrackingEnabled(),
                reporterConfiguration.getManualLocation(),
                reporterConfiguration.isFirstActivationAsUpdate(),
                reporterConfiguration.getSessionTimeout(),
                reporterConfiguration.getMaxReportsCount(),
                reporterConfiguration.getDispatchPeriod(),
                reporterConfiguration.isLogEnabled(),
                reporterConfiguration.getDataSendingEnabled(),
                clidsFromClient,
                reporterConfiguration.getMaxReportsInDbCount(),
                reporterConfiguration.getReportNativeCrashesEnabled(),
                reporterConfiguration.isRevenueAutoTrackingEnabled(),
                reporterConfiguration.isAdvIdentifiersTrackingEnabled(),
                new HashSet<>(reporterConfiguration.getAutoCollectedDataSubscribers())
            );
        }

        public ReporterArguments() {
            this(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new HashSet<>()
            );
        }

        @NonNull
        @Override
        public ReporterArguments mergeFrom(@NonNull ReporterArguments other) {
            return new ReporterArguments(
                WrapUtils.getOrDefaultNullable(apiKey, other.apiKey),
                WrapUtils.getOrDefaultNullable(locationTracking, other.locationTracking),
                WrapUtils.getOrDefaultNullable(manualLocation, other.manualLocation),
                WrapUtils.getOrDefaultNullable(firstActivationAsUpdate, other.firstActivationAsUpdate),
                WrapUtils.getOrDefaultNullable(sessionTimeout, other.sessionTimeout),
                WrapUtils.getOrDefaultNullable(maxReportsCount, other.maxReportsCount),
                WrapUtils.getOrDefaultNullable(dispatchPeriod, other.dispatchPeriod),
                WrapUtils.getOrDefaultNullable(logEnabled, other.logEnabled),
                WrapUtils.getOrDefaultNullable(dataSendingEnabled, other.dataSendingEnabled),
                WrapUtils.getOrDefaultNullable(clidsFromClient, other.clidsFromClient),
                WrapUtils.getOrDefaultNullable(maxReportsInDbCount, other.maxReportsInDbCount),
                WrapUtils.getOrDefaultNullable(nativeCrashesEnabled, other.nativeCrashesEnabled),
                WrapUtils.getOrDefaultNullable(revenueAutoTrackingEnabled, other.revenueAutoTrackingEnabled),
                WrapUtils.getOrDefaultNullable(advIdentifiersTrackingEnabled, other.advIdentifiersTrackingEnabled),
                CollectionUtils.merge(autoCollectedDataSubscribers, other.autoCollectedDataSubscribers)
            );
        }

        @Override
        public boolean compareWithOtherArguments(@NonNull ReporterArguments other) {
            return equals(other);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ReporterArguments that = (ReporterArguments) o;
            return Objects.equals(apiKey, that.apiKey) &&
                Objects.equals(locationTracking, that.locationTracking) &&
                Objects.equals(manualLocation, that.manualLocation) &&
                Objects.equals(firstActivationAsUpdate, that.firstActivationAsUpdate) &&
                Objects.equals(sessionTimeout, that.sessionTimeout) &&
                Objects.equals(maxReportsCount, that.maxReportsCount) &&
                Objects.equals(dispatchPeriod, that.dispatchPeriod) &&
                Objects.equals(logEnabled, that.logEnabled) &&
                Objects.equals(dataSendingEnabled, that.dataSendingEnabled) &&
                Objects.equals(clidsFromClient, that.clidsFromClient) &&
                Objects.equals(maxReportsInDbCount, that.maxReportsInDbCount) &&
                Objects.equals(nativeCrashesEnabled, that.nativeCrashesEnabled) &&
                Objects.equals(revenueAutoTrackingEnabled, that.revenueAutoTrackingEnabled) &&
                Objects.equals(advIdentifiersTrackingEnabled, that.advIdentifiersTrackingEnabled) &&
                Objects.equals(autoCollectedDataSubscribers, that.autoCollectedDataSubscribers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                apiKey,
                locationTracking,
                manualLocation,
                firstActivationAsUpdate,
                sessionTimeout,
                maxReportsCount,
                dispatchPeriod,
                logEnabled,
                dataSendingEnabled,
                clidsFromClient,
                maxReportsInDbCount,
                nativeCrashesEnabled,
                revenueAutoTrackingEnabled,
                advIdentifiersTrackingEnabled,
                autoCollectedDataSubscribers
            );
        }

        @Override
        public String toString() {
            return "ReporterArguments{" +
                "apiKey='" + apiKey + '\'' +
                ", locationTracking=" + locationTracking +
                ", manualLocation=" + manualLocation +
                ", firstActivationAsUpdate=" + firstActivationAsUpdate +
                ", sessionTimeout=" + sessionTimeout +
                ", maxReportsCount=" + maxReportsCount +
                ", dispatchPeriod=" + dispatchPeriod +
                ", logEnabled=" + logEnabled +
                ", dataSendingEnabled=" + dataSendingEnabled +
                ", clidsFromClient=" + clidsFromClient +
                ", maxReportsInDbCount=" + maxReportsInDbCount +
                ", nativeCrashesEnabled=" + nativeCrashesEnabled +
                ", revenueAutoTrackingEnabled=" + revenueAutoTrackingEnabled +
                ", advIdentifiersTrackingEnabled=" + advIdentifiersTrackingEnabled +
                ", autoCollectedDataSubscribers=" + autoCollectedDataSubscribers +
                '}';
        }
    }
}
