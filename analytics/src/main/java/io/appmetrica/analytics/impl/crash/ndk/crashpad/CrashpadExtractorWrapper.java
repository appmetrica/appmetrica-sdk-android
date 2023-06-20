package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.ac.CrashpadHelper;
import io.appmetrica.analytics.impl.utils.AbiResolver;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Helper to extract crashpad binary from apk.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CrashpadExtractorWrapper {
    private static final String TAG = "[CrashpadExtractorWrapper]";

    private static final String CRASHPAD_DIR = "appmetrica_crashpad_handler_extracted";
    private static final String APK_ENTRY = "appmetrica_crashpad_handler";
    @Deprecated
    private static final String APK_ENTRY_DEPRECATED = "appmetrica_handler";

    private static final Set<String> SUPPORTED_ABIS = new HashSet<String>();

    static {
        SUPPORTED_ABIS.add("armeabi-v7a");
        SUPPORTED_ABIS.add("arm64-v8a");
        SUPPORTED_ABIS.add("x86");
        SUPPORTED_ABIS.add("x86_64");
    }

    @NonNull
    private final Context context;
    @NonNull
    private final ICommonExecutor executor;
    @Nullable
    private final File defaultHandler;
    @NonNull
    private final List<String> executableNames;
    @Nullable
    private final File cacheDir;
    @Nullable
    private final File extractedBinariesDir;
    @NonNull
    private final Function<Void, String> versionExtractor;
    @NonNull
    private final AbiResolver abiResolver;
    @NonNull
    private final CrashpadExtractor crashpadExtractor;
    @NonNull
    private final Callable<String> libDirInsideApkCallable;
    @NonNull
    private final AppProcessConfigProvider appProcessConfigProvider;
    @NonNull
    private final FileProvider fileProvider;

    public CrashpadExtractorWrapper(@NonNull Context context,
                                    @NonNull FileProvider fileProvider,
                                    @NonNull ICommonExecutor executor
    ) {
        this(context, fileProvider, executor, Arrays.asList(
                "lib" + APK_ENTRY + ".so",
                "lib" + APK_ENTRY_DEPRECATED + ".so"
        ));
    }

    private CrashpadExtractorWrapper(@NonNull Context context,
                                     @NonNull FileProvider fileProvider,
                                     @NonNull ICommonExecutor executor,
                                     @NonNull List<String> defaultFileNames
    ) {
        this(
                context, executor, defaultFileNames, fileProvider,
                fileProvider.getAbsoluteFileByPath(fileProvider.getLibFolder(context), defaultFileNames.get(0)),
                fileProvider.getFileByPath(context.getCacheDir(), CRASHPAD_DIR),
                new Function<Void, String>() {
                    @Override
                    public String apply(Void input) {
                        return CrashpadHelper.getLibraryVersion();
                    }
                },
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return CrashpadHelper.getLibDirInsideApk();
                    }
                },
                new AbiResolver(SUPPORTED_ABIS)
        );
    }

    private CrashpadExtractorWrapper(@NonNull Context context,
                                     @NonNull ICommonExecutor executor,
                                     @NonNull List<String> defaultFileNames,
                                     @NonNull FileProvider fileProvider,
                                     @Nullable File defaultHandler,
                                     @Nullable File extractedBinariesDir,
                                     @NonNull Function<Void, String> versionExtractor,
                                     @NonNull Callable<String> libDirInsideApkCallable,
                                     @NonNull AbiResolver abiResolver
    ) {
        this(
                context, executor, defaultFileNames,
                defaultHandler, extractedBinariesDir, versionExtractor, libDirInsideApkCallable,
                abiResolver, new CrashpadExtractor(context, extractedBinariesDir),
                new AppProcessConfigProvider(),
                fileProvider
        );
    }

    @VisibleForTesting
    CrashpadExtractorWrapper(@NonNull Context context,
                             @NonNull ICommonExecutor executor,
                             @NonNull List<String> defaultFileNames,
                             @Nullable File defaultHandler,
                             @Nullable File extractedBinariesDir,
                             @NonNull Function<Void, String> versionExtractor,
                             @NonNull Callable<String> libDirInsideApkCallable,
                             @NonNull AbiResolver abiResolver,
                             @NonNull CrashpadExtractor crashpadExtractor,
                             @NonNull AppProcessConfigProvider appProcessConfigProvider,
                             @NonNull FileProvider fileProvider
    ) {
        this.context = context;
        this.executor = executor;
        this.executableNames = defaultFileNames;
        this.defaultHandler = defaultHandler;
        this.cacheDir = context.getCacheDir();
        this.extractedBinariesDir = extractedBinariesDir;
        this.versionExtractor = versionExtractor;
        this.libDirInsideApkCallable = libDirInsideApkCallable;
        this.abiResolver = abiResolver;
        this.crashpadExtractor = crashpadExtractor;
        this.appProcessConfigProvider = appProcessConfigProvider;
        this.fileProvider = fileProvider;
    }

    /**
     * Returns true if we should try to extract crashpad binary.
     */
    public boolean shouldExtract() {
        return defaultHandler == null || !defaultHandler.exists();
    }

    /**
     * Do extraction.
     */
    @Nullable
    @WorkerThread
    public ExtractorResult findOrExtractHandler() {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)) {
            return extractForQ();
        } else if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.M)) {
            ExtractorResult result = extractForMP();
            if (result == null || result.appProcessConfig == null) {
                return findExecutable();
            } else {
                dropStaleFiles(new AllFilesMatcher());
                return result;
            }
        } else {
            return findExecutable();
        }
    }

    @Nullable
    private ExtractorResult findExecutable() {
        if (shouldExtract()) {
            return extractLibToDataDir();
        } else { // default handler is not null
            String handler = defaultHandler.getAbsolutePath();
            YLogger.debug(TAG, "handler found on path %s", handler);
            return new ExtractorResult(handler, false, null);
        }
    }

    @VisibleForTesting
    ExtractorResult extractForQ() {
        return createExtractorResult(true);
    }

    @Nullable
    private ExtractorResult createExtractorResult(boolean useLinker) {
        File libDir = getLibDir();
        if (libDir != null) {
            for (String executable: executableNames) {
                File handlerFile = fileProvider.getFileByNonNullPath(libDir, executable);
                if (handlerFile.exists()) {
                    return new ExtractorResult(
                            handlerFile.getAbsolutePath(),
                            useLinker,
                            null
                    );
                }
            }
        }
        return null;
    }

    @Nullable
    private File getLibDir() {
        String libDir = findLibDirInsideApk();
        if (!TextUtils.isEmpty(libDir)) {
            return fileProvider.getFileByNonNullPath(libDir);
        } else {
            return null;
        }
    }

    @VisibleForTesting
    ExtractorResult extractForMP() {
        AppProcessConfig config = appProcessConfigProvider.provideAppConfig(context, abiResolver.getAbi());
        if (config != null) {
            File libDir = getLibDir();
            if (libDir == null) {
                return new ExtractorResult(
                        //in case of start via app_process this argument is optional
                        // and used only for proper arguments alignment
                        "stub", false, config
                );
            } else {
                for (String executable: executableNames) {
                    File handlerFile = fileProvider.getFileByNonNullPath(libDir, executable);
                    if (handlerFile.exists()) {
                        return new ExtractorResult(handlerFile.getAbsolutePath(), false, config);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private String findLibDirInsideApk() {
        try {
            String libDir = libDirInsideApkCallable.call();
            YLogger.debug(TAG, "lib dir inside apk %s", libDir);
            return libDir;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @VisibleForTesting
    ExtractorResult extractLibToDataDir() {
        long startTime = SystemClock.elapsedRealtime();
        final String suffix = "-" + versionExtractor.apply(null);
        YLogger.debug(TAG, "Handler not found. Try to extract");
        String abi = abiResolver.getAbi();
        YLogger.debug(TAG, "current ABI is %s", abi);
        if (abi != null) {
            if (makeCrashpadDirAndSetPermission()) {
                String extractedFile;
                dropStaleFiles(new FileNameWithoutSuffixMatcher(suffix));
                for (String executable: executableNames) {
                    extractedFile = crashpadExtractor.extractFileIfStale(
                            String.format("lib/%s/%s", abi, executable), executable + suffix
                    );
                    long duration = SystemClock.elapsedRealtime() - startTime;
                    YLogger.debug(TAG, "Time to extract crashpad binary: %d ms", duration);
                    if (extractedFile != null) {
                        return new ExtractorResult(extractedFile, false, null);
                    }
                }
            } else {
                YLogger.debug(TAG, "can't make tmp dir");
            }
        }
        return null;
    }

    @VisibleForTesting
    boolean makeCrashpadDirAndSetPermission() {
        if (extractedBinariesDir == null) {
            YLogger.info(TAG, "Extracted binaries dir is null");
            return false;
        }
        if (!extractedBinariesDir.exists()) {
            YLogger.debug(TAG, "make crashpad dir %s", extractedBinariesDir.getAbsolutePath());
            if (!extractedBinariesDir.mkdirs()) {
                return false;
            }
            if (cacheDir == null || !cacheDir.setExecutable(true, false)) {
                return false;
            }
            return extractedBinariesDir.setExecutable(true, false);
        }
        return true;
    }

    @VisibleForTesting
    void deleteFiles(@NonNull Function<File, Boolean> condition) {
        if (extractedBinariesDir == null) {
            return;
        }
        File[] files = extractedBinariesDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (condition.apply(file)) {
                if (file.delete()) {
                    YLogger.debug(TAG, "Removed obsolete file %s", file.getName());
                } else {
                    YLogger.warning(TAG, "Unable to remove %s", file.getName());
                }
            }
        }
    }

    private void dropStaleFiles(@NonNull final Function<File, Boolean> condition) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                deleteFiles(condition);
            }
        });
    }

    @VisibleForTesting
    static class FileNameWithoutSuffixMatcher implements Function<File, Boolean> {

        private final String suffix;

        public FileNameWithoutSuffixMatcher(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public Boolean apply(File file) {
            return !file.getName().endsWith(suffix);
        }
    }

    @VisibleForTesting
    static class AllFilesMatcher implements Function<File, Boolean> {

        @Override
        public Boolean apply(File input) {
            return true;
        }
    }
}
