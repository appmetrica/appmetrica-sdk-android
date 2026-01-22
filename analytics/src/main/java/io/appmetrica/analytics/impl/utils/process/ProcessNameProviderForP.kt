package io.appmetrica.analytics.impl.utils.process

import android.annotation.TargetApi
import android.app.Application
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline

@DoNotInline
@TargetApi(Build.VERSION_CODES.P)
internal class ProcessNameProviderForP : ProcessNameProvider {

    override fun getProcessName(): String? {
        return Application.getProcessName()
    }
}
