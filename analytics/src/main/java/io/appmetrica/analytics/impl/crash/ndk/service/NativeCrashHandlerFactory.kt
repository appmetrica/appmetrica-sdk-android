package io.appmetrica.analytics.impl.crash.ndk.service

import android.content.Context
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.analytics.impl.crash.service.AlwaysAllowSendCrashPredicate
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashHandler

internal class NativeCrashHandlerFactory(
    private val markCrashCompleted: (String) -> Unit
) {

    fun createHandlerForActualSession(context: Context, reportConsumer: ReportConsumer): NativeCrashHandler {
        return NativeCrashHandlerImpl(
            context,
            reportConsumer,
            markCrashCompleted,
            { nativeCrash -> NativeCrashFromCurrentSessionPredicate(nativeCrash.metadata.processID) },
            InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
            "actual"
        )
    }

    fun createHandlerForPrevSession(context: Context, reportConsumer: ReportConsumer): NativeCrashHandler {
        return NativeCrashHandlerImpl(
            context,
            reportConsumer,
            markCrashCompleted,
            { _: AppMetricaNativeCrash -> AlwaysAllowSendCrashPredicate() },
            InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
            "prev session"
        )
    }
}
