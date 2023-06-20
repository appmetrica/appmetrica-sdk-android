package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.FileProvider;
import io.appmetrica.analytics.impl.ac.CrashpadHelper;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.crash.ndk.NdkCrashInitializer;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CrashpadInitializer implements NdkCrashInitializer {

    private static final String TAG = "[CrashpadInitializer]";

    private static final String ARGUMENT_DUMP_DIR = "arg_dd";
    private static final String ARGUMENT_HANDLER_PATH = "arg_hp";
    private static final String ARGUMENT_USE_LINKER = "arg_ul";

    private static final String ARGUMENT_USE_APP_PROCESS = "arg_ap";
    private static final String ARGUMENT_MAIN_CLASS = "arg_mc";
    private static final String ARGUMENT_APK_PATH = "arg_akp";
    private static final String ARGUMENT_LIB_PATH = "arg_lp";
    private static final String ARGUMENT_DATA_DIRECTORY = "arg_dp";

    private static final String ARGUMENT_IS_64_BIT = "arg_i64";
    private static final String ARGUMENT_SOCKET_NAME = "arg_sn";

    @NonNull
    private final Context context;
    @NonNull
    private final ProcessConfiguration processConfiguration;
    @NonNull
    private CrashpadExtractorWrapper crashpadExtractorWrapper;
    @NonNull
    private final Consumer<Bundle> setUpNativeWrapper;
    @NonNull
    private final PermanentConfigSerializer permanentConfigSerializer;
    @NonNull
    private final RuntimeConfigStorage runtimeConfigStorage;
    @NonNull
    private final Function<Void, String> crashpadVersionRetriever;

    public CrashpadInitializer(@NonNull Context context, @NonNull ProcessConfiguration processConfiguration) {
        this(context, processConfiguration, new FileProvider(),
                new Consumer<Bundle>() {
                    @Override
                    public void consume(Bundle input) {
                        YLogger.debug(TAG, "start crashpad with arguments %s", input);
                        CrashpadHelper.setUpNativeUncaughtExceptionHandler(input);
                    }
                }
        );
    }

    private CrashpadInitializer(@NonNull Context context,
                                @NonNull ProcessConfiguration processConfiguration,
                                @NonNull FileProvider fileProvider,
                                @NonNull Consumer<Bundle> setUpNativeWrapper
    ) {
        this(context, processConfiguration,
                new CrashpadExtractorWrapper(
                        context, fileProvider,
                        ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor()
                ), setUpNativeWrapper,
                new PermanentConfigSerializer(),
                new RuntimeConfigStorage(),
                new Function<Void, String>() {
                    @Override
                    public String apply(Void input) {
                        return CrashpadHelper.getLibraryVersion();
                    }
                });
    }

    @VisibleForTesting
    CrashpadInitializer(@NonNull Context context,
                        @NonNull ProcessConfiguration processConfiguration,
                        @NonNull CrashpadExtractorWrapper crashpadExtractorWrapper,
                        @NonNull Consumer<Bundle> setUpNativeWrapper,
                        @NonNull PermanentConfigSerializer serializer,
                        @NonNull RuntimeConfigStorage runtimeConfigStorage,
                        @NonNull Function<Void, String> crashpadVersionRetriever) {
        this.context = context;
        this.processConfiguration = processConfiguration;
        this.crashpadExtractorWrapper = crashpadExtractorWrapper;
        this.setUpNativeWrapper = setUpNativeWrapper;
        this.permanentConfigSerializer = serializer;
        this.runtimeConfigStorage = runtimeConfigStorage;
        this.crashpadVersionRetriever = crashpadVersionRetriever;
    }

    @NonNull
    @Override
    public String getFolderName() {
        return CrashpadConstants.APPMETRICA_NATIVE_CRASHES_FOLDER;
    }

    @NonNull
    @Override
    public String getLibraryName() {
        return "appmetrica-native";
    }

    @WorkerThread
    @Override
    public void setUpHandler(@NonNull String apiKey, @NonNull String folder, @Nullable String errorEnv) {
        ExtractorResult executable = crashpadExtractorWrapper.findOrExtractHandler();
        if (executable != null &&
                (!TextUtils.isEmpty(executable.pathToHandler) || executable.appProcessConfig != null )) {
            runtimeConfigStorage.setErrorEnvironment(errorEnv);
            runtimeConfigStorage.setHandlerVersion(crashpadVersionRetriever.apply(null));
            setUpNativeWrapper.consume(fillParameters(apiKey, folder, executable, runtimeConfigStorage.serialize()));
        } else {
            YLogger.debug(TAG, "can't extract handler");
        }
    }

    @Override
    public void cancelSetUp() {
        CrashpadHelper.cancelSetUpNativeUncaughtExceptionHandler();
    }

    @Override
    public void setLogsEnabled(boolean enabled) {
        CrashpadHelper.logsEnabled(enabled);
    }

    @Override
    public void updateErrorEnvironment(@Nullable String errorEnvironment) {
        runtimeConfigStorage.setErrorEnvironment(errorEnvironment);
        CrashpadHelper.updateRuntimeConfig(runtimeConfigStorage.serialize());
    }

    @VisibleForTesting
    @NonNull
    Bundle fillParameters(@NonNull String apiKey, @NonNull String folder,
                          @NonNull ExtractorResult extractorResult, @NonNull String runtimeConfig) {
        Bundle data = new Bundle();

        data.putString(CrashpadConstants.ARGUMENT_CLIENT_DESCRIPTION,
                permanentConfigSerializer.serialize(apiKey, processConfiguration)
        );
        data.putString(CrashpadConstants.ARGUMENT_RUNTIME_CONFIG, runtimeConfig);

        data.putString(ARGUMENT_DUMP_DIR, folder);
        data.putString(ARGUMENT_HANDLER_PATH, extractorResult.pathToHandler);
        data.putBoolean(ARGUMENT_IS_64_BIT, extractorResult.is64bit);
        data.putBoolean(ARGUMENT_USE_LINKER, extractorResult.useLinker);
        data.putString(ARGUMENT_SOCKET_NAME, CrashpadConstants.getCrashpadNewCrashSocketName(context));
        if (extractorResult.appProcessConfig == null) {
            //if there is no required arguments to run java class via app_process,
            //try to use default initialization mechanism
            data.putBoolean(ARGUMENT_USE_APP_PROCESS, false);
        } else {
            data.putBoolean(ARGUMENT_USE_APP_PROCESS, true);
            data.putString(ARGUMENT_MAIN_CLASS, extractorResult.appProcessConfig.className);
            data.putString(ARGUMENT_APK_PATH, extractorResult.appProcessConfig.apkPath);
            data.putString(ARGUMENT_LIB_PATH, extractorResult.appProcessConfig.libPath);
            data.putString(ARGUMENT_DATA_DIRECTORY, extractorResult.appProcessConfig.dataDirectory);
        }
        return data;
    }
}
