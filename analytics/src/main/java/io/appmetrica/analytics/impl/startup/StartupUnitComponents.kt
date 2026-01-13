package io.appmetrica.analytics.impl.startup

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.ClidsInfoStorage
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommutationComponentId
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider
import io.appmetrica.analytics.impl.startup.uuid.UuidValidator
import io.appmetrica.analytics.impl.utils.DeviceIdGenerator

internal class StartupUnitComponents(
    val context: Context,
    val packageName: String,
    val requestConfigArguments: StartupRequestConfig.Arguments,
    val resultListener: StartupResultListener
) {

    val startupStateHolder = GlobalServiceLocator.getInstance().startupStateHolder
    private val startupState = startupStateHolder.getStartupState()

    val componentId: ComponentId = CommutationComponentId(packageName)
    val startupStateStorage: StartupState.Storage = StartupState.Storage(context)
    val deviceIdGenerator = DeviceIdGenerator()
    val timeProvider: TimeProvider = SystemTimeProvider()
    val clidsStorage: ClidsInfoStorage = GlobalServiceLocator.getInstance().clidsStorage
    val clidsStateChecker = ClidsStateChecker()

    val startupConfigurationHolder = StartupConfigurationHolder(
        StartupRequestConfig.Loader(context, packageName),
        startupState,
        requestConfigArguments
    )

    val multiProcessSafeUuidProvider: MultiProcessSafeUuidProvider =
        GlobalServiceLocator.getInstance().multiProcessSafeUuidProvider

    val uuidValidator = UuidValidator()
}
