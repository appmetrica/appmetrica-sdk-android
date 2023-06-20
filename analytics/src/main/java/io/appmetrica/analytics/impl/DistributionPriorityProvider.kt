package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.utils.MapWithDefault

internal abstract class DistributionPriorityProvider<T> {

    protected val priorities = MapWithDefault<DistributionSource, Int>(0).apply {
        put(DistributionSource.UNDEFINED, 0)
        put(DistributionSource.APP, 1)
        put(DistributionSource.SATELLITE, 2)
        put(DistributionSource.RETAIL, 3)
    }

    abstract fun isNewDataMoreImportant(newData: T, oldData: T): Boolean
}
