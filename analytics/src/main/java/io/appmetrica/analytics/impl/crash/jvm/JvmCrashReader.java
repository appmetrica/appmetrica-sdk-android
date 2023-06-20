package io.appmetrica.analytics.impl.crash.jvm;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.IOUtils;
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
            YLogger.e("%s can't read crash %s", TAG, crashFile.getName());
        } else {
            try {
                return new JvmCrash(crashData);
            } catch (Throwable e) {
                YLogger.e("%s can't read crash %s due to JSONException", TAG, crashFile.getName());
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
                YLogger.w("%s file %s was not deleted.", TAG, crashFile.getName());
            }
        } catch (Throwable exception) {
            YLogger.e(
                    "%s can't handle delete crash file %s due to Exception",
                    TAG, crashFile.getName(), exception
            );
        }
    }
}
