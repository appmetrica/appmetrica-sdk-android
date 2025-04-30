package io.appmetrica.analytics.impl.referrer.service

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReferrerFromLibraryRetrieverTest : CommonTest() {

    private val context: Context = mock()
    private val listener: ReferrerReceivedListener = mock()
    private val executor: ICommonExecutor = mock()
    private val runnableCaptor = argumentCaptor<Runnable>()

    private val installReferrer = "some test referrer"
    private val clickTimestamp: Long = 438573450
    private val installTimestamp: Long = 3288994

    private val installReferrerDetails = mock<ReferrerDetails> {
        on { installReferrer } doReturn installReferrer
        on { referrerClickTimestampSeconds } doReturn clickTimestamp
        on { installBeginTimestampSeconds } doReturn installTimestamp
    }

    private val client: InstallReferrerClient = mock {
        on { installReferrer } doReturn installReferrerDetails
    }

    private val referrerListenerCaptor = argumentCaptor<InstallReferrerStateListener>()

    private val installReferrerClientBuilder: InstallReferrerClient.Builder = mock {
        on { build() } doReturn client
    }

    @get:Rule
    val installReferrerClientRule = staticRule<InstallReferrerClient> {
        on { InstallReferrerClient.newBuilder(context) } doReturn installReferrerClientBuilder
    }

    private val referrerFromLibraryRetriever by setUp { ReferrerFromLibraryRetriever(context, executor) }

    @Test
    fun `retrieveReferrer if ok`() {
        referrerFromLibraryRetriever.retrieveReferrer(listener)
        inOrder(client, executor, listener) {
            verify(client).startConnection(referrerListenerCaptor.capture())
            verifyNoMoreInteractions()
            referrerListenerCaptor.firstValue.onInstallReferrerSetupFinished(0)
            verify(executor).execute(runnableCaptor.capture())
            verifyNoMoreInteractions()
            runnableCaptor.firstValue.run()
            verify(listener).onReferrerReceived(argThat {
                this.installReferrer == installReferrer &&
                    this.referrerClickTimestampSeconds == clickTimestamp &&
                    this.installBeginTimestampSeconds == installTimestamp
            })
            verify(client).endConnection()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `retrieveReferrer if thrown`() {
        val exception = RuntimeException()
        whenever(client.installReferrer).thenThrow(exception)
        referrerFromLibraryRetriever.retrieveReferrer(listener)
        inOrder(client, executor, listener) {
            verify(client).startConnection(referrerListenerCaptor.capture())
            verifyNoMoreInteractions()
            referrerListenerCaptor.firstValue.onInstallReferrerSetupFinished(0)
            verify(executor).execute(runnableCaptor.capture())
            verifyNoMoreInteractions()
            runnableCaptor.firstValue.run()
            verify(listener).onReferrerRetrieveError(exception)
            verify(client).endConnection()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `retrieveReferrer if bad code`() {
        referrerFromLibraryRetriever.retrieveReferrer(listener)
        inOrder(client, executor, listener) {
            verify(client).startConnection(referrerListenerCaptor.capture())
            verifyNoMoreInteractions()
            referrerListenerCaptor.firstValue.onInstallReferrerSetupFinished(1)
            verify(executor).execute(runnableCaptor.capture())
            verifyNoMoreInteractions()
            runnableCaptor.firstValue.run()
            verify(listener).onReferrerRetrieveError(argThat {
                this is IllegalStateException && this.message == "Referrer check failed with error 1"
            })
            verifyNoMoreInteractions()
        }
    }
}
