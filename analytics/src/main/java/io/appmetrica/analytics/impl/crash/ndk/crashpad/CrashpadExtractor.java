package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.utils.file.SuspendableFileLocker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class CrashpadExtractor {

    private static final String TAG = "[CrashpadExtractor]";

    @NonNull
    private final Context context;
    @Nullable
    private final File extractedBinariesDir;
    @NonNull
    private final FileProvider fileProvider;

    CrashpadExtractor(@NonNull Context context, @Nullable File extractedBinariesDir) {
        this(context, extractedBinariesDir, new FileProvider());
    }

    @VisibleForTesting
    CrashpadExtractor(@NonNull Context context,
                      @Nullable File extractedBinariesDir,
                      @NonNull FileProvider fileProvider) {
        this.context = context;
        this.extractedBinariesDir = extractedBinariesDir;
        this.fileProvider = fileProvider;
    }

    @SuppressLint({"SetWorldReadable"})
    @Nullable
    String extractFileIfStale(@NonNull String pathWithinApk, @NonNull String extractedExecutableName) {
        String apkPath = context.getApplicationInfo().sourceDir;
        File libraryFile = fileProvider.getFileByPath(extractedBinariesDir, extractedExecutableName);
        if (libraryFile == null) {
            YLogger.info(TAG, "Library file %s/%s is null", extractedBinariesDir, extractedExecutableName);
            return null;
        }
        if (libraryFile.exists()) {
            YLogger.debug(TAG, "crashpad handler is already extracted");
            return libraryFile.getAbsolutePath();
        }

        SuspendableFileLocker fileLocker = SuspendableFileLocker.getLock(context, "crpad_ext");

        ZipFile zipFile = null;
        try {
            fileLocker.lock();
            if (libraryFile.exists()) {
                YLogger.debug(TAG, "crashpad handler is already extracted - under lock");
                return libraryFile.getAbsolutePath();
            }
            zipFile = new ZipFile(apkPath);
            ZipEntry zipEntry = zipFile.getEntry(pathWithinApk);
            if (zipEntry == null) {
                throw new RuntimeException("Cannot find ZipEntry" + pathWithinApk);
            }
            InputStream inputStream = zipFile.getInputStream(zipEntry);

            IOUtils.copyChars(inputStream, new FileOutputStream(libraryFile));
            if (!libraryFile.setReadable(true, false)) {
                YLogger.debug(TAG, "can't make crashpad executable readable");
                return null;
            }
            if (!libraryFile.setExecutable(true, false)) {
                YLogger.debug(TAG, "can't make crashpad executable executable");
                return null;
            }
            return libraryFile.getAbsolutePath();
        } catch (Throwable e) {
            YLogger.error(TAG, e, "can't copy handler");
            return null;
        } finally {
            fileLocker.unlock();
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ignored) { }
        }
    }

}
