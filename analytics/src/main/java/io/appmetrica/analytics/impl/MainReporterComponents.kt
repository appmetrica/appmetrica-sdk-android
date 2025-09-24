package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter
import io.appmetrica.analytics.impl.crash.jvm.converter.AnrConverter
import io.appmetrica.analytics.impl.crash.jvm.converter.CustomErrorConverter
import io.appmetrica.analytics.impl.crash.jvm.converter.RegularErrorConverter
import io.appmetrica.analytics.impl.crash.jvm.converter.UnhandledExceptionConverter
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashClient
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoWrapper
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.CounterConfigurationReporterType
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

internal class MainReporterComponents(
    val context: Context,
    val reporterFactoryProvider: IReporterFactoryProvider,
    val processConfiguration: ProcessConfiguration,
    val reportsHandler: ReportsHandler,
    val startupHelper: StartupHelper,
) {

    val nativeCrashClient = NativeCrashClient(processConfiguration)

    val extraMetaInfoRetriever = ExtraMetaInfoRetriever(context)

    val errorEnvironment = ErrorEnvironment(
        SimpleMapLimitation(LoggerStorage.getMainPublicOrAnonymousLogger(), ErrorEnvironment.TAG)
    )

    val reporterEnvironment = ReporterEnvironment(
        processConfiguration,
        CounterConfiguration(CounterConfigurationReporterType.MAIN),
        errorEnvironment
    )

    val appStatusMonitor = AppStatusMonitor()

    val processDetector = ClientServiceLocator.getInstance().processNameProvider

    val activityStateManager = ActivityStateManager()

    val pluginErrorDetailsConverter = PluginErrorDetailsConverter(extraMetaInfoRetriever)

    val unhandledExceptionConverter = UnhandledExceptionConverter()

    val regularErrorConverter = RegularErrorConverter()

    val customErrorConverter = CustomErrorConverter()

    val anrConverter = AnrConverter()

    fun updateAnonymousConfig(config: AppMetricaConfig, logger: PublicLogger) {
        logger.info("Update anonymous config with value ${config.toJson()}")
        reporterEnvironment.reporterConfiguration.applyFromAnonymousConfig(config)
    }

    fun updateConfig(config: AppMetricaConfig, logger: PublicLogger) {
        logger.info("Update config with value ${config.toJson()}")
        reporterEnvironment.reporterConfiguration.applyFromConfig(config)
        reporterEnvironment.initialUserProfileID = config.userProfileID
        reporterEnvironment.preloadInfoWrapper = PreloadInfoWrapper(
            config.preloadInfo,
            logger,
            AppMetricaInternalConfigExtractor.getPreloadInfoAutoTracking(config)
                ?: DefaultValues.DEFAULT_AUTO_PRELOAD_INFO_DETECTION
        )
        logger.info(
            "Actual session timeout is ${config.sessionTimeout ?: DefaultValues.DEFAULT_SESSION_TIMEOUT_SECONDS}"
        )
    }
}
