package io.appmetrica.analytics.impl.crash;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.io.File;

public class CrashFolderPreparer {

    private static final String TAG = "[CrashFolderPreparer]";

    public boolean prepareCrashFolder(@Nullable File crashFolder) {
        DebugLogger.INSTANCE.info(TAG, "prepare crash directory: %s", crashFolder);
        if (crashFolder == null) {
            return false;
        }
        if (crashFolder.exists()) {
            if (crashFolder.isDirectory()) {
                DebugLogger.INSTANCE.info(TAG, "Crash directory: %s already exists", crashFolder);
                return true;
            } else {
                DebugLogger.INSTANCE.info(TAG, "Recreate crash directory: %s", crashFolder);
                if (crashFolder.delete()) {
                    return makeCrashesFolder(crashFolder);
                } else {
                    DebugLogger.INSTANCE.info(
                        TAG,
                        "Can't delete non-directory file with crash directory path."
                    );
                    return false;
                }
            }
        } else {
            return makeCrashesFolder(crashFolder);
        }
    }

    boolean makeCrashesFolder(@NonNull File crashFolder) {
        if (crashFolder.mkdir()) {
            DebugLogger.INSTANCE.info(TAG, "Crash directory: %s created successful", crashFolder);
            return true;
        } else {
            DebugLogger.INSTANCE.info(TAG, "Can't make crash directory.");
            return false;
        }
    }

}
