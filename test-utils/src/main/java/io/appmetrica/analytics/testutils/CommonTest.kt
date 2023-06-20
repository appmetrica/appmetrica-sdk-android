package io.appmetrica.analytics.testutils

import org.junit.Rule
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

open class CommonTest {

    @Rule
    @JvmField
    val testTimeoutRule = DisableOnDebug(Timeout(30, TimeUnit.SECONDS))
    @Rule
    @JvmField
    val printExecutorRule = PrintExecutorRule()

}
