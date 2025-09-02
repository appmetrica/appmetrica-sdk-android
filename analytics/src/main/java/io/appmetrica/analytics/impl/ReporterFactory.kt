package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Handler
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.coreutils.internal.ApiKeyUtils
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.reporter.MainReporterContext
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator
import io.appmetrica.analytics.impl.utils.validation.Validator
import io.appmetrica.analytics.impl.utils.validation.api.ReporterKeyIsUsedValidator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.logger.appmetrica.internal.ImportantLogger
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

internal class ReporterFactory(
    private val context: Context,
    private val processConfiguration: ProcessConfiguration,
    private val reportsHandler: ReportsHandler,
    private val tasksHandler: Handler,
    private val startupHelper: StartupHelper
) : IReporterFactory {
    private val tag = "[ReporterFactory]"

    private val mainReporterComponents = MainReporterComponents(
        context,
        this,
        processConfiguration,
        reportsHandler,
        startupHelper
    )

    private val reporters = mutableMapOf<String, IReporterExtended?>()

    private val reporterApiKeyUsedValidator: Validator<String> = ThrowIfFailedValidator(
        ReporterKeyIsUsedValidator(reporters)
    )
    private val apiKeysToIgnoreStartup = listOf(
        SdkData.SDK_API_KEY_UUID,
        SdkData.SDK_API_KEY_PUSH_SDK
    )
    private var mainReporter: MainReporter? = null

    private var unhandledSituationReporter: CrashReporter? = null

    @Synchronized
    override fun buildOrUpdateAnonymousMainReporter(
        config: AppMetricaConfig,
        logger: PublicLogger,
        configExtension: AppMetricaConfigExtension,
    ): MainReporter = mainReporter?.also {
        DebugLogger.info(tag, "Create anonymous main reporter")
    } ?: createMainReporter(config, logger, configExtension).also {
        mainReporter = it
        DebugLogger.info(tag, "Create anonymous main reporter")
    }

    @WorkerThread
    @Synchronized
    override fun buildOrUpdateMainReporter(
        config: AppMetricaConfig,
        logger: PublicLogger,
        configExtension: AppMetricaConfigExtension,
    ): MainReporter = mainReporter?.also {
        DebugLogger.info(
            tag,
            "Main reporter already exists. Update configuration with apiKey = %s",
            config.apiKey
        )
        mainReporterComponents.updateConfig(config, logger)
        it.updateConfig(config, configExtension)
        notifyMainReporterCreated(it, config, logger)
        reporters[config.apiKey] = it
    } ?: createMainReporter(config, logger, configExtension).also {
        DebugLogger.info(tag, "Create main reporter with apiKey = %s", config.apiKey)
        notifyMainReporterCreated(it, config, logger)
        mainReporter = it
    }

    @Synchronized
    override fun activateReporter(config: ReporterConfig) {
        if (reporters.containsKey(config.apiKey)) {
            val logger = LoggerStorage.getOrCreatePublicLogger(config.apiKey)
            logger.warning("Reporter with apiKey=%s already exists.", ApiKeyUtils.createPartialApiKey(config.apiKey))
        } else {
            getOrCreateReporter(config)
            ImportantLogger.info(
                SdkUtils.APPMETRICA_TAG,
                "Activate reporter with APIKey " + ApiKeyUtils.createPartialApiKey(config.apiKey)
            )
        }
    }

    @Synchronized
    override fun getOrCreateReporter(config: ReporterConfig): IReporterExtended {
        var reporter = reporters[config.apiKey]
        if (reporter == null) {
            DebugLogger.info(tag, "Create reporter for API_KEY = %s", config.apiKey)
            if (!apiKeysToIgnoreStartup.contains(config.apiKey)) {
                startupHelper.sendStartupIfNeeded()
            }
            val manualReporter = ManualReporter(
                context,
                processConfiguration,
                config,
                reportsHandler
            )
            performCommonInitialization(manualReporter)
            manualReporter.start()
            reporter = manualReporter
            reporters[config.apiKey] = reporter
        }
        return reporter
    }

    @Synchronized
    override fun getUnhandhedSituationReporter(config: AppMetricaConfig): IUnhandledSituationReporter {
        return unhandledSituationReporter?.apply {
            DebugLogger.info(tag, "Update config for crash reporter")
            updateConfig(config)
        } ?: let {
            DebugLogger.info(tag, "Create new unhandled situation reporter")
            val fieldsProvider = CrashReporterFieldsProvider(
                processConfiguration,
                mainReporterComponents.errorEnvironment,
                reportsHandler,
                config
            )
            CrashReporter(fieldsProvider)
        }.also { unhandledSituationReporter = it }
    }

    private fun createMainReporter(
        config: AppMetricaConfig,
        logger: PublicLogger,
        configExtension: AppMetricaConfigExtension,
    ): MainReporter {
        reporterApiKeyUsedValidator.validate(config.apiKey)
        mainReporterComponents.updateConfig(config, logger)
        val reporter = MainReporter(mainReporterComponents)
        DebugLogger.info(tag, "performCommonInitialization for main reporter with api key = %s", config.apiKey)
        performCommonInitialization(reporter)
        DebugLogger.info(tag, "updateConfig for apiKey = %s", config.apiKey)
        reporter.updateConfig(config, configExtension)
        reporter.start()
        reportsHandler.setShouldDisconnectFromServiceChecker(
            object : ShouldDisconnectFromServiceChecker {
                override fun shouldDisconnect(): Boolean = reporter.isPaused
            }
        )
        reporters[config.apiKey] = reporter
        return reporter
    }

    private fun notifyMainReporterCreated(
        reporter: MainReporter,
        config: AppMetricaConfig,
        logger: PublicLogger
    ) {
        val listener = ClientServiceLocator.getInstance().reporterLifecycleListener
        if (listener == null) {
            DebugLogger.info(tag, "ignore notifyMainReporterCreated: listener is null")
            return
        }
        DebugLogger.info(tag, "notifyMainReporterCreated")
        val mainReporterContext = MainReporterContext(
            mainReporterComponents,
            config,
            logger
        )
        listener.onCreateMainReporter(mainReporterContext, reporter)
    }

    private fun performCommonInitialization(reporter: BaseReporter) {
        reporter.setKeepAliveHandler(KeepAliveHandler(tasksHandler, reporter))
        reporter.setStartupParamsProvider(startupHelper)
    }

    override fun getReporterFactory(): ReporterFactory {
        return this
    }
}
