package io.appmetrica.analytics.impl

internal interface UpdatedCandidatesProvider<CANDIDATE, CHOSEN> : Function2<List<CANDIDATE>, CHOSEN, List<CANDIDATE>?>
