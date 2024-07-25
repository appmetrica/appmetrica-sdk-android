package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.DistributionPriorityProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class PreloadInfoPriorityProvider : DistributionPriorityProvider<PreloadInfoState>() {

    private val tag = "[PreloadInfoPriorityProvider]"

    override fun isNewDataMoreImportant(newData: PreloadInfoState, oldData: PreloadInfoState): Boolean {
        if (!newData.wasSet) {
            return false
        }
        if (!oldData.wasSet) {
            return true
        }
        return (priorities[newData.source] > priorities[oldData.source]).also {
            DebugLogger.info(
                tag,
                "Choosing the most important data from $newData and $oldData. " +
                    "Is new data more important: $it"
            )
        }
    }
}
