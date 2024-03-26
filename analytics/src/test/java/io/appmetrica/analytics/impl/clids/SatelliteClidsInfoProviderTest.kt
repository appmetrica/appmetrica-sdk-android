package io.appmetrica.analytics.impl.clids

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SatelliteClidsInfoProviderTest : CommonTest() {

    private val authorities = "com.yandex.preinstallsatellite.appmetrica.provider"
    private val uri = "content://$authorities/clids"
    private val columnClidKey = "clid_key"
    private val columnClidValue = "clid_value"

    private val cursor: Cursor = mock()

    private val contentResolver: ContentResolver = mock {
        on { query(Uri.parse(uri), null, null, null, null) } doReturn cursor
    }

    private var context: Context = mock {
        on { contentResolver } doReturn contentResolver
    }

    @get:Rule
    val packageManagerUtilsMockedStaticRule = staticRule<PackageManagerUtils> {
        on { PackageManagerUtils.hasContentProvider(context, authorities) } doReturn true
    }

    private val expectedClids: MutableMap<String, String> = mutableMapOf("clid0" to "0")

    private val provider: SatelliteClidsInfoProvider by setUp { SatelliteClidsInfoProvider(context) }

    @Test
    fun nullContentResolver() {
        whenever(context.contentResolver).thenReturn(null)
        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun nullCursor() {
        whenever(
            contentResolver.query(
                Uri.parse(uri),
                null,
                null,
                null,
                null
            )
        ).thenReturn(null)

        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun emptyCursor() {
        whenever(cursor.moveToNext()).thenReturn(false)
        assertThat(provider.invoke())
            .isEqualTo(ClidsInfo.Candidate(HashMap(), DistributionSource.SATELLITE))
        verify(cursor).close()
    }

    @Test
    fun cursorWithInvalidRow() {
        whenever(cursor.moveToNext()).thenReturn(true, true, false)
        whenever(cursor.getColumnIndexOrThrow(columnClidKey)).thenReturn(-1, 0)
        whenever(cursor.getColumnIndexOrThrow(columnClidValue)).thenReturn(-1, 1)
        whenever(cursor.getString(0)).thenReturn("clid0")
        whenever(cursor.getString(1)).thenReturn("0")

        assertThat(provider.invoke())
            .isEqualTo(ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE))
    }

    @Test
    fun cursorWithIRowThatThrows() {
        whenever(cursor.moveToNext()).thenReturn(true, false)
        whenever(cursor.getColumnIndexOrThrow(columnClidKey)).thenThrow(RuntimeException())
        whenever(cursor.getString(any())).thenReturn("clid0")

        assertThat(provider.invoke())
            .isEqualTo(ClidsInfo.Candidate(HashMap(), DistributionSource.SATELLITE))
    }

    @Test
    fun cursorWithNullKeyRow() {
        whenever(cursor.moveToNext()).thenReturn(true, true, false)
        whenever(cursor.getColumnIndexOrThrow(columnClidKey)).thenReturn(0)
        whenever(cursor.getColumnIndexOrThrow(columnClidValue)).thenReturn(1)
        whenever(cursor.getString(0)).thenReturn(null, "clid0")
        whenever(cursor.getString(1)).thenReturn("1", "0")

        assertThat(provider.invoke())
            .isEqualTo(ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE))
    }

    @Test
    fun cursorWithEmptyKeyRow() {
        whenever(cursor.moveToNext()).thenReturn(true, true, false)
        whenever(cursor.getColumnIndexOrThrow(columnClidKey)).thenReturn(0)
        whenever(cursor.getColumnIndexOrThrow(columnClidValue)).thenReturn(1)
        whenever(cursor.getString(0)).thenReturn("", "clid0")
        whenever(cursor.getString(1)).thenReturn("1", "0")
        assertThat(provider.invoke())
            .isEqualTo(ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE))
    }

    @Test
    fun cursorWithNullValueRow() {
        whenever(cursor.moveToNext()).thenReturn(true, true, false)
        whenever(cursor.getColumnIndexOrThrow(columnClidKey)).thenReturn(0)
        whenever(cursor.getColumnIndexOrThrow(columnClidValue)).thenReturn(1)
        whenever(cursor.getString(0)).thenReturn("clid1", "clid0")
        whenever(cursor.getString(1)).thenReturn(null, "0")
        assertThat(provider.invoke())
            .isEqualTo(ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE))
    }

    @Test
    fun cursorWithEmptyValueRow() {
        whenever(cursor.moveToNext()).thenReturn(true, true, false)
        whenever(cursor.getColumnIndexOrThrow(columnClidKey)).thenReturn(0)
        whenever(cursor.getColumnIndexOrThrow(columnClidValue)).thenReturn(1)
        whenever(cursor.getString(0)).thenReturn("clid1", "clid0")
        whenever(cursor.getString(1)).thenReturn("", "0")
        assertThat(provider.invoke())
            .isEqualTo(ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE))
    }

    @Test
    fun cursorValid() {
        val clids: MutableMap<String, String> = HashMap()
        clids["clid0"] = "0"
        clids["clid1"] = "1"
        whenever(cursor.moveToNext()).thenReturn(true, true, false)
        whenever(cursor.getColumnIndexOrThrow(columnClidKey)).thenReturn(0)
        whenever(cursor.getColumnIndexOrThrow(columnClidValue)).thenReturn(1)
        whenever(cursor.getString(0)).thenReturn("clid1", "clid0")
        whenever(cursor.getString(1)).thenReturn("1", "0")
        assertThat(provider.invoke())
            .isEqualTo(ClidsInfo.Candidate(clids, DistributionSource.SATELLITE))
    }

    @Test
    fun contentProviderWasNotDetected() {
        whenever(PackageManagerUtils.hasContentProvider(context, authorities)).thenReturn(false)

        val clids: MutableMap<String, String> = HashMap()
        clids["clid0"] = "0"
        clids["clid1"] = "1"
        whenever(cursor.moveToNext()).thenReturn(true, true, false)
        whenever(cursor.getColumnIndexOrThrow(columnClidKey)).thenReturn(0)
        whenever(cursor.getColumnIndexOrThrow(columnClidValue)).thenReturn(1)
        whenever(cursor.getString(0)).thenReturn("clid1", "clid0")
        whenever(cursor.getString(1)).thenReturn("1", "0")

        assertThat(provider.invoke()).isNull()
    }
}
