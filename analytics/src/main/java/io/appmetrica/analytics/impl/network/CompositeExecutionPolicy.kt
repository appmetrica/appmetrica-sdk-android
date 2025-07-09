package io.appmetrica.analytics.impl.network

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class CompositeExecutionPolicy(private vararg val policies: IExecutionPolicy) : IExecutionPolicy {

    private val tag = "[CompositeExecutionPolicy]"

    private val descriptionValue = "Composite of {${policies.joinToString(", ") { it.description() }}}"

    override fun description(): String = descriptionValue

    override fun canBeExecuted(): Boolean {
        if (policies.isEmpty()) {
            DebugLogger.warning(tag, "Policies are empty.")
            return false
        }

        val forbiddenPolicy = policies.find { !it.canBeExecuted() }
        if (forbiddenPolicy != null) {
            DebugLogger.info(tag, "forbid by policy: ${forbiddenPolicy.description()}}")
            return false
        }

        return true
    }
}
