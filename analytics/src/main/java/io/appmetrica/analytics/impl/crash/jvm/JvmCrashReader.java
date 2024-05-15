package io.appmetrica.analytics.impl.crash.jvm;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.io.File;

public class JvmCrashReader implements Function<File, JvmCrash>, Consumer<File> {

    private static final String TAG = "[JvmCrashReader]";

    @Override
    @Nullable
    public JvmCrash apply(@NonNull File crashFile) {
        return handleCrashData(crashFile, IOUtils.getStringFileLocked(crashFile));
    }

    @VisibleForTesting
    @Nullable
    JvmCrash handleCrashData(@NonNull File crashFile, @Nullable String crashData) {
        if (TextUtils.isEmpty(crashData)) {
            DebugLogger.error(TAG, "can't read crash %s", crashFile.getName());
        } else {
            try {
                return new JvmCrash(crashData);
            } catch (Throwable e) {
                DebugLogger.error(TAG, "can't read crash %s due to JSONException", crashFile.getName());
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
                DebugLogger.warning(TAG, "file %s was not deleted.", crashFile.getName());
            }
        } catch (Throwable exception) {
            DebugLogger.error(
                TAG,
                "can't handle delete crash file %s due to Exception",
                crashFile.getName(),
                exception
            );
        }
    }
}
