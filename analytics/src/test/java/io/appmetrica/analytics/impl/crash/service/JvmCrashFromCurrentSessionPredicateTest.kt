package io.appmetrica.analytics.impl.crash.service

import android.os.Process
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class JvmCrashFromCurrentSessionPredicateTest : CommonTest() {

    private val myPid = 100500
    private val notMyPid = 200500
    private val crash: JvmCrash = mock()

    @get:Rule
    val jvmCrashFromCurrentSessionPredicate = staticRule<Process> {
        on { Process.myPid() } doReturn myPid
    }

    private val predicate: JvmCrashFromCurrentSessionPredicate by setUp { JvmCrashFromCurrentSessionPredicate() }

    @Test
    fun `shouldSend for myPid`() {
        whenever(crash.pid).thenReturn(myPid)
        assertThat(predicate.shouldSend(crash)).isFalse()
    }

    @Test
    fun `shouldSend for not my process`() {
        whenever(crash.pid).thenReturn(notMyPid)
        assertThat(predicate.shouldSend(crash)).isTrue()
    }
}
