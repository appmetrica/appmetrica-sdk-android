package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Handler
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.impl.modules.ModuleStatusReporter
import io.appmetrica.analytics.impl.referrer.client.ReferrerHelper
import io.appmetrica.analytics.impl.startup.StartupHelper

internal class AppMetricaImplFieldsProvider {
    fun createDataResultReceiver(handler: Handler, appMetrica: AppMetricaImpl): DataResultReceiver =
        DataResultReceiver(handler, appMetrica)

    fun createProcessConfiguration(
        context: Context,
        dataResultReceiver: DataResultReceiver
    ): ProcessConfiguration = ProcessConfiguration(context, dataResultReceiver)

    fun createReportsHandler(
        processConfiguration: ProcessConfiguration,
        context: Context,
        executor: ICommonExecutor
    ): ReportsHandler = ReportsHandler(processConfiguration, context, executor)

    fun createStartupHelper(
        context: Context,
        reportsHandler: ReportsHandler,
        clientPreferences: PreferencesClientDbStorage,
        handler: Handler
    ): StartupHelper = StartupHelper(context, reportsHandler, clientPreferences, handler)

    fun createReferrerHelper(
        reportsHandler: ReportsHandler,
        clientPreferences: PreferencesClientDbStorage,
        handler: Handler
    ): ReferrerHelper = ReferrerHelper(reportsHandler, clientPreferences, handler)

    fun createReporterFactory(
        context: Context,
        processConfiguration: ProcessConfiguration,
        reportsHandler: ReportsHandler,
        handler: Handler,
        startupHelper: StartupHelper
    ): ReporterFactory = ReporterFactory(context, processConfiguration, reportsHandler, handler, startupHelper)

    fun createModuleStatusReporter(context: Context): ModuleStatusReporter = ModuleStatusReporter(
        executor = ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor,
        preferences = ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context),
        modulesType = "client_modules",
    )
}
