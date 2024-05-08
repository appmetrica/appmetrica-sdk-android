package io.appmetrica.analytics.impl.crash;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.io.File;

public class CrashFolderPreparer {

    private static final String TAG = "[CrashFolderPreparer]";

    public boolean prepareCrashFolder(@Nullable File crashFolder) {
        if (crashFolder == null) {
            return false;
        }
        if (crashFolder.exists()) {
            if (crashFolder.isDirectory()) {
                return true;
            } else {
                if (crashFolder.delete()) {
                    return makeCrashesFolder(crashFolder);
                } else {
                    YLogger.debug(TAG, "Can't delete non-directory file with crash directory path.");
                    return false;
                }
            }
        } else {
            return makeCrashesFolder(crashFolder);
        }
    }

    boolean makeCrashesFolder(@NonNull File crashFolder) {
        if (crashFolder.mkdir()) {
            return true;
        } else {
            YLogger.debug(TAG, "Can't make crash directory.");
            return false;
        }
    }

}
