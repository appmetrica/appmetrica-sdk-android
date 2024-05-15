package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.impl.component.clients.ClientRepository;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable;
import io.appmetrica.analytics.impl.crash.client.converter.NativeCrashConverter;
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash;
import io.appmetrica.analytics.impl.crash.jvm.JvmCrashReader;
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash;
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrashMetadata;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashDumpReader;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashHandlerDescription;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.logger.internal.DebugLogger;
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
        DebugLogger.info(
            TAG,
            "reportData: type = %s; customType = %s; name = %s",
            reportData.getType(), reportData.getCustomType(), reportData.getName()
        );
        if (reportData.isUndefinedType() == false) {
            final Runnable reportTask = new ReportRunnable(mContext, reportData, extras, mClientRepository);
            mTasksExecutor.execute(reportTask);
        } else {
            DebugLogger.warning(TAG, "Undefined report type: %d", reportData.getType());
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

    public void consumeCurrentSessionNativeCrash(
        @NonNull final AppMetricaNativeCrash nativeCrash,
        @NonNull final Consumer<File> finalizer
    ) {
        consumeNativeCrash(nativeCrash, finalizer,
            new Function<String, CounterReport>() {
                @Override
                public CounterReport apply(String input) {
                    CounterReport counterReport = EventsManager.currentSessionNativeCrashEntry(
                        input,
                        nativeCrash.getUuid(),
                        LoggerStorage.getOrCreatePublicLogger(nativeCrash.getMetadata().getApiKey())
                    );
                    counterReport.setEventEnvironment(nativeCrash.getMetadata().getErrorEnvironment());
                    return counterReport;
                }
            }
        );
    }

    public void consumePrevSessionNativeCrash(
        @NonNull final AppMetricaNativeCrash nativeCrash,
        @NonNull final Consumer<File> finalizer
    ) {
        consumeNativeCrash(nativeCrash, finalizer,
            new Function<String, CounterReport>() {
                @Override
                public CounterReport apply(String input) {
                    CounterReport counterReport = EventsManager.prevSessionNativeCrashEntry(
                        input,
                        nativeCrash.getUuid(),
                        LoggerStorage.getOrCreatePublicLogger(nativeCrash.getMetadata().getApiKey())
                    );
                    counterReport.setEventEnvironment(nativeCrash.getMetadata().getErrorEnvironment());
                    return counterReport;
                }
            }
        );
    }

    private void consumeNativeCrash(
        @NonNull final AppMetricaNativeCrash nativeCrash,
        @NonNull final Consumer<File> finalizer,
        @NonNull final Function<String, CounterReport> reportCreator
    ) {
        mTasksExecutor.execute(
            new ReadAndReportRunnable<>(
                fileProvider.getFileByNonNullPath(nativeCrash.getDumpFile()),
                new NativeCrashDumpReader(
                    new NativeCrashHandlerDescription(nativeCrash.getSource(), nativeCrash.getHandlerVersion()),
                    new NativeCrashConverter()
                ),
                finalizer,
                new NativeCrashConsumer(nativeCrash.getMetadata(), reportCreator)
            )
        );
    }

    @VisibleForTesting
    protected class NativeCrashConsumer implements Consumer<String> {

        private final AppMetricaNativeCrashMetadata metadata;
        private final Function<String, CounterReport> reportCreator;

        public NativeCrashConsumer(AppMetricaNativeCrashMetadata metadata,
                                   Function<String, CounterReport> reportCreator) {
            this.metadata = metadata;
            this.reportCreator = reportCreator;
        }

        @Override
        public void consume(@NonNull String input) {
            consumeCrash(
                new ClientDescription(
                    metadata.getApiKey(),
                    metadata.getPackageName(),
                    metadata.getProcessID(),
                    metadata.getProcessSessionID(),
                    metadata.getReporterType()
                ),
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
