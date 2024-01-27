package io.appmetrica.analytics.modulesapi.internal

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeTypeProvider

interface ServiceContext {

    val context: Context

    val moduleLifecycleController: ModuleLifecycleController

    val storageProvider: StorageProvider

    val networkContext: NetworkContext

    val executorProvider: ExecutorProvider

    val selfReporter: ModuleSelfReporter

    val sdkEnvironmentProvider: SdkEnvironmentProvider

    val platformIdentifiers: PlatformIdentifiers

    val serviceWakeLock: ServiceWakeLock

    val locationServiceApi: LocationServiceApi

    val applicationStateProvider: ApplicationStateProvider

    val chargeTypeProvider: ChargeTypeProvider

    val dataSendingRestrictionController: DataSendingRestrictionController
}
