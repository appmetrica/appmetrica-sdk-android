package io.appmetrica.analytics.appsetid.internal

import android.content.Context

interface IAppSetIdRetriever {

    @Throws(Throwable::class)
    fun retrieveAppSetId(context: Context, listener: AppSetIdListener)
}
