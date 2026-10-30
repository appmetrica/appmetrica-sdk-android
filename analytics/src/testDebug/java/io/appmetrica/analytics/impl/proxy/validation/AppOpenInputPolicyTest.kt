package io.appmetrica.analytics.impl.proxy.validation

import android.content.Intent
import io.appmetrica.analytics.impl.DeeplinkConsumer
import io.appmetrica.gradle.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions

@RunWith(Parameterized::class)
internal class AppOpenInputPolicyTest(
    private val description: String,
    private val deeplink: String?,
    private val expected: String?
) : CommonTest() {

    @Test
    fun `consumer preserves input policy deduplication and provenance`() {
        val reporter = mock<DeeplinkConsumer.AppOpenEventReporter>(name = description)
        val consumer = DeeplinkConsumer(reporter)
        val intent = mock<Intent> {
            on { dataString } doReturn deeplink
        }

        if (expected == null) {
            consumer.reportAppOpen(deeplink)
            consumer.reportAutoAppOpen(deeplink)
            consumer.reportAppOpen(intent)
            consumer.reportAutoAppOpen("app://first")
            consumer.reportAppOpen(deeplink)
            consumer.reportAppOpen(intent)
            consumer.reportAppOpen("app://first")
            consumer.reportAutoAppOpen(deeplink)
            consumer.reportAppOpen("app://second")

            inOrder(reporter) {
                verify(reporter).report("app://first", true)
                verify(reporter).report("app://second", false)
                verifyNoMoreInteractions()
            }
        } else {
            consumer.reportAppOpen(intent)
            consumer.reportAppOpen(deeplink)
            consumer.reportAutoAppOpen(deeplink)
            consumer.reportAutoAppOpen("${expected}next")

            inOrder(reporter) {
                verify(reporter).report(expected, false)
                verify(reporter).report("${expected}next", true)
                verifyNoMoreInteractions()
            }
        }
        verifyNoMoreInteractions(reporter)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<String?>> = listOf(
            arrayOf("null", null, null),
            arrayOf("empty", "", null),
            arrayOf("space", " ", null),
            arrayOf("ASCII whitespace", "\t\r\n", null),
            arrayOf("non-breaking space U+00A0", "\u00a0", null),
            arrayOf("em space U+2003", "\u2003", null),
            arrayOf("zero-width space U+200B", "\u200b", "\u200b"),
            arrayOf("valid deeplink", "app://test", "app://test"),
            arrayOf("valid string with edge spaces", " scheme://value ", " scheme://value ")
        )
    }
}
