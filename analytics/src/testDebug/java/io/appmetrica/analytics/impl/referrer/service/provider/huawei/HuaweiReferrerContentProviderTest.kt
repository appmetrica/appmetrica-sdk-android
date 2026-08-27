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
        val installTimestamp = 1234567900L

        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(installReferrer)
        whenever(cursor.getLong(1)).thenReturn(clickTimestamp)
        whenever(cursor.getLong(2)).thenReturn(installTimestamp)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Success::class.java)
        val success = result as ReferrerResult.Success
        assertThat(success.referrerInfo.installReferrer).isEqualTo(installReferrer)
        assertThat(success.referrerInfo.referrerClickTimestampSeconds).isEqualTo(clickTimestamp)
        assertThat(success.referrerInfo.installBeginTimestampSeconds).isEqualTo(installTimestamp)
        assertThat(success.referrerInfo.source).isEqualTo(ReferrerInfo.Source.HMS)

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
        assertThat(failure.throwable?.message).isEqualTo("Not found content provider")
    }

    @Test
    fun `getReferrer returns Failure when cursor is empty`() {
        whenever(cursor.moveToFirst()).thenReturn(false)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable?.message).isEqualTo("Cursor is empty")

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Failure when installReferrer is null`() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(null)

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable?.message).isEqualTo("Referrer is empty")

        verify(cursor).close()
    }

    @Test
    fun `getReferrer returns Failure when installReferrer is blank`() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn("   ")

        val result = contentProvider.getReferrer(context)

        assertThat(result).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = result as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to get referrer from huawei content provider")
        assertThat(failure.throwable?.message).isEqualTo("Referrer is empty")

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
