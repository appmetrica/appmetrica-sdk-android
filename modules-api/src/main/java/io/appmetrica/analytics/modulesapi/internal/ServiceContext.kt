package io.appmetrica.analytics.modulesapi.internal

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController
import io.appmetrica.analytics.coreapi.internal.identifiers.SimpleAdvertisingIdGetter
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeTypeProvider
import io.appmetrica.analytics.coreapi.internal.system.LocaleProvider

interface ServiceContext {

    val context: Context

    val moduleLifecycleController: ModuleLifecycleController

    val storageProvider: StorageProvider

    val networkContext: NetworkContext

    val executorProvider: ExecutorProvider

    val selfReporter: ModuleSelfReporter

    val advertisingIdGetter: SimpleAdvertisingIdGetter

    val serviceWakeLock: ServiceWakeLock

    val locationServiceApi: LocationServiceApi

    val applicationStateProvider: ApplicationStateProvider

    val chargeTypeProvider: ChargeTypeProvider

    val dataSendingRestrictionController: DataSendingRestrictionController

    val localeProvider: LocaleProvider
}
