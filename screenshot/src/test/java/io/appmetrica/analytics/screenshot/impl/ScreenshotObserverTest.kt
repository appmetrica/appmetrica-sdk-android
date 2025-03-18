package io.appmetrica.analytics.screenshot.impl

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideContentObserverCaptorConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenshotObserverTest : CommonTest() {

    private val now = 34234234L
    private val detectWindowSeconds = 1998L

    private val dataCursor = MatrixCursor(
        arrayOf(
            MediaStore.Images.Media.DATE_ADDED,
            "first",
            "second"
        )
    )

    private val contentResolver: ContentResolver = mock {
        on {
            query(
                any(),
                any(),
                anyString(),
                any(),
                anyString()
            )
        } doReturn dataCursor
    }
    private val context: Context = mock {
        on { contentResolver } doReturn contentResolver
    }
    private val handler: Handler = mock()
    private val defaultExecutor: IHandlerExecutor = mock {
        on { handler } doReturn handler
    }
    private val clientContext: ClientContext = mock {
        on { context } doReturn context
        on { defaultExecutor } doReturn defaultExecutor
    }
    private val screenshotCapturedCallback: () -> Unit = mock()

    private val uri: Uri = mock {
        on { toString() } doReturn MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()
    }
    private val config: ClientSideContentObserverCaptorConfig = mock {
        on { mediaStoreColumnNames } doReturn listOf(
            "first",
            "second"
        )
        on { detectWindowSeconds } doReturn detectWindowSeconds
    }

    @get:Rule
    val systemTimeProviderRule = constructionRule<SystemTimeProvider> {
        on { currentTimeSeconds() } doReturn now
    }

    private val observer = ScreenshotObserver(clientContext, screenshotCapturedCallback)

    @Test
    fun onChangeIfConfigIsNull() {
        observer.onChange(true, uri)
        verifyNoInteractions(contentResolver, screenshotCapturedCallback)
    }

    @Test
    fun onChangeIfEmptyCursor() {
        observer.updateConfig(config)
        observer.onChange(true, uri)

        verify(contentResolver).query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATE_ADDED, "first", "second"),
            MediaStore.Images.Media.DATE_ADDED + " >= ?",
            arrayOf((now - detectWindowSeconds).toString()),
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        verify(screenshotCapturedCallback, never()).invoke()
    }

    @Test
    fun onChangeIfGoodFirstColumn() {
        dataCursor.addRow(arrayOf(now, "screenshot", "some string"))
        observer.updateConfig(config)
        observer.onChange(true, uri)

        verify(contentResolver).query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATE_ADDED, "first", "second"),
            MediaStore.Images.Media.DATE_ADDED + " >= ?",
            arrayOf((now - detectWindowSeconds).toString()),
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        verify(screenshotCapturedCallback).invoke()
    }

    @Test
    fun onChangeIfGoodSecondColumn() {
        dataCursor.addRow(arrayOf(now, "some string", "screenshot"))
        observer.updateConfig(config)
        observer.onChange(true, uri)

        verify(contentResolver).query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATE_ADDED, "first", "second"),
            MediaStore.Images.Media.DATE_ADDED + " >= ?",
            arrayOf((now - detectWindowSeconds).toString()),
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        verify(screenshotCapturedCallback).invoke()
    }

    @Test
    fun onChangeIfBadColumns() {
        dataCursor.addRow(arrayOf(now, "some string", "some string"))
        observer.updateConfig(config)
        observer.onChange(true, uri)

        verify(contentResolver).query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATE_ADDED, "first", "second"),
            MediaStore.Images.Media.DATE_ADDED + " >= ?",
            arrayOf((now - detectWindowSeconds).toString()),
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        verify(screenshotCapturedCallback, never()).invoke()
    }

    @Test
    fun onChangeIfWrongColumns() {
        whenever(config.mediaStoreColumnNames).thenReturn(listOf("third"))
        observer.updateConfig(config)
        observer.onChange(true, uri)

        verify(contentResolver).query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATE_ADDED, "third"),
            MediaStore.Images.Media.DATE_ADDED + " >= ?",
            arrayOf((now - detectWindowSeconds).toString()),
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        verify(screenshotCapturedCallback, never()).invoke()
    }
}
