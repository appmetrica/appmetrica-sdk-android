// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.impl.FileProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class builds paths for the Chrome package.
 */
public abstract class PackagePaths {
    /*
    private static final String TAG = "PackagePaths";

    // Prevent instantiation.
    private PackagePaths() {}

     */

    @NonNull
    private static final FileProvider fileProvider = new FileProvider();

    /**
     * @ Build paths for the chrome/webview package for the purpose of loading CrashpadMain via
     * /system/bin/app_process.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("InlinedApi")
    public static String[] makePackagePaths(Context context, String arch) {
        try {
            PackageManager pm = context.getPackageManager();
            //region customization
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SHARED_LIBRARY_FILES | (AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)
                            ? PackageManager.MATCH_UNINSTALLED_PACKAGES : 0));
            //endregion

            List<String> zipPaths = new ArrayList<String>(10);
            zipPaths.add(pi.applicationInfo.sourceDir);
            if (pi.applicationInfo.splitSourceDirs != null) {
                Collections.addAll(zipPaths, pi.applicationInfo.splitSourceDirs);
            }
            if (pi.applicationInfo.sharedLibraryFiles != null) {
                Collections.addAll(zipPaths, pi.applicationInfo.sharedLibraryFiles);
            }

            List<String> libPaths = new ArrayList<String>(10);
            String nativeLibraryDir = pi.applicationInfo.nativeLibraryDir;
            final File parent = TextUtils.isEmpty(nativeLibraryDir) ? null :
                    fileProvider.getFileByNonNullPath(nativeLibraryDir).getParentFile();
            if (parent != null) {
                libPaths.add(fileProvider.getFileInNonNullDirectory(parent, arch).getPath());

                // arch is the currently loaded library's ABI name. This is the name of the library
                // directory in an APK, but may differ from the library directory extracted to the
                // filesystem. ARM family abi names have a suffix specifying the architecture
                // version, but may be extracted to directories named "arm64" or "arm".
                // crbug.com/930342
                if (arch.startsWith("arm64")) {
                    libPaths.add(fileProvider.getFileInNonNullDirectory(parent, "arm64").getPath());
                } else if (arch.startsWith("arm")) {
                    libPaths.add(fileProvider.getFileInNonNullDirectory(parent, "arm").getPath());
                }
            }
            for (String zip : zipPaths) {
                if (zip.endsWith(".apk")) {
                    libPaths.add(zip + "!/lib/" + arch);
                }
            }
            libPaths.add(System.getProperty("java.library.path"));
            libPaths.add(pi.applicationInfo.nativeLibraryDir);
            return new String[] {TextUtils.join(File.pathSeparator, zipPaths),
                    TextUtils.join(File.pathSeparator, libPaths)};

        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
