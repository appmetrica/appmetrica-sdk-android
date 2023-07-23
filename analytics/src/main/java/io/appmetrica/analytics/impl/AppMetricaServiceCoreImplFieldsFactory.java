package io.appmetrica.analytics.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.crash.jvm.CrashDirectoryWatcher;
import java.io.File;

public class AppMetricaServiceCoreImplFieldsFactory {

    public CrashDirectoryWatcher createCrashDirectoryWatcher(
            @NonNull File crashDirectory, @NonNull Consumer<File> newCrashListener
    ) {
        return new CrashDirectoryWatcher(crashDirectory, newCrashListener);
    }

    public ReportConsumer createReportConsumer(@NonNull Context context, @NonNull ClientRepository clientRepository) {
        return new ReportConsumer(context, clientRepository);
    }
}
