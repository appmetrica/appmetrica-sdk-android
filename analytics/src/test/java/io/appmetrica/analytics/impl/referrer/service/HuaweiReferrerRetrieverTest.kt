package io.appmetrica.analytics.impl.referrer.service

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doAnswer
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

@RunWith(RobolectricTestRunner::class)
internal class HuaweiReferrerRetrieverTest : CommonTest() {
    private val listener: ReferrerReceivedListener = mock()
    private val contentResolver: ContentResolver = mock()
    private val cursor: Cursor = mock()
    private val hmsReferrerThread: InterruptionSafeThread = mock()
    private val validUri: Uri = Uri.parse("content://com.huawei.appmarket.commondata/item/5")
    private lateinit var referrerRetriever: HuaweiReferrerRetriever
    private lateinit var threadRunnable: Runnable

    @get:Rule
    val globalServiceLocatorRule: GlobalServiceLocatorRule = GlobalServiceLocatorRule()
    private val context: Context by lazy { globalServiceLocatorRule.context }

    @Before
    fun setUp() {
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.query(validUri, null, null, arrayOf<String>(context.packageName), null))
            .thenReturn(cursor)
        whenever(GlobalServiceLocator.getInstance().serviceExecutorProvider.getHmsReferrerThread(any()))
            .thenAnswer(
                Answer { invocation ->
                    threadRunnable = invocation.getArgument(0)
                    hmsReferrerThread
                }
            )
        doAnswer(
            Answer {
                threadRunnable.run()
                null
            }
        ).whenever(hmsReferrerThread).start()
        referrerRetriever = HuaweiReferrerRetriever(context)
    }

    @Test
    fun noCursor() {
        whenever(contentResolver.query(validUri, null, null, arrayOf<String>(context.packageName), null))
            .thenReturn(null)
        referrerRetriever.retrieveReferrer(listener)
        verify(listener).onReferrerReceived(null)
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun getCursorThrowsException() {
        val exception = RuntimeException()
        whenever(contentResolver.query(validUri, null, null, arrayOf<String>(context.packageName), null))
            .thenThrow(exception)
        referrerRetriever.retrieveReferrer(listener)
        verify(listener).onReferrerRetrieveError(any<ExecutionException>())
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun emptyCursor() {
        whenever(cursor.moveToFirst()).thenReturn(false)
        referrerRetriever.retrieveReferrer(listener)
        verify(listener).onReferrerReceived(null)
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun emptyReferrer() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getLong(1)).thenReturn(40L)
        whenever(cursor.getLong(2)).thenReturn(50L)
        referrerRetriever.retrieveReferrer(listener)
        verify(listener).onReferrerReceived(null)
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun hasReferrer() {
        val referrer = "test referrer"
        val clickTimestamp = 12L
        val installTimestamp = 13L
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(referrer)
        whenever(cursor.getLong(1)).thenReturn(clickTimestamp)
        whenever(cursor.getLong(2)).thenReturn(installTimestamp)
        referrerRetriever.retrieveReferrer(listener)
        verify(listener).onReferrerReceived(
            ReferrerInfo(
                referrer,
                clickTimestamp,
                installTimestamp,
                ReferrerInfo.Source.HMS
            )
        )
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun getCursorHangs() {
        whenever((contentResolver).query(validUri, null, null, arrayOf(context.packageName), null))
            .thenAnswer(
                Answer {
                    Thread.sleep(10000)
                    cursor
                }
            )
        whenever(GlobalServiceLocator.getInstance().serviceExecutorProvider.getHmsReferrerThread(any()))
            .thenAnswer(Answer { invocation -> InterruptionSafeThread((invocation.getArgument(0) as Runnable)) })
        val start = System.currentTimeMillis()
        referrerRetriever.retrieveReferrer(listener)
        val end = System.currentTimeMillis()
        verify(listener).onReferrerRetrieveError(any<TimeoutException>())
        verifyNoMoreInteractions(listener)
        assertThat(end - start).isGreaterThanOrEqualTo(5000).isLessThan(10000)
    }

    @Test
    fun cursorIsClosed() {
        referrerRetriever.retrieveReferrer(listener)
        verify(cursor).close()
    }
}
