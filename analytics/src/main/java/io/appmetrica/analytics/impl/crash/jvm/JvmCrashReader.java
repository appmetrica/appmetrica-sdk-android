package io.appmetrica.analytics.impl.crash.jvm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.io.File;

public class JvmCrashReader implements Function<File, JvmCrash>, Consumer<File> {

    private static final String TAG = "[JvmCrashReader]";

    @Override
    @Nullable
    public JvmCrash apply(@NonNull File crashFile) {
        return handleCrashData(crashFile, IOUtils.getStringFileLocked(crashFile), crashFile.lastModified());
    }

    @VisibleForTesting
    @Nullable
    JvmCrash handleCrashData(@NonNull File crashFile, @Nullable String crashData, long fileModifiedTimestamp) {
        if (StringUtils.isNullOrEmpty(crashData)) {
            DebugLogger.INSTANCE.error(TAG, "can't read crash %s", crashFile.getName());
        } else {
            try {
                return new JvmCrash(crashData, fileModifiedTimestamp);
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(
                    TAG,
                    "can't read crash %s due to JSONException",
                    crashFile.getName()
                );
            }
        }
        return null;
    }

    @Override
    public void consume(@NonNull File crashFile) {
        //Example of unexpected situation: security exception (owner - other user), regular IOException.
        try {
            boolean isDeleted = crashFile.delete();
            if (isDeleted == false) {
                DebugLogger.INSTANCE.warning(TAG, "file %s was not deleted.", crashFile.getName());
            }
        } catch (Throwable exception) {
            DebugLogger.INSTANCE.error(
                TAG,
                "can't handle delete crash file %s due to Exception",
                crashFile.getName(),
                exception
            );
        }
    }
}
