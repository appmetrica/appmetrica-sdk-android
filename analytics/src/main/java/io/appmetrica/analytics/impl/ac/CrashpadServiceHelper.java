package io.appmetrica.analytics.impl.ac;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class CrashpadServiceHelper {

    public static native void setUpServiceHelper(String folder);

    public static native void cancelSetUpServiceHelper();

    public static native Bundle readCrash(String uuid);

    public static native List<Bundle> readOldCrashes();

    private static native boolean markCrashCompleted(String uuid);

    public static native boolean deleteCompletedReports();

    public static boolean markCrashCompletedAndDeleteAllCompleted(String uuid) {
        YLogger.debug("[CrashpadServiceHelper]","mark crash %s completed and remove completed", uuid);
        return markCrashCompleted(uuid) && deleteCompletedReports();
    }

}
