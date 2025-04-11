package io.appmetrica.analytics.impl.crash.ndk.service

import android.os.Process
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate

class NativeCrashFromCurrentSessionPredicate(
    private val crashPid: Int
) : ShouldSendCrashNowPredicate<String> {
    override fun shouldSend(crash: String): Boolean {
        return crashPid != Process.myPid()
    }
}
