package io.appmetrica.analytics.impl

internal interface DistributionInfo<CANDIDATE, CHOSEN> {

    val chosen: CHOSEN
    val candidates: List<CANDIDATE>
}
