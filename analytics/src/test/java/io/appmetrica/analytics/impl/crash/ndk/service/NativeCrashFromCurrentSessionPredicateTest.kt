package io.appmetrica.analytics.impl.crash.ndk.service

import android.os.Process
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn

class NativeCrashFromCurrentSessionPredicateTest : CommonTest() {

    private val myPid = 100500
    private val notMyPid = 200500
    private val crash = "crash dump"

    @get:Rule
    val processMockedStaticRule = staticRule<Process> {
        on { Process.myPid() } doReturn myPid
    }

    @Test
    fun `shouldSend for my pid`() {
        val predicate = NativeCrashFromCurrentSessionPredicate(myPid)
        assertThat(predicate.shouldSend(crash)).isFalse()
    }

    @Test
    fun `shouldSend for not my pid`() {
        val predicate = NativeCrashFromCurrentSessionPredicate(notMyPid)
        assertThat(predicate.shouldSend(crash)).isTrue()
    }
}
