package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.db.FileConstants;
import io.appmetrica.analytics.impl.utils.UuidGenerator;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.io.File;
import java.io.FileOutputStream;

class PersistentUuidHolder {

    private static final String TAG = "[PersistentUuidProvider]";

    @NonNull
    private static final String FILE_NAME = FileConstants.UUID_FILE_NAME;

    @NonNull
    private final Context context;
    @NonNull
    private final FileProvider fileProvider;
    @NonNull
    private final UuidGenerator uuidGenerator;

    public PersistentUuidHolder(@NonNull Context context) {
        this(context, new FileProvider(), new UuidGenerator());
    }

    @Nullable
    public String readUuid() {
        File file = FileUtils.getFileFromSdkStorage(context, FILE_NAME);
        String result = IOUtils.getStringFileLocked(file);
        YLogger.info(TAG, "Uuid from file with path \"%s\" (file exists? %b) is \"%s\".",
                file == null ? null : file.getPath(), file != null && file.exists(), result);
        return result;
    }

    @Nullable
    public String handleUuid(@Nullable String knownUuid) {
        YLogger.info(TAG, "Uuid generation started...");
        try {
            String uuid = TextUtils.isEmpty(knownUuid) ? uuidGenerator.generateUuid() : knownUuid;
            YLogger.info(TAG, "Save new uuid = %s", uuid);
            File file = fileProvider.getFileFromStorage(context, FILE_NAME);
            if (file != null) {
                IOUtils.writeStringFileLocked(uuid, FILE_NAME, new FileOutputStream(file));
                YLogger.info(TAG, "Generated uuid = \"%s\" was stored to file with path = \"%s\"",
                        uuid, file.getPath());
            } else {
                YLogger.info(TAG, "File is null");
            }
            return uuid;
        } catch (Throwable e) {
            YLogger.error(TAG, e);
        }
        return null;
    }

    public void checkMigration() {
        File file = FileUtils.getFileFromSdkStorage(context, FILE_NAME);
        if (file == null) {
            YLogger.w(TAG, "UUID file is null");
            return;
        }
        if (!file.exists()) {
            YLogger.info(TAG, "File with path = `%s` is not exist. Trying migrate old", file.getPath());
            File old = FileUtils.getFileFromAppStorage(context, FILE_NAME);
            if (old != null && old.exists()) {
                YLogger.info(TAG, "Found old uuid.data file with path = `%s`. Move it.", old.getPath());
                boolean status = FileUtils.copyToNullable(old, file);
                YLogger.info(TAG, "Move finished with status = %s", status);
            }
        }
    }

    @VisibleForTesting
    PersistentUuidHolder(@NonNull Context context,
                         @NonNull FileProvider fileProvider,
                         @NonNull UuidGenerator uuidGenerator) {
        this.context = context;
        this.fileProvider = fileProvider;
        this.uuidGenerator = uuidGenerator;
    }
}
