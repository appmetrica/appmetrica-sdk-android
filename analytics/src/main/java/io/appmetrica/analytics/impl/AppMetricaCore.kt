package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Handler
import androidx.annotation.AnyThread
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.clientcomponents.ClientComponentsInitializerProvider
import io.appmetrica.analytics.impl.crash.jvm.client.JvmCrashClientController
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.logger.common.BaseReleaseLogger

@AnyThread
internal class AppMetricaCore(
    private val context: Context,
    executorProvider: ClientExecutorProvider
) : IAppMetricaCore {

    private val tag = "[AppMetricaCore]"

    override val defaultExecutor: IHandlerExecutor = executorProvider.defaultExecutor
    override val clientTimeTracker = ClientTimeTracker()
    override val defaultHandler: Handler = defaultExecutor.handler
    override val appOpenWatcher = AppOpenWatcher()
    override val jvmCrashClientController = JvmCrashClientController()

    init {
        BaseReleaseLogger.init(context)
        defaultExecutor.execute { SdkUtils.logSdkInfo() }
        clientTimeTracker.trackCoreCreation()
        ClientComponentsInitializerProvider()
            .getClientComponentsInitializer()
            .onCreate()
    }

    @Synchronized
    override fun activate(
        config: AppMetricaConfig?,
        reporterFactoryProvider: IReporterFactoryProvider
    ) {
        DebugLogger.info(tag, "activate with config: ${config?.toJson()?.toString()}")
        if (!AppMetricaFacade.isFullyInitialized()) {
            if (config.shouldCollectCrashes()) {
                jvmCrashClientController.setUpCrashHandler()
                jvmCrashClientController.registerTechnicalCrashConsumers(context, reporterFactoryProvider)
                config?.let {
                    jvmCrashClientController.registerApplicationCrashConsumer(context, reporterFactoryProvider, it)
                }
            } else {
                jvmCrashClientController.clearCrashConsumers()
            }
            if (config.shouldTrackAppOpen()) {
                appOpenWatcher.startWatching()
            } else {
                appOpenWatcher.stopWatching()
            }
            // Config is null means anonymous activation
            if (config != null) {
                AppMetricaFacade.markFullyInitialized()
            }
        } else {
            DebugLogger.info(tag, "Full activation already has been completed")
        }
    }

    private fun AppMetricaConfig?.shouldTrackAppOpen(): Boolean = this?.let {
        it.appOpenTrackingEnabled ?: DefaultValues.DEFAULT_APP_OPEN_TRACKING_ENABLED
    } ?: DefaultValues.DEFAULT_APP_OPEN_TRACKING_ENABLED_FOR_ANONYMOUS_ACTIVATION

    private fun AppMetricaConfig?.shouldCollectCrashes(): Boolean = this?.let {
        it.crashReporting ?: DefaultValuesForCrashReporting.DEFAULT_REPORTS_CRASHES_ENABLED
    } ?: DefaultValuesForCrashReporting.DEFAULT_REPORT_CRASHES_ENABLED_FOR_ANONYMOUS_ACTIVATION
}
