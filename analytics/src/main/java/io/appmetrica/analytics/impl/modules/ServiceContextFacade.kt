package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController
import io.appmetrica.analytics.coreapi.internal.crypto.CryptoProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrier
import io.appmetrica.analytics.coreapi.internal.servicecomponents.FirstExecutionConditionService
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider
import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeTypeProvider
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.modulesapi.internal.ExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.LocationServiceApi
import io.appmetrica.analytics.modulesapi.internal.ModuleLifecycleController
import io.appmetrica.analytics.modulesapi.internal.ServiceContext

internal class ServiceContextFacade(
    override val moduleLifecycleController: ModuleLifecycleController
) : ServiceContext {

    override val context
        get() = GlobalServiceLocator.getInstance().context

    override val networkContext = NetworkContextImpl(context)

    override val selfReporter = ModuleSelfReporterImpl()

    override val sdkEnvironmentProvider: SdkEnvironmentProvider
        get() = GlobalServiceLocator.getInstance().sdkEnvironmentHolder

    override val platformIdentifiers: PlatformIdentifiers
        get() = GlobalServiceLocator.getInstance().platformIdentifiers

    override val serviceWakeLock = ServiceWakeLockImpl(
        context,
        ServiceWakeLockBinder(AppMetricaServiceWakeLockIntentProvider())
    )

    override val storageProvider = StorageProviderImpl(
        context,
        GlobalServiceLocator.getInstance().servicePreferences,
        DatabaseStorageFactory.getInstance(context).storageForService
    )

    override val executorProvider: ExecutorProvider = ExecutorProviderImpl()

    override val locationServiceApi: LocationServiceApi
        get() = GlobalServiceLocator.getInstance().locationServiceApi

    override val applicationStateProvider: ApplicationStateProvider
        get() = GlobalServiceLocator.getInstance().applicationStateProvider

    override val chargeTypeProvider: ChargeTypeProvider
        get() = GlobalServiceLocator.getInstance().batteryInfoProvider

    override val dataSendingRestrictionController: DataSendingRestrictionController
        get() = GlobalServiceLocator.getInstance().dataSendingRestrictionController

    override val firstExecutionConditionService: FirstExecutionConditionService
        get() = GlobalServiceLocator.getInstance().firstExecutionConditionService

    override val activationBarrier: ActivationBarrier
        get() = GlobalServiceLocator.getInstance().activationBarrier

    override val cryptoProvider: CryptoProvider = CryptoProviderImpl()
}
