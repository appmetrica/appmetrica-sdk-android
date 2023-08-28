package io.appmetrica.analytics.impl.client;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.internal.CounterConfiguration;

public class ClientConfiguration {

    @NonNull private final ProcessConfiguration mProcessConfiguration;
    @NonNull private final CounterConfiguration mCounterConfiguration;

    @Nullable
    public static ClientConfiguration fromBundle(@NonNull Context context, @NonNull Bundle bundle) {
        ProcessConfiguration processConfiguration = ProcessConfiguration.fromBundle(bundle);
        CounterConfiguration counterConfiguration = CounterConfiguration.fromBundle(bundle);
        if (isValidCounterConfiguration(counterConfiguration) &&
            isValidProcessConfiguration(context, processConfiguration)) {
            return new ClientConfiguration(processConfiguration, counterConfiguration);
        }
        return null;
    }

    public ClientConfiguration(@NonNull ProcessConfiguration processConfiguration,
                               @NonNull CounterConfiguration configuration) {
        mProcessConfiguration = processConfiguration;
        mCounterConfiguration = configuration;
    }

    @NonNull
    public ProcessConfiguration getProcessConfiguration() {
        return mProcessConfiguration;
    }

    @NonNull
    public CounterConfiguration getReporterConfiguration() {
        return mCounterConfiguration;
    }

    @Override
    public String toString() {
        return "ClientConfiguration{" +
                "mProcessConfiguration=" + mProcessConfiguration +
                ", mCounterConfiguration=" + mCounterConfiguration +
                '}';
    }

    private static boolean isValidProcessConfiguration(@NonNull Context context,
                                                       @Nullable ProcessConfiguration processConfiguration) {
        return processConfiguration != null &&
            context.getPackageName().equals(processConfiguration.getPackageName()) &&
            processConfiguration.getSdkApiLevel() == AppMetrica.getLibraryApiLevel();
    }

    private static boolean isValidCounterConfiguration(@Nullable CounterConfiguration counterConfiguration) {
        return counterConfiguration != null;
    }
}
