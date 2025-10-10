package io.appmetrica.analytics.modulesapi.internal.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController
import io.appmetrica.analytics.coreapi.internal.crypto.CryptoProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrier
import io.appmetrica.analytics.coreapi.internal.servicecomponents.FirstExecutionConditionService
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentLifecycle
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeTypeProvider
import io.appmetrica.analytics.coreapi.internal.system.ActiveNetworkTypeProvider
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.modulesapi.internal.common.ExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.common.ModuleSelfReporter

interface ServiceContext {

    val context: Context

    val moduleServiceLifecycleController: ModuleServiceLifecycleController

    val serviceStorageProvider: ServiceStorageProvider

    val networkContext: ServiceNetworkContext

    val executorProvider: ExecutorProvider

    val selfReporter: ModuleSelfReporter

    val sdkEnvironmentProvider: SdkEnvironmentProvider

    val platformIdentifiers: PlatformIdentifiers

    val serviceWakeLock: ServiceWakeLock

    val locationServiceApi: LocationServiceApi

    val applicationStateProvider: ApplicationStateProvider

    val chargeTypeProvider: ChargeTypeProvider

    val dataSendingRestrictionController: DataSendingRestrictionController

    val firstExecutionConditionService: FirstExecutionConditionService

    val activationBarrier: ActivationBarrier

    val cryptoProvider: CryptoProvider

    val permissionExtractor: PermissionExtractor

    val serviceModuleReporterComponentLifecycle: ServiceModuleReporterComponentLifecycle

    val activeNetworkTypeProvider: ActiveNetworkTypeProvider
}
