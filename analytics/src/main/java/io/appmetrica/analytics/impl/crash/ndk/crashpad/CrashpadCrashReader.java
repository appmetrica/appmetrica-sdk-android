package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ac.CrashpadServiceHelper;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashReader;
import java.util.List;
import java.util.concurrent.Callable;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class CrashpadCrashReader implements NativeCrashReader<String> {

    private static final String TAG = "[CrashpadCrashReader]";

    @NonNull
    private final CrashpadCrashReporter crashReporter;
    @NonNull
    private final Function<String, Bundle> crashReader;
    @NonNull
    private final Callable<List<Bundle>> oldCrashesReader;
    @NonNull
    private final Consumer<String> completedConsumer;
    @NonNull
    private final CrashpadCrashParser parser;

    public CrashpadCrashReader(@NonNull CrashpadCrashReporter reporter) {
        this(reporter, new CrashpadCrashParser(),
        new Function<String, Bundle>() {
            @Override
            public Bundle apply(@NonNull String uuid) {
                return CrashpadServiceHelper.readCrash(uuid);
            }
        }, new Callable<List<Bundle>>() {
            @Override
            public List<Bundle> call() {
                return CrashpadServiceHelper.readOldCrashes();
            }
        }, new Consumer<String>() {
            @Override
            public void consume(String uuid) {
                CrashpadServiceHelper.markCrashCompletedAndDeleteAllCompleted(uuid);
            }
        });
    }

    @VisibleForTesting
    public CrashpadCrashReader(@NonNull CrashpadCrashReporter reporter,
                               @NonNull CrashpadCrashParser parser,
                               @NonNull Function<String, Bundle> crashReader,
                               @NonNull Callable<List<Bundle>> oldCrashesReader,
                               @NonNull Consumer<String> completedConsumer) {
        this.crashReporter = reporter;
        this.parser = parser;
        this.crashReader = crashReader;
        this.oldCrashesReader = oldCrashesReader;
        this.completedConsumer = completedConsumer;
    }

    @Override
    public void checkForPreviousSessionCrashes() {
        try {
            for (Bundle bundle: oldCrashesReader.call()) {
                String uuid = bundle.getString(CrashpadCrashReport.ARGUMENT_UUID);
                if (!TextUtils.isEmpty(uuid)) {
                    CrashpadCrash crash = parser.apply(uuid, bundle);
                    if (crash != null) {
                        YLogger.debug(TAG, "handle old crash with uuid %s", uuid);
                        crashReporter.reportPrevSessionNativeCrash(crash);
                    } else {
                        completedConsumer.consume(uuid);
                        YLogger.debug(TAG, "can't read old crash with uuid %s", uuid);
                    }
                }
            }
        } catch (Exception e) {
            YLogger.error(TAG, e, "can't read old crashes");
        }
    }

    @Override
    public void handleRealtimeCrash(@NonNull String uuid) {
        CrashpadCrash crash = readCrash(uuid);
        if (crash != null) {
            YLogger.debug(TAG, "new current session crashpad crash with id %s", uuid);
            crashReporter.reportCurrentSessionNativeCrash(crash);
        } else {
            completedConsumer.consume(uuid);
            YLogger.debug(TAG, "can't read new session crashpad crash with id %s", uuid);
        }
    }

    @Nullable
    CrashpadCrash readCrash(@NonNull String uuid) {
        YLogger.debug(TAG, "new crash from crashpad %s", uuid);
        try {
            Bundle data = crashReader.apply(uuid);
            if (data != null) {
                return parser.apply(uuid, data);
            } else {
                return null;
            }
        } catch (Throwable exception) {
            YLogger.error(TAG, exception, "can't read report for %s", uuid);
        }
        return null;
    }

}
