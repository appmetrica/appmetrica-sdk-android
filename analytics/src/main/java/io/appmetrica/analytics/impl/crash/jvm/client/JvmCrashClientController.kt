package io.appmetrica.analytics.impl.crash.jvm.client

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.crash.ApplicationCrashProcessorCreator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class JvmCrashClientController {

    private val tag = "[JvmCrashClientController]"

    private val applicationCrashProcessorCreator = ApplicationCrashProcessorCreator()
    private val crashProcessor = CrashProcessorComposite()
    private lateinit var crashProcessorInstaller: ThreadUncaughtExceptionHandlerInstaller

    private var technicalCrashConsumersRegistered = false
    private var mainCrashConsumerRegistered = false

    @Synchronized
    fun setUpCrashHandler() {
        if (this::crashProcessorInstaller.isInitialized) {
            DebugLogger.info(tag, "Already has been initialized")
            return
        }
        crashProcessorInstaller = ThreadUncaughtExceptionHandlerInstaller(
            AppMetricaUncaughtExceptionHandler(crashProcessor)
        )
        crashProcessorInstaller.install()
    }

    @Synchronized
    fun registerTechnicalCrashConsumers(context: Context, reporterFactoryProvider: IReporterFactoryProvider) {
        if (technicalCrashConsumersRegistered) {
            DebugLogger.info(tag, "Technical crash consumers have already been registered")
            return
        }

        DebugLogger.info(tag, "Register technical crash consumers")
        crashProcessor.register(
            ClientServiceLocator.getInstance().crashProcessorFactory.createCrashProcessors(
                context,
                reporterFactoryProvider
            )
        )
        technicalCrashConsumersRegistered = true
    }

    @Synchronized
    fun registerApplicationCrashConsumer(
        context: Context,
        reporterFactoryProvider: IReporterFactoryProvider,
        config: AppMetricaConfig
    ) {
        if (mainCrashConsumerRegistered) {
            DebugLogger.info(tag, "Application crash consumer has already been registered")
            return
        }

        DebugLogger.info(tag, "Register application crash consumer")
        crashProcessor.register(
            applicationCrashProcessorCreator.createCrashProcessor(
                context,
                config,
                reporterFactoryProvider
            )
        )
        mainCrashConsumerRegistered = true
    }

    @Synchronized
    fun clearCrashConsumers() {
        crashProcessor.clearAllCrashProcessors()
        technicalCrashConsumersRegistered = false
        mainCrashConsumerRegistered = false
    }
}
