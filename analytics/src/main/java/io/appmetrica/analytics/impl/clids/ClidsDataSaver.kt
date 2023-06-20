package io.appmetrica.analytics.impl.clids

import androidx.annotation.MainThread
import io.appmetrica.analytics.impl.ContentProviderDataSaver
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.GlobalServiceLocator

internal class ClidsDataSaver : ContentProviderDataSaver<Map<String, String>> {

    @MainThread
    override fun invoke(data: Map<String, String>): Boolean =
        GlobalServiceLocator.getInstance()
            .clidsStorage.updateIfNeeded(ClidsInfo.Candidate(data, DistributionSource.RETAIL))
}
