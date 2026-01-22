package io.appmetrica.analytics.impl.id

import android.content.Context
import io.appmetrica.analytics.appsetid.internal.AppSetIdListener
import io.appmetrica.analytics.appsetid.internal.IAppSetIdRetriever

internal class DummyAppSetIdRetriever : IAppSetIdRetriever {

    override fun retrieveAppSetId(context: Context, listener: AppSetIdListener) {
        listener.onFailure(IllegalStateException("No App Set ID library"))
    }
}
