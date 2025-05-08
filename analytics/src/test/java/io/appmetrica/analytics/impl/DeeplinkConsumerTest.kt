package io.appmetrica.analytics.impl

import android.content.Intent
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeeplinkConsumerTest : CommonTest() {

    private val reporter = mock<IMainReporter>()
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
        deeplinkConsumer = DeeplinkConsumer(reporter)
    }

    @Test
    fun `reportAppOpen with null intent`() {
        deeplinkConsumer.reportAppOpen(null as Intent?)
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAppOpen with null string`() {
        deeplinkConsumer.reportAppOpen(null as String?)
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAutoAppOpen with null string`() {
        deeplinkConsumer.reportAutoAppOpen(null)
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAppOpen with null intent after non null`() {
        deeplinkConsumer.reportAppOpen(intent)
        deeplinkConsumer.reportAppOpen(null as Intent?)
        verify(reporter).reportAppOpen(deeplink, false)
    }

    @Test
    fun `reportAppOpen with null string after non null`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(null as String?)
        verify(reporter).reportAppOpen(deeplink, false)
    }

    @Test
    fun `reportAutoAppOpen with null string after non null`() {
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        deeplinkConsumer.reportAutoAppOpen(null)
        verify(reporter).reportAppOpen(deeplink, true)
    }

    @Test
    fun `reportAppOpen with empty intent`() {
        deeplinkConsumer.reportAppOpen(mock<Intent>())
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAppOpen with intent with empty deeplink`() {
        whenever(intent.dataString).thenReturn("")
        deeplinkConsumer.reportAppOpen(intent)
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAppOpen with empty deeplink`() {
        deeplinkConsumer.reportAppOpen("")
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAutoAppOpen with empty deeplink`() {
        deeplinkConsumer.reportAutoAppOpen("")
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAppOpen with empty intent after non empty deeplink`() {
        deeplinkConsumer.reportAppOpen(intent)
        deeplinkConsumer.reportAppOpen(mock<Intent>())
        verify(reporter).reportAppOpen(deeplink, false)
    }

    @Test
    fun `reportAppOpen with intent with empty deeplink after non empty`() {
        deeplinkConsumer.reportAppOpen(intent)
        whenever(intent.dataString).thenReturn("")
        deeplinkConsumer.reportAppOpen(intent)
        verify(reporter).reportAppOpen(deeplink, false)
    }

    @Test
    fun `reportAppOpen with empty deeplink after non empty`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen("")
        verify(reporter).reportAppOpen(deeplink, false)
    }

    @Test
    fun `reportAutoAppOpen with empty deeplink after non empty`() {
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        deeplinkConsumer.reportAutoAppOpen("")
        verify(reporter).reportAppOpen(deeplink, true)
    }

    @Test
    fun `reportAppOpen with two intent deeplinks`() {
        deeplinkConsumer.reportAppOpen(intent)
        deeplinkConsumer.reportAppOpen(secondIntent)
        inOrder(reporter) {
            verify(reporter).reportAppOpen(deeplink, false)
            verify(reporter).reportAppOpen(secondDeeplink, false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `reportAppOpen with two deeplinks`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(secondDeeplink)
        inOrder(reporter) {
            verify(reporter).reportAppOpen(deeplink, false)
            verify(reporter).reportAppOpen(secondDeeplink, false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `reportAutoAppOpen with two deeplinks`() {
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        deeplinkConsumer.reportAutoAppOpen(secondDeeplink)
        inOrder(reporter) {
            verify(reporter).reportAppOpen(deeplink, true)
            verify(reporter).reportAppOpen(secondDeeplink, true)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `reportAppOpen with the same intent`() {
        deeplinkConsumer.reportAppOpen(intent)
        clearInvocations(reporter)
        deeplinkConsumer.reportAppOpen(intent)
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAppOpen with the same deeplink`() {
        deeplinkConsumer.reportAppOpen(deeplink)
        clearInvocations(reporter)
        deeplinkConsumer.reportAppOpen(deeplink)
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAutoAppOpen with the same deeplink`() {
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        clearInvocations(reporter)
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        verifyNoMoreInteractions(reporter)
    }

    @Test
    fun `reportAppOpen with different ways`() {
        deeplinkConsumer.reportAppOpen(intent)
        deeplinkConsumer.reportAutoAppOpen(deeplink)
        deeplinkConsumer.reportAppOpen(deeplink)

        deeplinkConsumer.reportAppOpen(secondDeeplink)
        deeplinkConsumer.reportAppOpen(secondIntent)
        deeplinkConsumer.reportAutoAppOpen(secondDeeplink)

        inOrder(reporter) {
            verify(reporter).reportAppOpen(deeplink, false)
            verify(reporter).reportAppOpen(secondDeeplink, false)
            verifyNoMoreInteractions()
        }
    }
}
