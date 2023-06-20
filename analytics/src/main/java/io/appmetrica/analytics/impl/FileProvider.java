package io.appmetrica.analytics.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import java.io.File;

@SuppressWarnings("checkstyle:rawFileCreation")
public class FileProvider {

    @Nullable
    public String getStorageSubDirectory(@NonNull Context context, @NonNull String subfolder) {
        File storageDirectory = FileUtils.getAppStorageDirectory(context);
        return storageDirectory == null ? null : storageDirectory.getAbsolutePath() + "/" + subfolder;
    }

    @Nullable
    public File getStorageSubDirectoryFile(@NonNull Context context, @NonNull String subfolder) {
        return getFileFromStorage(context, subfolder);
    }

    @Nullable
    public File getLibFolder(@NonNull Context context) {
        File dataDir = FileUtils.getAppDataDir(context);
        if (dataDir == null) {
            return null;
        }
        File libDir = new File(dataDir, "lib");
        if (!libDir.exists()) {
            libDir.mkdirs();
        }
        return libDir;
    }

    @Nullable
    public File getDbFileFromFilesStorage(@NonNull Context context, @NonNull String dbName) {
        return context.getDatabasePath(dbName);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    public File getDbFileFromNoBackupStorage(@NonNull Context context, @NonNull String dbName) {
        File path = context.getNoBackupFilesDir();
        if (path == null) {
            return null;
        }
        return new File(path, dbName);
    }

    @Nullable
    public File getFileFromStorage(@NonNull Context context, @NonNull String fileName) {
        return FileUtils.getFileFromAppStorage(context, fileName);
    }

    @Nullable
    public File getCrashesDirectory(@NonNull Context context) {
        return getStorageSubDirectoryFile(context, "appmetrica_crashes");
    }

    @Nullable
    public File getFileByPath(@Nullable String path) {
        return path == null ? null : getFileByNonNullPath(path);
    }

    @NonNull
    public File getFileByNonNullPath(@NonNull String path) {
        return new File(path);
    }

    @Nullable
    public File getFileByPath(@Nullable File file, @NonNull String name) {
        if (file == null) {
            return null;
        }
        return getFileByNonNullPath(file, name);
    }

    @NonNull
    public File getFileByNonNullPath(@NonNull File file, @NonNull String name) {
        return new File(file, name);
    }

    @Nullable
    public File getAbsoluteFileByPath(@Nullable File file, @NonNull String name) {
        if (file == null) {
            return null;
        }
        return getFileByPath(file.getAbsoluteFile(), name);
    }

    @NonNull
    public File getFileInNonNullDirectory(@NonNull File parent, @NonNull String fileName) {
        return new File(parent, fileName);
    }
}
