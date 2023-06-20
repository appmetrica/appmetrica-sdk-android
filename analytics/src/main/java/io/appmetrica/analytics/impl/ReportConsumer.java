package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable;
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash;
import io.appmetrica.analytics.impl.crash.jvm.JvmCrashReader;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashHandlerDescription;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashSource;
import io.appmetrica.analytics.impl.crash.ndk.NativeDumpHandler;
import io.appmetrica.analytics.impl.crash.ndk.NativeDumpHandlerWrapper;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrash;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrashReporter;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import java.io.File;

public class ReportConsumer {

    private static final String TAG = "[ReportConsumer]";

    @NonNull
    private final Context mContext;
    @NonNull
    private final ICommonExecutor mTasksExecutor;
    @NonNull
    private final ClientRepository mClientRepository;
    @NonNull
    private final FileProvider fileProvider;

    public ReportConsumer(@NonNull Context context,
                          @NonNull ClientRepository clientRepository) {
        this(
                context,
                clientRepository,
                GlobalServiceLocator.getInstance().getServiceExecutorProvider().getReportRunnableExecutor(),
                new FileProvider()
        );
    }

    @VisibleForTesting
    ReportConsumer(@NonNull Context context,
                   @NonNull ClientRepository clientRepository,
                   @NonNull ICommonExecutor tasksExecutor,
                   @NonNull FileProvider fileProvider) {
        mContext = context;
        mTasksExecutor = tasksExecutor;
        mClientRepository = clientRepository;
        this.fileProvider = fileProvider;
    }

    public void consumeReport(final CounterReport reportData, final Bundle extras) {
        YLogger.info(
            TAG,
            "reportData: type = %s; customType = %s; name = %s",
            reportData.getType(), reportData.getCustomType(), reportData.getName()
        );
        if (reportData.isUndefinedType() == false) {
            final Runnable reportTask = new ReportRunnable(mContext, reportData, extras, mClientRepository);
            mTasksExecutor.execute(reportTask);
        } else {
            YLogger.w("%sUndefined report type: %d", TAG, reportData.getType());
        }
    }

    public void consumeCrashFromFile(@NonNull File crashFile) {
        JvmCrashReader reader = new JvmCrashReader();
        mTasksExecutor.execute(
                new ReadAndReportRunnable<JvmCrash>(crashFile, reader, reader,
                new Consumer<JvmCrash>() {
                    @Override
                    public void consume(JvmCrash input) {
                        consumeCrash(
                                new ClientDescription(
                                        input.getApiKey(),
                                        input.getPackageName(),
                                        input.getPid(),
                                        input.getPsid(),
                                        input.getReporterType()
                                ),
                                EventsManager.unhandledExceptionFromFileReportEntry(
                                        input.getName(),
                                        input.getCrashValue(),
                                        input.getBytesTruncated(),
                                        input.getTrimmedFields(),
                                        input.getEnvironment(),
                                        LoggerStorage.getOrCreatePublicLogger(input.getApiKey())
                                ),
                                new CommonArguments(
                                        new StartupRequestConfig.Arguments(),
                                        new CommonArguments.ReporterArguments(),
                                        null
                                )
                        );
                    }
                }
            )
        );
    }

    public void consumeCrash(@NonNull ClientDescription clientDescription,
                             @NonNull CounterReport reportData,
                             @NonNull CommonArguments arguments) {
        ClientUnit unit = mClientRepository.getOrCreateClient(clientDescription, arguments);
        unit.handle(reportData, arguments);
        mClientRepository.remove(
                clientDescription.getPackageName(),
                clientDescription.getProcessID(),
                clientDescription.getProcessSessionID()
        );
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void consumeCrashpadCrash(
            @NonNull final CrashpadCrash crashpadCrash,
            @NonNull final Function<String, CounterReport> reportCreator
    ) {
        mTasksExecutor.execute(
                new ReadAndReportRunnable<String>(
                        fileProvider.getFileByNonNullPath(crashpadCrash.crashReport.dumpFile),
                        new NativeDumpHandlerWrapper(new NativeCrashHandlerDescription(
                                NativeCrashSource.CRASHPAD, crashpadCrash.runtimeConfig.handlerVersion
                        ), new NativeDumpHandler()),
                        new CrashpadCrashReporter.CrashCompletedConsumer(crashpadCrash.crashReport.uuid),
                        new CrashpadConsumer(crashpadCrash.clientDescription, reportCreator)
                )
        );
    }

    @VisibleForTesting
    protected class CrashpadConsumer implements Consumer<String> {

        private final ClientDescription clientDescription;
        private final Function<String, CounterReport> reportCreator;

        public CrashpadConsumer(ClientDescription clientDescription, Function<String, CounterReport> reportCreator) {
            this.clientDescription = clientDescription;
            this.reportCreator = reportCreator;
        }

        @Override
        public void consume(@NonNull String input) {
            consumeCrash(
                    clientDescription,
                    reportCreator.apply(input),
                    new CommonArguments(
                            new StartupRequestConfig.Arguments(),
                            new CommonArguments.ReporterArguments(),
                            null
                    )
            );
        }
    }
}
