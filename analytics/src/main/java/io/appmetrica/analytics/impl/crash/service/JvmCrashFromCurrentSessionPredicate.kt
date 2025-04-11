package io.appmetrica.analytics.impl.crash.service

import android.os.Process
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash

class JvmCrashFromCurrentSessionPredicate : ShouldSendCrashNowPredicate<JvmCrash> {
    override fun shouldSend(crash: JvmCrash): Boolean {
        return crash.pid != Process.myPid()
    }
}
