package io.appmetrica.analytics.impl.referrer.service.provider.huawei

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class HuaweiReferrerContentProviderTest : CommonTest() {
    private val packageName = "com.test.app"
    private val providerUri: Uri = mock()
    private val cursor: Cursor = mock {
        on { close() } doAnswer {}
    }
    private val contentResolver: ContentResolver = mock {
        on { query(eq(providerUri), isNull(), isNull(), eq(arrayOf(packageName)), isNull()) } doReturn cursor
    }
    private val context: Context = mock {
        on { packageName } doReturn packageName
        on { contentResolver } doReturn contentResolver
    }

    @get:Rule
    val uriRule = staticRule<Uri> {
        on { Uri.parse("content://com.huawei.appmarket.commondata/item/5") } doReturn providerUri
    }

    private val contentProvider by setUp { HuaweiReferrerContentProvider() }

    @Test
    fun `getReferrer returns Success with referrer info when cursor has data`() {
        val installReferrer = "utm_source=test&utm_medium=test"
        val clickTimestamp = 1234567890L
        val installBeginTimestamp = 1234567900L
        val installFinishTimestamp = 1234567950L

        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.columnCount).thenReturn(6)
        whenever(cursor.getString(5)).thenReturn(installReferrer)
        whenever(cursor.isNull(1)).thenReturn(false)
        whenever(cursor.isNull(3)).thenReturn(false)
        whenever(cursor.getLong(1)).thenReturn(clickTimestamp)
        whenever(cursor.getLong(2)).thenReturn(installFinishTimestamp)
        whenever(cursor.getLong(3)).thenReturn(installBeginTimestamp)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Success::class.java)
        val success = result as ReferrerResult.Success
        assertThat(success.referrerInfo.installReferrer).isEqualTo(installReferrer)
        assertThat(success.referrerInfo.referrerClickTimestampSeconds).isEqualTo(clickTimestamp)
        assertThat(success.referrerInfo.installBeginTimestampSeconds).isEqualTo(installBeginTimestamp)
        assertThat(success.referrerInfo.source).isEqualTo(ReferrerInfo.Source.HMS)

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Success with zero timestamps when values are null`() {
        val installReferrer = "utm_source=test&utm_medium=test"

        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.columnCount).thenReturn(6)
        whenever(cursor.getString(5)).thenReturn(installReferrer)
        whenever(cursor.isNull(1)).thenReturn(true)
        whenever(cursor.isNull(3)).thenReturn(true)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Success::class.java)
        val success = result as ReferrerResult.Success
        assertThat(success.referrerInfo.installReferrer).isEqualTo(installReferrer)
        assertThat(success.referrerInfo.referrerClickTimestampSeconds).isEqualTo(0L)
        assertThat(success.referrerInfo.installBeginTimestampSeconds).isEqualTo(0L)

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Success with zero timestamps when getLong throws`() {
        val installReferrer = "utm_source=test&utm_medium=test"

        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.columnCount).thenReturn(6)
        whenever(cursor.getString(5)).thenReturn(installReferrer)
        whenever(cursor.isNull(1)).thenReturn(false)
        whenever(cursor.isNull(3)).thenReturn(false)
        whenever(cursor.getLong(1)).thenThrow(RuntimeException("click timestamp failed"))
        whenever(cursor.getLong(3)).thenThrow(RuntimeException("install begin timestamp failed"))

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Success::class.java)
        val success = result as ReferrerResult.Success
        assertThat(success.referrerInfo.installReferrer).isEqualTo(installReferrer)
        assertThat(success.referrerInfo.referrerClickTimestampSeconds).isEqualTo(0L)
        assertThat(success.referrerInfo.installBeginTimestampSeconds).isEqualTo(0L)

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Failure when queryReferrer returns null`() {
        whenever(contentResolver.query(anyOrNull(), isNull(), isNull(), eq(arrayOf(packageName)), isNull()))
            .thenReturn(null)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable).isNotNull
        assertThat(failure.throwable!!.message).isEqualTo("Not found content provider")
    }

    @Test
    fun `getReferrer returns Failure when cursor is empty`() {
        whenever(cursor.moveToFirst()).thenReturn(false)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable).isNotNull
        assertThat(failure.throwable!!.message).isEqualTo("Cursor is empty")

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Failure when columnCount is insufficient`() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.columnCount).thenReturn(5)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable).isNotNull
        assertThat(failure.throwable!!.message).isEqualTo("Cursor column count is insufficient: 5")

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Failure when installReferrer at index 5 is null even if index 0 is filled`() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.columnCount).thenReturn(6)
        whenever(cursor.getString(0)).thenReturn("legacy-referrer-at-0")
        whenever(cursor.getString(5)).thenReturn(null)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable).isNotNull
        assertThat(failure.throwable!!.message).isEqualTo("Referrer is empty")

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Failure when installReferrer at index 5 is blank even if index 0 is filled`() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.columnCount).thenReturn(6)
        whenever(cursor.getString(0)).thenReturn("legacy-referrer-at-0")
        whenever(cursor.getString(5)).thenReturn("   ")

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable).isNotNull
        assertThat(failure.throwable!!.message).isEqualTo("Referrer is empty")

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Failure when exception occurs during query`() {
        val exception = RuntimeException("Test exception")
        whenever(contentResolver.query(anyOrNull(), isNull(), isNull(), eq(arrayOf(packageName)), isNull()))
            .thenThrow(exception)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable).isEqualTo(exception)
    }
}
