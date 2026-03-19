package io.appmetrica.analytics.impl.crash.jvm.service

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class FileCrashTimestampProviderTest : CommonTest() {

    private val currentTimeMillis = 1700000000000L
    private val fileModifiedTimestamp = 1600000000000L

    @get:Rule
    val systemTimeProviderRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() } doReturn currentTimeMillis
    }

    @Test
    fun `getTimestamp returns file timestamp when it is positive`() {
        val jvmCrash: JvmCrash = mock { on { fileModifiedTimestamp } doReturn fileModifiedTimestamp }
        assertThat(FileCrashTimestampProvider().getTimestamp(jvmCrash)).isEqualTo(fileModifiedTimestamp)
    }

    @Test
    fun `getTimestamp returns system time when file timestamp is zero`() {
        val jvmCrash: JvmCrash = mock { on { fileModifiedTimestamp } doReturn 0L }
        assertThat(FileCrashTimestampProvider().getTimestamp(jvmCrash)).isEqualTo(currentTimeMillis)
    }

    @Test
    fun `getTimestamp returns system time when file timestamp is negative`() {
        val jvmCrash: JvmCrash = mock { on { fileModifiedTimestamp } doReturn -1L }
        assertThat(FileCrashTimestampProvider().getTimestamp(jvmCrash)).isEqualTo(currentTimeMillis)
    }
}
