package io.appmetrica.analytics.impl;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ReporterFactory implements IReporterFactory {

    private static final String TAG = "[ReporterFactory]";

    @NonNull
    private final Context context;
    @NonNull
    private final ProcessConfiguration processConfiguration;
    @NonNull
    private final ReportsHandler reportsHandler;
    @NonNull
    private final Handler tasksHandler;
    @NonNull
    private final StartupHelper startupHelper;
    @NonNull
    private final Map<String, IReporterExtended> reporters = new HashMap<>();
    @NonNull
    private final Validator<String> reporterApiKeyUsedValidator = new ThrowIfFailedValidator<>(
        new ReporterKeyIsUsedValidator(reporters)
    );
    @NonNull
    private final List<String> apiKeysToIgnoreStartup = Arrays.asList(
        SdkData.SDK_API_KEY_UUID,
        SdkData.SDK_API_KEY_PUSH_SDK
    );

    public ReporterFactory(@NonNull Context context,
                           @NonNull ProcessConfiguration processConfiguration,
                           @NonNull ReportsHandler reportsHandler,
                           @NonNull Handler tasksHandler,
                           @NonNull StartupHelper startupHelper) {
        this.context = context;
        this.processConfiguration = processConfiguration;
        this.reportsHandler = reportsHandler;
        this.tasksHandler = tasksHandler;
        this.startupHelper = startupHelper;
    }

    @NonNull
    @WorkerThread
    @Override
    public MainReporter buildMainReporter(
        @NonNull AppMetricaConfig config,
        final boolean needToClearEnvironment
    ) {
        reporterApiKeyUsedValidator.validate(config.apiKey);

        final MainReporter mainReporter = new MainReporter(
            context,
            processConfiguration,
            config, reportsHandler,
            startupHelper,
            new UnhandledSituationReporterProvider(this, SdkData.SDK_API_KEY_UUID),
            new UnhandledSituationReporterProvider(this, SdkData.SDK_API_KEY_PUSH_SDK)
        );

        performCommonInitialization(mainReporter);
        mainReporter.updateConfig(config, needToClearEnvironment);
        mainReporter.start();

        reportsHandler.setShouldDisconnectFromServiceChecker(
            new ShouldDisconnectFromServiceChecker() {
                @Override
                public boolean shouldDisconnect() {
                    return mainReporter.isPaused();
                }
            }
        );

        reporters.put(config.apiKey, mainReporter);

        return mainReporter;
    }

    @Override
    public synchronized void activateReporter(@NonNull ReporterConfig config) {
        if (reporters.containsKey(config.apiKey)) {
            PublicLogger logger = LoggerStorage.getOrCreatePublicLogger(config.apiKey);
            if (logger.isEnabled()) {
                logger.fw("Reporter with apiKey=%s already exists.", config.apiKey);
            }
        } else {
            getOrCreateReporter(config);
            Log.i(
                SdkUtils.APPMETRICA_TAG,
                "Activate reporter with APIKey " + Utils.createPartialApiKey(config.apiKey)
            );
        }
    }

    @Override
    @NonNull
    public synchronized IReporterExtended getOrCreateReporter(@NonNull ReporterConfig config) {
        IReporterExtended reporter = reporters.get(config.apiKey);

        if (reporter == null) {
            if (!apiKeysToIgnoreStartup.contains(config.apiKey)) {
                startupHelper.sendStartupIfNeeded();
            }
            ManualReporter manualReporter = new ManualReporter(
                context,
                processConfiguration,
                config,
                reportsHandler
            );
            performCommonInitialization(manualReporter);
            manualReporter.start();
            reporter = manualReporter;
            reporters.put(config.apiKey, reporter);
        }

        return reporter;
    }

    @Override
    @NonNull
    public synchronized IUnhandledSituationReporter getMainOrCrashReporter(@NonNull AppMetricaConfig config) {
        IReporterExtended reporter = reporters.get(config.apiKey);
        if (reporter == null) {
            DebugLogger.info(TAG, "No main reporter - will create crash reporter");
            CrashReporter crashReporter = new CrashReporter(
                context,
                processConfiguration,
                config,
                reportsHandler
            );
            performCommonInitialization(crashReporter);
            crashReporter.updateConfig(config);
            crashReporter.start();
            reporter = crashReporter;
        }
        return reporter;
    }

    private void performCommonInitialization(@NonNull BaseReporter reporter) {
        reporter.setKeepAliveHandler(new KeepAliveHandler(tasksHandler, reporter));
        reporter.setStartupParamsProvider(startupHelper);
    }

    @NonNull
    @Override
    public ReporterFactory getReporterFactory() {
        return this;
    }
}
