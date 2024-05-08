package io.appmetrica.analytics.impl.crash;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.ReportToSend;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash;
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock;
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CrashToFileWriter {

    private static final String TAG = "[CrashToFileWriter]";

    @NonNull
    private final FileProvider mFileProvider;
    @NonNull
    private final CrashFolderPreparer mCrashFolderPreparer;
    @NonNull
    private final Context mContext;
    @NonNull
    private final FileLocksHolder fileLocksHolder;

    public CrashToFileWriter(@NonNull Context context) {
        this(
                context,
                new FileProvider(),
                new CrashFolderPreparer(),
                FileLocksHolder.getInstance(context)
        );
    }

    @VisibleForTesting
    CrashToFileWriter(@NonNull Context context,
                      @NonNull FileProvider fileProvider,
                      @NonNull CrashFolderPreparer crashFolderPreparer,
                      @NonNull FileLocksHolder fileLocksHolder) {
        mContext = context;
        mFileProvider = fileProvider;
        mCrashFolderPreparer = crashFolderPreparer;
        this.fileLocksHolder = fileLocksHolder;
    }

    public void writeToFile(@NonNull ReportToSend toSend) {
        File crashFolder = FileUtils.getCrashesDirectory(mContext);
        if (mCrashFolderPreparer.prepareCrashFolder(crashFolder)) {
            ProcessConfiguration configuration = toSend.getEnvironment().getProcessConfiguration();
            Integer processID = configuration.getProcessID();
            String processSessionID = configuration.getProcessSessionID();
            final String fileName = processID + "-" + processSessionID;
            final ExclusiveMultiProcessFileLock fileLocker = fileLocksHolder.getOrCreate(fileName);
            PrintWriter printWriter = null;
            try {
                fileLocker.lock();
                File crash = mFileProvider.getFileInNonNullDirectory(crashFolder, fileName);
                printWriter = new PrintWriter(new BufferedOutputStream(new FileOutputStream(crash)));
                printWriter.write(new JvmCrash(
                        toSend.getReport(),
                        toSend.getEnvironment(),
                        toSend.getTrimmedFields()
                ).toJSONString());
                YLogger.debug(TAG, "Crash saved: %s.", crash.getName());
            } catch (IOException ioe) {
                YLogger.error(TAG, ioe, "Can't write crash to file.");
            } catch (Throwable e) {
                YLogger.error(TAG, e, "Can't write crash to file. Wrong json.");
            } finally {
                Utils.closeCloseable(printWriter);
                fileLocker.unlockAndClear();
            }
        } else {
            YLogger.error(TAG, "Failed to prepare crash folder %s", crashFolder.getAbsolutePath());
        }
    }
}
