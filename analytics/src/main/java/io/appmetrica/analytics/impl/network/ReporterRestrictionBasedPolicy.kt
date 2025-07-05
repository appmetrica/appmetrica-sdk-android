package io.appmetrica.analytics.impl.network

import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy

class ReporterRestrictionBasedPolicy(
    private val restrictionController: DataSendingRestrictionController,
) : IExecutionPolicy {

    private val descriptionValue = "data restriction based"

    override fun description(): String = descriptionValue

    override fun canBeExecuted(): Boolean = !restrictionController.isRestrictedForSdk
}
