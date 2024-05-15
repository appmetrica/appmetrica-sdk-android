package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.impl.utils.validation.api.ReporterKeyIsUsedValidator;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporterFactory implements IReporterFactory {

    private static final String TAG = "[ReporterFactory]";

    @NonNull private Context mContext;
    @NonNull private ProcessConfiguration mProcessConfiguration;
    @NonNull private ReportsHandler mReportsHandler;
    @NonNull private Handler mTasksHandler;
    @NonNull private StartupHelper mStartupHelper;

    private Map<String, IReporterExtended> mReporters = new HashMap<String, IReporterExtended>();
    private final Validator<String> mReporterApiKeyUsedValidator = new ThrowIfFailedValidator<String>(
            new ReporterKeyIsUsedValidator(mReporters)
    );
    private final List<String> mApiKeysToIgnoreStartup = Arrays.asList(
            SdkData.SDK_API_KEY_UUID,
            SdkData.SDK_API_KEY_PUSH_SDK
    );

    public ReporterFactory(@NonNull Context context,
                           @NonNull ProcessConfiguration processConfiguration,
                           @NonNull ReportsHandler reportsHandler,
                           @NonNull Handler tasksHandler,
                           @NonNull StartupHelper startupHelper) {
        mContext = context;
        mProcessConfiguration = processConfiguration;
        mReportsHandler = reportsHandler;
        mTasksHandler = tasksHandler;
        mStartupHelper = startupHelper;
    }

    @NonNull
    @WorkerThread
    @Override
    public MainReporter buildMainReporter(@NonNull AppMetricaConfig config,
                                          final boolean needToClearEnvironment) {
        mReporterApiKeyUsedValidator.validate(config.apiKey);

        final MainReporter mainReporter = new MainReporter(
                mContext,
                mProcessConfiguration,
                config, mReportsHandler,
                mStartupHelper,
                new UnhandledSituationReporterProvider(this, SdkData.SDK_API_KEY_UUID),
                new UnhandledSituationReporterProvider(this, SdkData.SDK_API_KEY_PUSH_SDK)
        );

        performCommonInitialization(mainReporter);
        mainReporter.updateConfig(config, needToClearEnvironment);
        mainReporter.start();

        mReportsHandler.setShouldDisconnectFromServiceChecker(new ShouldDisconnectFromServiceChecker() {
            @Override
            public boolean shouldDisconnect() {
                return mainReporter.isPaused();
            }
        });

        mReporters.put(config.apiKey, mainReporter);

        return mainReporter;
    }

    @Override
    public synchronized void activateReporter(@NonNull ReporterConfig config) {
        if (mReporters.containsKey(config.apiKey)) {
            PublicLogger logger = LoggerStorage.getOrCreatePublicLogger(config.apiKey);
            if (logger.isEnabled()) {
                logger.fw("Reporter with apiKey=%s already exists.", config.apiKey);
            }
        } else {
            getOrCreateReporter(config);
            Log.i(SdkUtils.APPMETRICA_TAG, "Activate reporter with APIKey " + Utils.createPartialApiKey(config.apiKey));
        }
    }

    @Override
    @NonNull
    public synchronized IReporterExtended getOrCreateReporter(@NonNull ReporterConfig config) {
        IReporterExtended reporter = mReporters.get(config.apiKey);

        if (reporter == null) {
            if (mApiKeysToIgnoreStartup.contains(config.apiKey) == false) {
                mStartupHelper.sendStartupIfNeeded();
            }
            ManualReporter manualReporter = new ManualReporter(mContext,
                    mProcessConfiguration,
                    config,
                    mReportsHandler);
            performCommonInitialization(manualReporter);
            manualReporter.start();
            reporter = manualReporter;
            mReporters.put(config.apiKey, reporter);
        }

        return reporter;
    }

    @Override
    @NonNull
    public synchronized IUnhandledSituationReporter getMainOrCrashReporter(
            @NonNull AppMetricaConfig config) {
        IReporterExtended reporter = mReporters.get(config.apiKey);
        if (reporter == null) {
            DebugLogger.info(TAG, "No main reporter - will create crash reporter");
            CrashReporter crashReporter = new CrashReporter(
                    mContext,
                    mProcessConfiguration,
                    config,
                    mReportsHandler
            );
            performCommonInitialization(crashReporter);
            crashReporter.updateConfig(config);
            crashReporter.start();
            reporter = crashReporter;
        }
        return reporter;
    }

    private void performCommonInitialization(@NonNull BaseReporter reporter) {
        reporter.setKeepAliveHandler(new KeepAliveHandler(mTasksHandler, reporter));
        reporter.setStartupParamsProvider(mStartupHelper);
    }

    @NonNull
    @Override
    public ReporterFactory getReporterFactory() {
        return this;
    }
}
