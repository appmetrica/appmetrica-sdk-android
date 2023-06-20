package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.os.Build;
import androidx.annotation.RequiresApi;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ac.CrashpadServiceHelper;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class RemoveCompletedCrashesRunnable implements Runnable {

    @Override
    public void run() {
        YLogger.debug("[RemoveCompletedCrashesRunnable]", "remove completed crashes");
        CrashpadServiceHelper.deleteCompletedReports();
    }
}
