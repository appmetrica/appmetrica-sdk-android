package io.appmetrica.analytics.impl.component;

import android.location.Location;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.networktasks.internal.ArgumentsMerger;
import java.util.Map;
import java.util.Objects;

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
                          @Nullable Boolean revenueAutoTrackingEnabled) {
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
                    null
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

            final ReporterArguments that = (ReporterArguments) o;

            if (!Objects.equals(apiKey, that.apiKey)) return false;
            if (!Objects.equals(locationTracking, that.locationTracking))
                return false;
            if (!Objects.equals(manualLocation, that.manualLocation))
                return false;
            if (!Objects.equals(firstActivationAsUpdate, that.firstActivationAsUpdate))
                return false;
            if (!Objects.equals(sessionTimeout, that.sessionTimeout))
                return false;
            if (!Objects.equals(maxReportsCount, that.maxReportsCount))
                return false;
            if (!Objects.equals(dispatchPeriod, that.dispatchPeriod))
                return false;
            if (!Objects.equals(logEnabled, that.logEnabled)) return false;
            if (!Objects.equals(dataSendingEnabled, that.dataSendingEnabled))
                return false;
            if (!Objects.equals(clidsFromClient, that.clidsFromClient))
                return false;
            if (!Objects.equals(maxReportsInDbCount, that.maxReportsInDbCount))
                return false;
            if (!Objects.equals(nativeCrashesEnabled, that.nativeCrashesEnabled))
                return false;
            return Objects.equals(revenueAutoTrackingEnabled, that.revenueAutoTrackingEnabled);
        }

        @Override
        public int hashCode() {
            int result = apiKey != null ? apiKey.hashCode() : 0;
            result = 31 * result + (locationTracking != null ? locationTracking.hashCode() : 0);
            result = 31 * result + (manualLocation != null ? manualLocation.hashCode() : 0);
            result = 31 * result + (firstActivationAsUpdate != null ? firstActivationAsUpdate.hashCode() : 0);
            result = 31 * result + (sessionTimeout != null ? sessionTimeout.hashCode() : 0);
            result = 31 * result + (maxReportsCount != null ? maxReportsCount.hashCode() : 0);
            result = 31 * result + (dispatchPeriod != null ? dispatchPeriod.hashCode() : 0);
            result = 31 * result + (logEnabled != null ? logEnabled.hashCode() : 0);
            result = 31 * result + (dataSendingEnabled != null ? dataSendingEnabled.hashCode() : 0);
            result = 31 * result + (clidsFromClient != null ? clidsFromClient.hashCode() : 0);
            result = 31 * result + (maxReportsInDbCount != null ? maxReportsInDbCount.hashCode() : 0);
            result = 31 * result + (nativeCrashesEnabled != null ? nativeCrashesEnabled.hashCode() : 0);
            result = 31 * result + (revenueAutoTrackingEnabled != null ? revenueAutoTrackingEnabled.hashCode() : 0);
            return result;
        }
    }
}
