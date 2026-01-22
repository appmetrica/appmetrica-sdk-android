package io.appmetrica.analytics.impl.crash.service

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class AlwaysAllowSendCrashPredicateTest : CommonTest() {

    private val crash: Any = mock()

    private val predicate by setUp { AlwaysAllowSendCrashPredicate<Any>() }

    @Test
    fun shouldSend() {
        assertThat(predicate.shouldSend(crash)).isTrue()
    }
}
