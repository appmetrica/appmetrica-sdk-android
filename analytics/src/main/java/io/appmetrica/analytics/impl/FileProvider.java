package io.appmetrica.analytics.impl;

import android.content.Context;
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
    public File getFileFromStorage(@NonNull Context context, @NonNull String fileName) {
        return FileUtils.getFileFromAppStorage(context, fileName);
    }

    @NonNull
    public File getFileByNonNullPath(@NonNull String path) {
        return new File(path);
    }

    @NonNull
    public File getFileByNonNullPath(@NonNull File file, @NonNull String name) {
        return new File(file, name);
    }

    @NonNull
    public File getFileInNonNullDirectory(@NonNull File parent, @NonNull String fileName) {
        return new File(parent, fileName);
    }
}
