package io.appmetrica.analytics.impl

import android.content.Context
import android.location.Location
import android.os.Bundle
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.AdvIdentifiersResult
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.DeferredDeeplinkListener
import io.appmetrica.analytics.DeferredDeeplinkParametersListener
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.StartupParamsCallback
import io.appmetrica.analytics.coreutils.internal.ApiKeyUtils.createPartialApiKey
import io.appmetrica.analytics.coreutils.internal.WrapUtils
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.AppMetricaInternalConfigExtractor.getClids
import io.appmetrica.analytics.impl.AppMetricaInternalConfigExtractor.getDistributionReferrer
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.impl.modules.ModulesSeeker
import io.appmetrica.analytics.impl.modules.client.context.ClientContextImpl
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper
import io.appmetrica.analytics.impl.referrer.common.Constants
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.impl.utils.BooleanUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.logger.appmetrica.internal.ImportantLogger
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

internal class AppMetricaImpl @WorkerThread internal constructor(
    private val context: Context,
    private val core: IAppMetricaCore
) : IAppMetricaImpl {

    private val tag = "[AppMetricaImpl]"

    private val clientPreferences: PreferencesClientDbStorage =
        ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context)
    private val startupHelper: StartupHelper
    private val referrerHelper: ReferrerHelper
    private val processConfiguration: ProcessConfiguration
    private val reporterFactory: ReporterFactory
    private val reportsHandler: ReportsHandler
    private val defaultOneShotMetricaConfig: DefaultOneShotMetricaConfig
    private val sessionsTrackingManager: SessionsTrackingManager

    @Volatile
    private var mainReporterApiConsumerProvider: MainReporterApiConsumerProvider? = null
    private val modulesSeeker = ModulesSeeker()
    private val anonymousConfigProvider: AppMetricaConfigForAnonymousActivationProvider
    private var clientConfigInstalled = false

    init {
        modulesSeeker.discoverClientModules()
        val clientServiceLocator = ClientServiceLocator.getInstance()
        clientServiceLocator.modulesController.initClientSide(ClientContextImpl())
        val fieldsProvider = AppMetricaImplFieldsProvider()
        val dataResultReceiver = fieldsProvider.createDataResultReceiver(core.defaultHandler, this)
        processConfiguration =
            fieldsProvider.createProcessConfiguration(context, dataResultReceiver)
        defaultOneShotMetricaConfig = clientServiceLocator.defaultOneShotConfig
        reportsHandler = fieldsProvider.createReportsHandler(
            processConfiguration,
            context,
            core.defaultExecutor
        )
        defaultOneShotMetricaConfig.setReportsHandler(reportsHandler)
        startupHelper = fieldsProvider.createStartupHelper(
            context,
            reportsHandler,
            clientPreferences,
            core.defaultHandler
        )
        reportsHandler.setStartupParamsProvider(startupHelper)
        referrerHelper = fieldsProvider.createReferrerHelper(
            reportsHandler,
            clientPreferences,
            core.defaultHandler
        )
        reporterFactory = fieldsProvider.createReporterFactory(
            context,
            processConfiguration,
            reportsHandler,
            core.defaultHandler,
            startupHelper
        )
        sessionsTrackingManager = clientServiceLocator.sessionsTrackingManager
        anonymousConfigProvider = AppMetricaConfigForAnonymousActivationProvider(clientPreferences)
    }

    @WorkerThread
    override fun activate(config: AppMetricaConfig) {
        DebugLogger.info(tag, "activate")
        val logger = LoggerStorage.getOrCreateMainPublicLogger(config.apiKey)
        val mainReporterInitializer = object : MainReporterInitializer {
            override fun initialize(): MainReporter {
                return reporterFactory.buildOrUpdateMainReporter(
                    config,
                    logger,
                    defaultOneShotMetricaConfig.wasAppEnvironmentCleared()
                )
            }
        }
        val activatedNow = activateCommonComponentsIfNotYet(
            logger,
            config,
            mainReporterInitializer
        )
        var configUpdated = false
        if (!activatedNow && !clientConfigInstalled) {
            updateConfig(logger, config)
            configUpdated = true
        }
        if (activatedNow || configUpdated) {
            clientPreferences.saveAppMetricaConfig(config)
        } else {
            logger.warning("AppMetrica SDK already has been activated")
        }
        if (activatedNow) {
            ImportantLogger.info(
                SdkUtils.APPMETRICA_TAG,
                "Activate AppMetrica with APIKey " + createPartialApiKey(config.apiKey)
            )
        }
        if (configUpdated) {
            ImportantLogger.info(
                SdkUtils.APPMETRICA_TAG,
                "Upgrade AppMetrica anonymous mode to normal with APIKey " + createPartialApiKey(config.apiKey)
            )
        }
        clientConfigInstalled = true
    }

    @WorkerThread
    override fun activateAnonymously() {
        DebugLogger.info(tag, "Activate anonymously")
        val config = anonymousConfigProvider.config
        val publicLogger = LoggerStorage.getMainPublicOrAnonymousLogger()
        val mainReporterInitializer = object : MainReporterInitializer {
            override fun initialize(): MainReporter {
                return reporterFactory.buildOrUpdateAnonymousMainReporter(
                    config,
                    publicLogger,
                    defaultOneShotMetricaConfig.wasAppEnvironmentCleared()
                )
            }
        }
        val activatedNow =
            activateCommonComponentsIfNotYet(publicLogger, config, mainReporterInitializer)
        if (activatedNow) {
            ImportantLogger.info(
                SdkUtils.APPMETRICA_TAG,
                "Activate AppMetrica in anonymous mode"
            )
        }
    }

    @AnyThread
    override fun getMainReporterApiConsumerProvider(): MainReporterApiConsumerProvider? {
        return mainReporterApiConsumerProvider
    }

    @AnyThread
    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        DebugLogger.info(tag, "On receive data, result code: %d", resultCode)
        startupHelper.processResultFromResultReceiver(resultData)
    }

    @WorkerThread
    override fun requestDeferredDeeplinkParameters(listener: DeferredDeeplinkParametersListener) {
        referrerHelper.requestDeferredDeeplinkParameters(listener)
    }

    @WorkerThread
    override fun requestDeferredDeeplink(listener: DeferredDeeplinkListener) {
        referrerHelper.requestDeferredDeeplink(listener)
    }

    @WorkerThread
    override fun activateReporter(config: ReporterConfig) {
        reporterFactory.activateReporter(config)
    }

    @WorkerThread
    override fun getReporter(config: ReporterConfig): IReporterExtended {
        return reporterFactory.getOrCreateReporter(config)
    }

    @AnyThread
    override fun getDeviceId(): String? {
        return startupHelper.deviceId
    }

    @AnyThread
    override fun getCachedAdvIdentifiers(): AdvIdentifiersResult {
        return startupHelper.cachedAdvIdentifiers
    }

    @AnyThread
    override fun getFeatures(): FeaturesResult {
        return startupHelper.features
    }

    @AnyThread
    override fun getClids(): Map<String, String>? {
        return startupHelper.clids
    }

    @WorkerThread
    override fun requestStartupParams(
        callback: StartupParamsCallback,
        params: List<String>
    ) {
        startupHelper.requestStartupParams(callback, params, processConfiguration.clientClids)
    }

    @WorkerThread
    private fun activateCommonComponentsIfNotYet(
        publicLogger: PublicLogger,
        config: AppMetricaConfig,
        mainReporterInitializer: MainReporterInitializer
    ): Boolean {
        return if (null == mainReporterApiConsumerProvider) {
            DebugLogger.info(
                tag,
                "Activate common components"
            )
            updateConfig(publicLogger, config)
            referrerHelper.maybeRequestReferrer()
            initMainReporterConsumerProvider(mainReporterInitializer, config)
            true
        } else {
            mainReporterInitializer.initialize()
            DebugLogger.info(
                tag,
                "Common components has already been activated"
            )
            false
        }
    }

    @WorkerThread
    private fun updateConfig(logger: PublicLogger, config: AppMetricaConfig) {
        DebugLogger.info(tag, "updateConfig")
        updatePublicLoggerState(config, logger)
        updateCrashHandlerState(config, logger)
        updateSessionAutoTrackingState(config)
        processConfiguration.update(config)
        startupHelper.setPublicLogger(logger)
        initStartup(config)
        reportsHandler.updatePreActivationConfig(config.locationTracking, config.dataSendingEnabled)
        startupHelper.sendStartupIfNeeded()
    }

    @WorkerThread
    private fun updatePublicLoggerState(config: AppMetricaConfig, logger: PublicLogger) {
        if (BooleanUtils.isTrue(config.logs)) {
            logger.setEnabled(true)
            PublicLogger.getAnonymousInstance().setEnabled(true)
        } else {
            logger.setEnabled(false)
            PublicLogger.getAnonymousInstance().setEnabled(false)
        }
    }

    @WorkerThread
    private fun updateCrashHandlerState(appMetricaConfig: AppMetricaConfig, logger: PublicLogger) {
        val crashReportingEnabled = WrapUtils.getOrDefault(
            appMetricaConfig.crashReporting,
            DefaultValuesForCrashReporting.DEFAULT_REPORTS_CRASHES_ENABLED
        )
        if (crashReportingEnabled) {
            core.jvmCrashClientController.registerApplicationCrashConsumer(
                context,
                this,
                appMetricaConfig
            )
            core.jvmCrashClientController.registerTechnicalCrashConsumers(context, this)
            logger.info("Register application crash handler")
        } else {
            core.jvmCrashClientController.clearCrashConsumers()
            logger.info("Disable all crash handlers")
        }
    }

    @WorkerThread
    private fun updateSessionAutoTrackingState(config: AppMetricaConfig) {
        DebugLogger.info(
            tag,
            "updateSessionAutoTrackingState: sessionAutoTrackingEnabled = ${config.sessionAutoTrackingEnabled()}"
        )
        if (config.sessionAutoTrackingEnabled()) {
            sessionsTrackingManager.startWatchingIfNotYet()
        } else {
            sessionsTrackingManager.stopWatchingIfHasAlreadyBeenStarted()
        }
    }

    @WorkerThread
    private fun initMainReporterConsumerProvider(
        creator: MainReporterInitializer,
        config: AppMetricaConfig
    ) {
        val reporter = creator.initialize()
        mainReporterApiConsumerProvider = MainReporterApiConsumerProvider(reporter).also {
            core.appOpenWatcher.setDeeplinkConsumer(it.deeplinkConsumer)
        }
        sessionsTrackingManager.setReporter(reporter)
    }

    @WorkerThread
    private fun initStartup(config: AppMetricaConfig) {
        startupHelper.setCustomHosts(config.customHosts)
        startupHelper.clids = getClids(config)
        val distributionReferrer = getDistributionReferrer(config)
        startupHelper.setDistributionReferrer(distributionReferrer)
        if (distributionReferrer != null) {
            startupHelper.setInstallReferrerSource(Constants.INSTALL_REFERRER_SOURCE_API)
        }
    }

    @WorkerThread
    override fun setLocation(location: Location?) {
        mainReporter.setLocation(location)
    }

    @WorkerThread
    override fun setLocationTracking(enabled: Boolean) {
        mainReporter.setLocationTracking(enabled)
    }

    @WorkerThread
    override fun setDataSendingEnabled(value: Boolean) {
        mainReporter.setDataSendingEnabled(value)
    }

    @WorkerThread
    override fun putAppEnvironmentValue(key: String, value: String) {
        mainReporter.putAppEnvironmentValue(key, value)
    }

    @WorkerThread
    override fun clearAppEnvironment() {
        mainReporter.clearAppEnvironment()
    }

    @WorkerThread
    override fun putErrorEnvironmentValue(key: String, value: String) {
        mainReporter.putErrorEnvironmentValue(key, value)
    }

    @WorkerThread
    override fun setUserProfileID(userProfileID: String?) {
        mainReporter.setUserProfileID(userProfileID)
    }

    @AnyThread
    override fun getReporterFactory(): ReporterFactory {
        return reporterFactory
    }

    private val mainReporter: IMainReporter
        get() = mainReporterApiConsumerProvider!!.mainReporter

    private fun AppMetricaConfig.sessionAutoTrackingEnabled() =
        WrapUtils.getOrDefault(sessionsAutoTrackingEnabled, DefaultValues.DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED)
}
