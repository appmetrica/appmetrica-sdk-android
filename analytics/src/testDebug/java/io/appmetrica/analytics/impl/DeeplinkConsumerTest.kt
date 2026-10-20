package io.appmetrica.analytics.impl

import android.content.Intent
import io.appmetrica.gradle.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.ignoreStubs
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class DeeplinkConsumerTest : CommonTest() {

    private val reportEvent = mock<DeeplinkConsumer.AppOpenEventReporter>()
    private val deeplink = "Some deeplink"
    private val secondDeeplink = "Second deeplink"
    private val intent = mock<Intent> {
        on { dataString } doReturn deeplink
    }
    private val secondIntent = mock<Intent> {
        on { dataString } doReturn secondDeeplink
    }

    private lateinit var deeplinkConsumer: DeeplinkConsumer

    @Before
    fun setUp() {
        deeplinkConsumer = DeeplinkConsumer(reportEvent)
    }

    @Test
    fun `reportAppOpen with null intent`() {
        deeplinkConsumer.reportAppOpen(null as Intent?)
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAppOpen with null string`() {
        deeplinkConsumer.reportAppOpen(null as String?)
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAutoAppOpen with null string`() {
        deeplinkConsumer.reportAutoAppOpen(null)
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAppOpen with null intent after non null`() {
        deeplinkConsumer.reportAppOpen(intent)
        deeplinkConsumer.reportAppOpen(null as Intent?)
        verify(reportEvent).report(deeplink, false)
    }

    @Test
    fun `reportAppOpen with null string after non null`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(null as String?)
        verify(reportEvent).report(deeplink, false)
    }

    @Test
    fun `reportAutoAppOpen with null string after non null`() {
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        deeplinkConsumer.reportAutoAppOpen(null)
        verify(reportEvent).report(deeplink, true)
    }

    @Test
    fun `reportAppOpen with empty intent`() {
        deeplinkConsumer.reportAppOpen(mock<Intent>())
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAppOpen with intent with empty deeplink`() {
        whenever(intent.dataString).thenReturn("")
        deeplinkConsumer.reportAppOpen(intent)
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAppOpen with empty deeplink`() {
        deeplinkConsumer.reportAppOpen("")
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAutoAppOpen with empty deeplink`() {
        deeplinkConsumer.reportAutoAppOpen("")
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAppOpen with empty intent after non empty deeplink`() {
        deeplinkConsumer.reportAppOpen(intent)
        deeplinkConsumer.reportAppOpen(mock<Intent>())
        verify(reportEvent).report(deeplink, false)
    }

    @Test
    fun `reportAppOpen with intent with empty deeplink after non empty`() {
        deeplinkConsumer.reportAppOpen(intent)
        whenever(intent.dataString).thenReturn("")
        deeplinkConsumer.reportAppOpen(intent)
        verify(reportEvent).report(deeplink, false)
    }

    @Test
    fun `reportAppOpen with empty deeplink after non empty`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen("")
        verify(reportEvent).report(deeplink, false)
    }

    @Test
    fun `reportAutoAppOpen with empty deeplink after non empty`() {
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        deeplinkConsumer.reportAutoAppOpen("")
        verify(reportEvent).report(deeplink, true)
    }

    @Test
    fun `reportAppOpen with two intent deeplinks`() {
        deeplinkConsumer.reportAppOpen(intent)
        deeplinkConsumer.reportAppOpen(secondIntent)
        inOrder(*ignoreStubs(reportEvent)) {
            verify(reportEvent).report(deeplink, false)
            verify(reportEvent).report(secondDeeplink, false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `reportAppOpen with two deeplinks`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(secondDeeplink)
        inOrder(*ignoreStubs(reportEvent)) {
            verify(reportEvent).report(deeplink, false)
            verify(reportEvent).report(secondDeeplink, false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `reportAutoAppOpen with two deeplinks`() {
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        deeplinkConsumer.reportAutoAppOpen(secondDeeplink)
        inOrder(*ignoreStubs(reportEvent)) {
            verify(reportEvent).report(deeplink, true)
            verify(reportEvent).report(secondDeeplink, true)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `reportAppOpen with the same intent`() {
        deeplinkConsumer.reportAppOpen(intent)
        clearInvocations(reportEvent)
        deeplinkConsumer.reportAppOpen(intent)
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAppOpen with the same deeplink`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        clearInvocations(reportEvent)
        deeplinkConsumer.reportAppOpen(deeplink)
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAutoAppOpen with the same deeplink`() {
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        clearInvocations(reportEvent)
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `reportAppOpen with different ways`() {
        deeplinkConsumer.reportAppOpen(intent)
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(deeplink)

        deeplinkConsumer.reportAppOpen(secondDeeplink)
        deeplinkConsumer.reportAppOpen(secondIntent)
        deeplinkConsumer.reportAutoAppOpen(secondDeeplink)

        inOrder(*ignoreStubs(reportEvent)) {
            verify(reportEvent).report(deeplink, false)
            verify(reportEvent).report(secondDeeplink, false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `only consecutive deeplinks are deduplicated`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(secondDeeplink)
        deeplinkConsumer.reportAppOpen(secondDeeplink)
        deeplinkConsumer.reportAppOpen(deeplink)

        inOrder(*ignoreStubs(reportEvent)) {
            verify(reportEvent).report(deeplink, false)
            verify(reportEvent).report(secondDeeplink, false)
            verify(reportEvent).report(deeplink, false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `invalid deeplinks do not reset consecutive deduplication`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(null as String?)
        deeplinkConsumer.reportAutoAppOpen("")
        deeplinkConsumer.reportAppOpen(mock<Intent>())
        deeplinkConsumer.reportAutoAppOpen(deeplink)

        verify(reportEvent).report(deeplink, false)
        verifyNoMoreInteractions(*ignoreStubs(reportEvent))
    }

    @Test
    fun `different consumers have independent deduplication`() {
        val secondReportEvent = mock<DeeplinkConsumer.AppOpenEventReporter>()
        val secondConsumer = DeeplinkConsumer(secondReportEvent)

        deeplinkConsumer.reportAppOpen(deeplink)
        secondConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(secondDeeplink)
        secondConsumer.reportAutoAppOpen(deeplink)

        inOrder(*ignoreStubs(reportEvent)) {
            verify(reportEvent).report(deeplink, false)
            verify(reportEvent).report(secondDeeplink, false)
            verifyNoMoreInteractions()
        }
        verify(secondReportEvent).report(deeplink, false)
        verifyNoMoreInteractions(*ignoreStubs(secondReportEvent))
    }
}
