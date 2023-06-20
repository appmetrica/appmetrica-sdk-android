package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.crash.jvm.CrashDirectoryWatcher;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrashReader;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrashReporter;
import java.io.File;

public class AppMetricaServiceCoreImplFieldsFactory {

    public CrashDirectoryWatcher createCrashDirectoryWatcher(
            @NonNull File crashDirectory, @NonNull Consumer<File> newCrashListener
    ) {
        return new CrashDirectoryWatcher(crashDirectory, newCrashListener);
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public CrashpadCrashReader createCrashpadCrashReader(@NonNull ReportConsumer reportConsumer) {
        return new CrashpadCrashReader(new CrashpadCrashReporter(reportConsumer));
    }

    public ReportConsumer createReportConsumer(@NonNull Context context, @NonNull ClientRepository clientRepository) {
        return new ReportConsumer(context, clientRepository);
    }
}
