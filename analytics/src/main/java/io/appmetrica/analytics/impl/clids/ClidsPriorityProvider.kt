package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.DistributionPriorityProvider
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.Utils

private const val TAG = "[ClidsPriorityProvider]"

internal class ClidsPriorityProvider : DistributionPriorityProvider<ClidsInfo.Candidate>() {

    override fun isNewDataMoreImportant(newData: ClidsInfo.Candidate, oldData: ClidsInfo.Candidate): Boolean {
        return if (Utils.isNullOrEmpty(oldData.clids)) {
            true
        } else if (Utils.isNullOrEmpty(newData.clids)) {
            return false
        } else if (newData.source == DistributionSource.APP) {
            priorities[newData.source] >= priorities[oldData.source]
        } else {
            priorities[newData.source] > priorities[oldData.source]
        }.also {
            YLogger.info(
                TAG,
                "Choosing the most important data from new data: $newData and old data: $oldData, result: $it"
            )
        }
    }
}
