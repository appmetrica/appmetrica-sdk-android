package io.appmetrica.analytics.impl.referrer.service.provider.google

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class GooglePlayReferrerLibraryTest : CommonTest() {
    private val context: Context = mock()
    private val listener: ReferrerListener = mock()
    private val client: InstallReferrerClient = mock()
    private val builder: InstallReferrerClient.Builder = mock {
        on { build() } doReturn client
    }
    private val executor: ICommonExecutor = mock {
        on { execute(any()) } doAnswer { (it.arguments[0] as Runnable).run() }
    }

    @get:Rule
    val installReferrerClientRule = staticRule<InstallReferrerClient> {
        on { InstallReferrerClient.newBuilder(context) } doReturn builder
    }

    private val selfReporter: SelfReporterWrapper = mock()

    @get:Rule
    val appMetricaSelfReportFacadeRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() } doReturn selfReporter
    }

    private val library by setUp { GooglePlayReferrerLibrary(executor) }

    @Test
    fun `requestReferrer creates client and starts connection`() {
        library.requestReferrer(context, listener)

        verify(builder).build()
        verify(client).startConnection(any())
    }

    @Test
    fun `requestReferrer returns Failure when exception occurs during client creation`() {
        val exception = RuntimeException("Test exception")
        whenever(InstallReferrerClient.newBuilder(context)) doThrow exception

        library.requestReferrer(context, listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from Google Play referrer library")
        assertThat(failure.throwable).isEqualTo(exception)

        verify(selfReporter).reportError(any(), any<Throwable>())
    }

    @Test
    fun `onInstallReferrerSetupFinished returns Success with valid referrer data`() {
        val installReferrer = "utm_source=test&utm_medium=test"
        val clickTimestamp = 1234567890L
        val installTimestamp = 1234567900L
        val referrerDetails: ReferrerDetails = mock {
            on { this.installReferrer } doReturn installReferrer
            on { referrerClickTimestampSeconds } doReturn clickTimestamp
            on { installBeginTimestampSeconds } doReturn installTimestamp
        }

        whenever(client.installReferrer) doReturn referrerDetails

        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())
        listenerCaptor.firstValue.onInstallReferrerSetupFinished(InstallReferrerClient.InstallReferrerResponse.OK)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        inOrder(client, listener) {
            verify(client).endConnection()
            verify(listener).onResult(resultCaptor.capture())
        }

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Success::class.java)
        ObjectPropertyAssertions(resultCaptor.firstValue.referrerInfo)
            .checkField("installReferrer", installReferrer)
            .checkField("referrerClickTimestampSeconds", clickTimestamp)
            .checkField("installBeginTimestampSeconds", installTimestamp)
            .checkField("source", ReferrerInfo.Source.GP)
            .checkAll()
    }

    @Test
    fun `onInstallReferrerSetupFinished returns Failure when responseCode is not OK`() {
        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())

        listenerCaptor.firstValue
            .onInstallReferrerSetupFinished(InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        inOrder(client, listener) {
            verify(client).endConnection()
            verify(listener).onResult(resultCaptor.capture())
        }

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("The connection returned an error code 1")
    }

    @Test
    fun `onInstallReferrerSetupFinished report on self reporter when responseCode is not OK`() {
        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())

        val codeWithReport = listOf(
            InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR,
            InstallReferrerClient.InstallReferrerResponse.PERMISSION_ERROR,
        )
        for (responseCode in -1..10) {
            clearInvocations(selfReporter)
            listenerCaptor.firstValue.onInstallReferrerSetupFinished(responseCode)

            val isNeedReport = responseCode in codeWithReport
            verify(selfReporter, times(if (isNeedReport) 1 else 0)).reportError(any(), any<Throwable>())
        }
    }

    @Test
    fun `onInstallReferrerSetupFinished returns Failure when installReferrer is null`() {
        whenever(client.installReferrer) doReturn null

        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())
        listenerCaptor.firstValue.onInstallReferrerSetupFinished(InstallReferrerClient.InstallReferrerResponse.OK)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        inOrder(client, listener) {
            verify(client).endConnection()
            verify(listener).onResult(resultCaptor.capture())
        }

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Referrer is null")

        verifyNoInteractions(selfReporter)
    }

    @Test
    fun `onInstallReferrerSetupFinished returns Failure when referrer installReferrer is null`() {
        val referrerDetails: ReferrerDetails = mock {
            on { installReferrer } doReturn null
        }
        whenever(client.installReferrer) doReturn referrerDetails

        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())
        listenerCaptor.firstValue.onInstallReferrerSetupFinished(InstallReferrerClient.InstallReferrerResponse.OK)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        inOrder(client, listener) {
            verify(client).endConnection()
            verify(listener).onResult(resultCaptor.capture())
        }

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Referrer is null")

        verifyNoInteractions(selfReporter)
    }

    @Test
    fun `onInstallReferrerSetupFinished returns Failure when referrer installReferrer is blank`() {
        val referrerDetails: ReferrerDetails = mock {
            on { installReferrer } doReturn "   "
        }
        whenever(client.installReferrer) doReturn referrerDetails

        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())
        listenerCaptor.firstValue.onInstallReferrerSetupFinished(InstallReferrerClient.InstallReferrerResponse.OK)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        inOrder(client, listener) {
            verify(client).endConnection()
            verify(listener).onResult(resultCaptor.capture())
        }

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Referrer is null")

        verifyNoInteractions(selfReporter)
    }

    @Test
    fun `listener is called even when installReferrer throws exception`() {
        whenever(client.installReferrer) doThrow RuntimeException("Test exception")

        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())
        listenerCaptor.firstValue.onInstallReferrerSetupFinished(InstallReferrerClient.InstallReferrerResponse.OK)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        inOrder(client, listener) {
            verify(client).endConnection()
            verify(listener).onResult(resultCaptor.capture())
        }

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer via Google Play referrer library")
        assertThat(failure.throwable?.message).isEqualTo("Test exception")

        verify(selfReporter).reportError(any(), any<Throwable>())
    }

    @Test
    fun `listener is called even when endConnection throws exception`() {
        val referrerDetails: ReferrerDetails = mock {
            on { installReferrer } doReturn "utm_source=test"
            on { referrerClickTimestampSeconds } doReturn 1234567890L
            on { installBeginTimestampSeconds } doReturn 1234567900L
        }

        whenever(client.installReferrer) doReturn referrerDetails
        whenever(client.endConnection()) doThrow RuntimeException("Test exception")

        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())
        listenerCaptor.firstValue.onInstallReferrerSetupFinished(InstallReferrerClient.InstallReferrerResponse.OK)

        inOrder(client, listener) {
            verify(client).endConnection()
            verify(listener).onResult(any())
        }

        verifyNoInteractions(selfReporter)
    }

    @Test
    fun `onInstallReferrerSetupFinished submits work to executor`() {
        val runnableCaptor = argumentCaptor<Runnable>()
        val nonExecutingExecutor: ICommonExecutor = mock {
            on { execute(runnableCaptor.capture()) } doAnswer {}
        }
        val nonExecutingLibrary = GooglePlayReferrerLibrary(nonExecutingExecutor)

        nonExecutingLibrary.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())
        listenerCaptor.firstValue.onInstallReferrerSetupFinished(InstallReferrerClient.InstallReferrerResponse.OK)

        verify(nonExecutingExecutor).execute(any())
        verify(listener, never()).onResult(any())

        // After running the submitted task, listener is called
        runnableCaptor.firstValue.run()
        verify(listener).onResult(any())
    }

    @Test
    fun `onInstallReferrerServiceDisconnected does not crash`() {
        library.requestReferrer(context, listener)

        val listenerCaptor = argumentCaptor<InstallReferrerStateListener>()
        verify(client).startConnection(listenerCaptor.capture())
        listenerCaptor.firstValue.onInstallReferrerServiceDisconnected()

        // Should not throw exception
        verifyNoInteractions(selfReporter)
    }
}
