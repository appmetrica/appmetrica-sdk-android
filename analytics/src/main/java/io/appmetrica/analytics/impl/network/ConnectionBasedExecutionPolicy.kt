package io.appmetrica.analytics.impl.network

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.impl.PhoneUtils
import io.appmetrica.analytics.impl.utils.ConnectionTypeProviderImpl
import io.appmetrica.analytics.impl.utils.IConnectionTypeProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.EnumSet

class ConnectionBasedExecutionPolicy(private val context: Context) : IExecutionPolicy {

    private val connectionTypeProvider: IConnectionTypeProvider = ConnectionTypeProviderImpl()
    private val forbiddenNetworkTypes: EnumSet<PhoneUtils.NetworkType> = EnumSet.of(
        PhoneUtils.NetworkType.OFFLINE
    )
    private val tag = "[ExecutionPolicyBasedOnConnection]"
    private val descriptionValue = "connection based"

    override fun description(): String = descriptionValue

    override fun canBeExecuted(): Boolean {
        val connectionType = connectionTypeProvider.getConnectionType(context)
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
