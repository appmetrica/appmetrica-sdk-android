package io.appmetrica.analytics.modulesapi.internal.client

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleRegistry
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueContext
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade

interface ClientContext {

    val context: Context

    val moduleAdRevenueContext: ModuleAdRevenueContext

    val clientStorageProvider: ClientStorageProvider

    val internalClientModuleFacade: InternalClientModuleFacade

    val activityLifecycleRegistry: ActivityLifecycleRegistry

    val defaultExecutor: IHandlerExecutor
}
