package io.appmetrica.analytics.ndkcrashes.impl.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import java.io.File

/**
 * This class builds paths for the Chrome package.
 */
object PackagePaths {
    /*
    private static final String TAG = "PackagePaths";

    // Prevent instantiation.
    private PackagePaths() {}

     */

    /**
     * @ Build paths for the chrome/webview package for the purpose of loading CrashpadMain via
     * /system/bin/app_process.
     */
    @SuppressLint("InlinedApi")
    @JvmStatic // for tests
    fun makePackagePaths(context: Context, arch: String): Array<String> {
        return try {
            val pm = context.packageManager
            //region customization
            val pi = pm.getPackageInfo(
                context.packageName,
                PackageManager.GET_SHARED_LIBRARY_FILES or
                    if (AndroidUtils.isAndroidNAchieved()) PackageManager.MATCH_UNINSTALLED_PACKAGES else 0
            )
            //endregion
            val zipPaths = ArrayList<String>(10)
            pi.applicationInfo?.sourceDir?.let { zipPaths.add(it) }
            pi.applicationInfo?.splitSourceDirs?.let { zipPaths.addAll(it) }
            pi.applicationInfo?.sharedLibraryFiles?.let { zipPaths.addAll(it) }

            val libPaths = ArrayList<String>(10)
            val parent: File? = pi.applicationInfo?.nativeLibraryDir?.takeIf { it.isNotEmpty() }?.let {
                File(it).parentFile
            }
            if (parent != null) {
                libPaths.add(File(parent, arch).path)

                // arch is the currently loaded library's ABI name. This is the name of the library
                // directory in an APK, but may differ from the library directory extracted to the
                // filesystem. ARM family abi names have a suffix specifying the architecture
                // version, but may be extracted to directories named "arm64" or "arm".
                // crbug.com/930342
                if (arch.startsWith("arm64")) {
                    libPaths.add(File(parent, "arm64").path)
                } else if (arch.startsWith("arm")) {
                    libPaths.add(File(parent, "arm").path)
                }
            }
            for (zip in zipPaths) {
                if (zip.endsWith(".apk")) {
                    libPaths.add("$zip!/lib/$arch")
                }
            }
            System.getProperty("java.library.path")?.let { libPaths.add(it) }
            pi.applicationInfo?.nativeLibraryDir?.let { libPaths.add(it) }

            arrayOf(
                TextUtils.join(File.pathSeparator, zipPaths),
                TextUtils.join(File.pathSeparator, libPaths)
            )
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }
    }
}
