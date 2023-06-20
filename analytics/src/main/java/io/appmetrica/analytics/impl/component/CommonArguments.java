package io.appmetrica.analytics.impl.component;

import android.location.Location;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.networktasks.internal.ArgumentsMerger;
import java.util.Map;

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

    public static class ReporterArguments implements ArgumentsMerger<ReporterArguments, ReporterArguments>  {

        @Nullable
        public final String deviceType;
        @Nullable
        public final String appVersion;
        @Nullable
        public final String appBuildNumber;
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
        public final Boolean statisticsSending;
        @Nullable
        public final Map<String, String> clidsFromClient;
        @Nullable
        public final Integer maxReportsInDbCount;
        @Nullable
        public final Boolean nativeCrashesEnabled;
        @Nullable
        public final Boolean revenueAutoTrackingEnabled;

        ReporterArguments(@Nullable String deviceType,
                          @Nullable String appVersion,
                          @Nullable String appBuildNumber,
                          @Nullable String apiKey,
                          @Nullable Boolean locationTracking,
                          @Nullable Location manualLocation,
                          @Nullable Boolean firstActivationAsUpdate,
                          @Nullable Integer sessionTimeout,
                          @Nullable Integer maxReportsCount,
                          @Nullable Integer dispatchPeriod,
                          @Nullable Boolean logEnabled,
                          @Nullable Boolean statisticsSending,
                          @Nullable Map<String, String> clidsFromClient,
                          @Nullable Integer maxReportsInDbCount,
                          @Nullable Boolean nativeCrashesEnabled,
                          @Nullable Boolean revenueAutoTrackingEnabled) {
            this.deviceType = deviceType;
            this.appVersion = appVersion;
            this.appBuildNumber = appBuildNumber;
            this.apiKey = apiKey;
            this.locationTracking = locationTracking;
            this.manualLocation = manualLocation;
            this.firstActivationAsUpdate = firstActivationAsUpdate;
            this.sessionTimeout = sessionTimeout;
            this.maxReportsCount = maxReportsCount;
            this.dispatchPeriod = dispatchPeriod;
            this.logEnabled = logEnabled;
            this.statisticsSending = statisticsSending;
            this.clidsFromClient = clidsFromClient;
            this.maxReportsInDbCount = maxReportsInDbCount;
            this.nativeCrashesEnabled = nativeCrashesEnabled;
            this.revenueAutoTrackingEnabled = revenueAutoTrackingEnabled;
        }

        public ReporterArguments(@NonNull CounterConfiguration reporterConfiguration,
                                 @Nullable Map<String, String> clidsFromClient) {
            this(
                    reporterConfiguration.getDeviceType(),
                    reporterConfiguration.getAppVersion(),
                    reporterConfiguration.getAppBuildNumber(),
                    reporterConfiguration.getApiKey(),
                    reporterConfiguration.isLocationTrackingEnabled(),
                    reporterConfiguration.getManualLocation(),
                    reporterConfiguration.isFirstActivationAsUpdate(),
                    reporterConfiguration.getSessionTimeout(),
                    reporterConfiguration.getMaxReportsCount(),
                    reporterConfiguration.getDispatchPeriod(),
                    reporterConfiguration.isLogEnabled(),
                    reporterConfiguration.getStatisticsSending(),
                    clidsFromClient,
                    reporterConfiguration.getMaxReportsInDbCount(),
                    reporterConfiguration.getReportNativeCrashesEnabled(),
                    reporterConfiguration.isRevenueAutoTrackingEnabled()
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
                    null,
                    null
            );
        }

        @NonNull
        @Override
        public ReporterArguments mergeFrom(@NonNull ReporterArguments other) {
            return new ReporterArguments(
                    WrapUtils.getOrDefaultNullable(deviceType, other.deviceType),
                    WrapUtils.getOrDefaultNullable(appVersion, other.appVersion),
                    WrapUtils.getOrDefaultNullable(appBuildNumber, other.appBuildNumber),
                    WrapUtils.getOrDefaultNullable(apiKey, other.apiKey),
                    WrapUtils.getOrDefaultNullable(locationTracking, other.locationTracking),
                    WrapUtils.getOrDefaultNullable(manualLocation, other.manualLocation),
                    WrapUtils.getOrDefaultNullable(firstActivationAsUpdate, other.firstActivationAsUpdate),
                    WrapUtils.getOrDefaultNullable(sessionTimeout, other.sessionTimeout),
                    WrapUtils.getOrDefaultNullable(maxReportsCount, other.maxReportsCount),
                    WrapUtils.getOrDefaultNullable(dispatchPeriod, other.dispatchPeriod),
                    WrapUtils.getOrDefaultNullable(logEnabled, other.logEnabled),
                    WrapUtils.getOrDefaultNullable(statisticsSending, other.statisticsSending),
                    WrapUtils.getOrDefaultNullable(clidsFromClient, other.clidsFromClient),
                    WrapUtils.getOrDefaultNullable(maxReportsInDbCount, other.maxReportsInDbCount),
                    WrapUtils.getOrDefaultNullable(nativeCrashesEnabled, other.nativeCrashesEnabled),
                    WrapUtils.getOrDefaultNullable(revenueAutoTrackingEnabled, other.revenueAutoTrackingEnabled)
            );
        }

        @Override
        public boolean compareWithOtherArguments(@NonNull ReporterArguments other) {
            return equals(other);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ReporterArguments that = (ReporterArguments) o;

            if (deviceType != null ? !deviceType.equals(that.deviceType) : that.deviceType != null)
                return false;
            if (appVersion != null ? !appVersion.equals(that.appVersion) : that.appVersion != null)
                return false;
            if (appBuildNumber != null ? !appBuildNumber.equals(that.appBuildNumber) : that.appBuildNumber != null)
                return false;
            if (apiKey != null ? !apiKey.equals(that.apiKey) : that.apiKey != null) return false;
            if (locationTracking != null ? !locationTracking.equals(that.locationTracking) :
                    that.locationTracking != null)
                return false;
            if (manualLocation != null ? !manualLocation.equals(that.manualLocation) : that.manualLocation != null)
                return false;
            if (firstActivationAsUpdate != null ? !firstActivationAsUpdate.equals(that.firstActivationAsUpdate) :
                    that.firstActivationAsUpdate != null)
                return false;
            if (sessionTimeout != null ? !sessionTimeout.equals(that.sessionTimeout) : that.sessionTimeout != null)
                return false;
            if (maxReportsCount != null ? !maxReportsCount.equals(that.maxReportsCount) : that.maxReportsCount != null)
                return false;
            if (dispatchPeriod != null ? !dispatchPeriod.equals(that.dispatchPeriod) : that.dispatchPeriod != null)
                return false;
            if (logEnabled != null ? !logEnabled.equals(that.logEnabled) : that.logEnabled != null)
                return false;
            if (statisticsSending != null ? !statisticsSending.equals(that.statisticsSending) :
                    that.statisticsSending != null)
                return false;
            if (clidsFromClient != null ? !clidsFromClient.equals(that.clidsFromClient) : that.clidsFromClient != null)
                return false;
            if (maxReportsInDbCount != null ? !maxReportsInDbCount.equals(that.maxReportsInDbCount) :
                    that.maxReportsInDbCount != null)
                return false;
            if (nativeCrashesEnabled != null ? !nativeCrashesEnabled.equals(that.nativeCrashesEnabled) :
                    that.nativeCrashesEnabled != null)
                return false;
            return revenueAutoTrackingEnabled != null ?
                    revenueAutoTrackingEnabled.equals(that.revenueAutoTrackingEnabled) :
                    that.revenueAutoTrackingEnabled == null;
        }

        @Override
        public int hashCode() {
            int result = deviceType != null ? deviceType.hashCode() : 0;
            result = 31 * result + (appVersion != null ? appVersion.hashCode() : 0);
            result = 31 * result + (appBuildNumber != null ? appBuildNumber.hashCode() : 0);
            result = 31 * result + (apiKey != null ? apiKey.hashCode() : 0);
            result = 31 * result + (locationTracking != null ? locationTracking.hashCode() : 0);
            result = 31 * result + (manualLocation != null ? manualLocation.hashCode() : 0);
            result = 31 * result + (firstActivationAsUpdate != null ? firstActivationAsUpdate.hashCode() : 0);
            result = 31 * result + (sessionTimeout != null ? sessionTimeout.hashCode() : 0);
            result = 31 * result + (maxReportsCount != null ? maxReportsCount.hashCode() : 0);
            result = 31 * result + (dispatchPeriod != null ? dispatchPeriod.hashCode() : 0);
            result = 31 * result + (logEnabled != null ? logEnabled.hashCode() : 0);
            result = 31 * result + (statisticsSending != null ? statisticsSending.hashCode() : 0);
            result = 31 * result + (clidsFromClient != null ? clidsFromClient.hashCode() : 0);
            result = 31 * result + (maxReportsInDbCount != null ? maxReportsInDbCount.hashCode() : 0);
            result = 31 * result + (nativeCrashesEnabled != null ? nativeCrashesEnabled.hashCode() : 0);
            result = 31 * result + (revenueAutoTrackingEnabled != null ? revenueAutoTrackingEnabled.hashCode() : 0);
            return result;
        }
    }
}
