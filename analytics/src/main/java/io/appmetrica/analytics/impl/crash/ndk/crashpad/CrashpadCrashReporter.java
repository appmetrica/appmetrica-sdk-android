package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.ReportConsumer;
import io.appmetrica.analytics.impl.ac.CrashpadServiceHelper;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashReporter;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import java.io.File;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class CrashpadCrashReporter implements NativeCrashReporter<CrashpadCrash> {

    @NonNull
    private final ReportConsumer reportConsumer;

    public CrashpadCrashReporter(@NonNull ReportConsumer reportConsumer) {
        this.reportConsumer = reportConsumer;
    }

    @Override
    public void reportCurrentSessionNativeCrash(
            @NonNull final CrashpadCrash crashpadCrash
    ) {
        reportConsumer.consumeCrashpadCrash(crashpadCrash,
                new Function<String, CounterReport>() {
                    @Override
                    public CounterReport apply(String input) {
                        CounterReport counterReport = EventsManager.currentSessionCrashpadCrashEntry(
                            input,
                            crashpadCrash.crashReport.uuid,
                            LoggerStorage.getOrCreatePublicLogger(crashpadCrash.clientDescription.getApiKey())
                        );
                        counterReport.setEventEnvironment(crashpadCrash.runtimeConfig.errorEnvironment);
                        return counterReport;
                    }
                }
        );
    }

    @Override
    public void reportPrevSessionNativeCrash(
            @NonNull final CrashpadCrash crashpadCrash
    ) {
        reportConsumer.consumeCrashpadCrash(crashpadCrash,
                new Function<String, CounterReport>() {
                    @Override
                    public CounterReport apply(String input) {
                        CounterReport counterReport = EventsManager.prevSessionCrashpadCrashEntry(
                            input,
                            crashpadCrash.crashReport.uuid,
                            LoggerStorage.getOrCreatePublicLogger(crashpadCrash.clientDescription.getApiKey())
                        );
                        counterReport.setEventEnvironment(crashpadCrash.runtimeConfig.errorEnvironment);
                        return counterReport;
                    }
                }
        );
    }

    public static class CrashCompletedConsumer implements Consumer<File> {

        @NonNull
        private final String uuid;

        public CrashCompletedConsumer(@NonNull String uuid) {
            this.uuid = uuid;
        }

        @Override
        public void consume(File input) {
            CrashpadServiceHelper.markCrashCompletedAndDeleteAllCompleted(uuid);
        }
    }

}
