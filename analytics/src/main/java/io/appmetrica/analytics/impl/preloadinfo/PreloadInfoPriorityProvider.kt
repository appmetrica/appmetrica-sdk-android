package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.DistributionPriorityProvider
import io.appmetrica.analytics.logger.internal.YLogger

private const val TAG = "[PreloadInfoPriorityProvider]"

internal class PreloadInfoPriorityProvider : DistributionPriorityProvider<PreloadInfoState>() {

    override fun isNewDataMoreImportant(newData: PreloadInfoState, oldData: PreloadInfoState): Boolean {
        if (!newData.wasSet) {
            return false
        }
        if (!oldData.wasSet) {
            return true
        }
        return (priorities[newData.source] > priorities[oldData.source]).also {
            YLogger.info(
                TAG,
                "Choosing the most important data from $newData and $oldData. " +
                    "Is new data more important: $it"
            )
        }
    }
}
