package io.appmetrica.analytics.impl.crash;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.io.File;

public class ReadAndReportRunnable<Output> implements Runnable {

    private static final String TAG = "[ReadAndReportRunnable]";

    @NonNull
    private final File crashFile;
    @NonNull
    private final Function<File, Output> fileReader;
    @NonNull
    private final Consumer<File> finalizator;
    @NonNull
    private final Consumer<Output> crashConsumer;

    public ReadAndReportRunnable(
            @NonNull File crashFile,
            @NonNull Function<File, Output> fileReader,
            @NonNull Consumer<File> finalizator,
            @NonNull Consumer<Output> crashConsumer
    ) {
        this.crashFile = crashFile;
        this.fileReader = fileReader;
        this.finalizator = finalizator;
        this.crashConsumer = crashConsumer;
    }

    @Override
    public void run() {
        if (crashFile.exists()) {
            try {
                Output result = fileReader.apply(crashFile);
                if (result != null) {
                    YLogger.debug(TAG, "for file %s result is %s", crashFile.getName(), result);
                    crashConsumer.consume(result);
                } else {
                    YLogger.debug(TAG, "for file %s result is null", crashFile.getName());
                }
            } catch (Throwable exception) {
                YLogger.error(TAG, exception, "can't handle crash in file %s due to Exception", crashFile.getName());
            } finally {
                finalizator.consume(crashFile);
            }
        }
    }
}
