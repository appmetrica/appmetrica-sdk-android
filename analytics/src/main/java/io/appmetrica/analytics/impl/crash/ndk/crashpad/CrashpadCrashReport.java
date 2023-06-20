package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

public final class CrashpadCrashReport {

    static final String ARGUMENT_UUID = "arg_ui";
    @VisibleForTesting
    static final String ARGUMENT_DUMP_FILE = "arg_df";
    @VisibleForTesting
    static final String ARGUMENT_CREATION_TIME = "arg_ct";

    public static final String PAYLOAD_CRASH_ID = "payload_crash_id";

    @NonNull
    public final String uuid;
    @NonNull
    public final String dumpFile;
    public final long creationTime;

    @VisibleForTesting
    public CrashpadCrashReport(@NonNull String uuid, @NonNull String dumpFile, long creationTime) {
        this.uuid = uuid;
        this.dumpFile = dumpFile;
        this.creationTime = creationTime;
    }

    @Nullable
    public static CrashpadCrashReport fromBundle(@NonNull String uuid, @NonNull Bundle bundle) {
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        if (!bundle.containsKey(ARGUMENT_DUMP_FILE) || !bundle.containsKey(ARGUMENT_CREATION_TIME)) {
            return null;
        }
        String dumpFile = bundle.getString(ARGUMENT_DUMP_FILE);
        if (TextUtils.isEmpty(dumpFile)) {
            return null;
        }
        long timestamp = bundle.getLong(ARGUMENT_CREATION_TIME);
        return new CrashpadCrashReport(uuid, dumpFile, timestamp);
    }
}
