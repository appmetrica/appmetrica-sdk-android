package io.appmetrica.analytics.impl

internal interface StateProvider<CANDIDATE, CHOSEN, STORAGE> : Function2<CHOSEN, List<CANDIDATE>, STORAGE>
