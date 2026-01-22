package io.appmetrica.analytics.impl.network

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.coreapi.internal.system.ActiveNetworkTypeProvider
import io.appmetrica.analytics.coreapi.internal.system.NetworkType
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.EnumSet

internal class ConnectionBasedExecutionPolicy(private val context: Context) : IExecutionPolicy {

    private val activeNetworkTypeProvider: ActiveNetworkTypeProvider =
        GlobalServiceLocator.getInstance().activeNetworkTypeProvider

    private val forbiddenNetworkTypes: EnumSet<NetworkType> = EnumSet.of(
        NetworkType.OFFLINE
    )

    private val tag = "[ExecutionPolicyBasedOnConnection]"
    private val descriptionValue = "connection based"

    override fun description(): String = descriptionValue

    override fun canBeExecuted(): Boolean {
        val connectionType = activeNetworkTypeProvider.getNetworkType(context)
        val canBeExecuted = connectionType !in forbiddenNetworkTypes
        DebugLogger.info(
            tag,
            "can request executed on network with type %s? %b",
            connectionType.toString(),
            canBeExecuted
        )
        return canBeExecuted
    }
}
