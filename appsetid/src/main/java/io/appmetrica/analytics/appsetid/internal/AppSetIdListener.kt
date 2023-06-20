package io.appmetrica.analytics.appsetid.internal

import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope

interface AppSetIdListener {

    fun onAppSetIdRetrieved(id: String?, scope: AppSetIdScope)
    fun onFailure(ex: Throwable?)
}
