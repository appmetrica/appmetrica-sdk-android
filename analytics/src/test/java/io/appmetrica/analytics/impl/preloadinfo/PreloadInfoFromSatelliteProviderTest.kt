package io.appmetrica.analytics.impl.preloadinfo

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.skyscreamer.jsonassert.JSONAssert

@RunWith(RobolectricTestRunner::class)
class PreloadInfoFromSatelliteProviderTest : CommonTest() {

    private val authorities = "com.yandex.preinstallsatellite.appmetrica.provider"
    private val uri = "content://$authorities/preload_info"
    private val columnTrackingId = "tracking_id"
    private val columnAdditionalParameters = "additional_parameters"

    private val trackingIdColumn = 0
    private val additionalParamsColumn = 1
    private val trackingId = "666777888"

    private val additionalParams = JSONObject().apply {
        put("source", "satellite")
    }

    private val cursor: Cursor = mock {
        on { getColumnIndexOrThrow(columnTrackingId) } doReturn trackingIdColumn
        on { getColumnIndexOrThrow(columnAdditionalParameters) } doReturn additionalParamsColumn
    }

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

    private val provider: PreloadInfoFromSatelliteProvider by setUp { PreloadInfoFromSatelliteProvider(context) }

    @Test
    fun retrievePreloadInfoIfWasNotDetectedContentProvider() {
        whenever(PackageManagerUtils.hasContentProvider(context, authorities)).thenReturn(false)
        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun retrievePreloadInfoNullContentResolver() {
        whenever(context.contentResolver).thenReturn(null)
        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun retrievePreloadInfoNullCursor() {
        whenever(contentResolver.query(Uri.parse(uri), null, null, null, null)).thenReturn(null)
        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun retrievePreloadInfoEmptyCursor() {
        whenever(cursor.moveToFirst()).thenReturn(false)
        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun retrievePreloadInfoNoTrackingId() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn(null)
        whenever(cursor.getString(additionalParamsColumn)).thenReturn(additionalParams.toString())
        checkPreloadInfo(provider.invoke(), null, additionalParams, false)
    }

    @Test
    fun retrievePreloadInfoEmptyTrackingId() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn("")
        whenever(cursor.getString(additionalParamsColumn)).thenReturn(additionalParams.toString())
        checkPreloadInfo(provider.invoke(), "", additionalParams, false)
    }

    @Test
    fun retrievePreloadInfoNotANumberTrackingId() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn("not-a-number-tracking-id")
        whenever(cursor.getString(additionalParamsColumn)).thenReturn(additionalParams.toString())
        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun retrievePreloadInfoNoAdditionalParams() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn(trackingId)
        whenever(cursor.getString(additionalParamsColumn)).thenReturn(null)
        checkPreloadInfo(provider.invoke(), trackingId, JSONObject(), true)
    }

    @Test
    fun retrievePreloadInfoEmptyAdditionalParams() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn(trackingId)
        whenever(cursor.getString(additionalParamsColumn)).thenReturn("")
        checkPreloadInfo(provider.invoke(), trackingId, JSONObject(), true)
    }

    @Test
    fun retrievePreloadInfoBadJsonAdditionalParams() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn(trackingId)
        whenever(cursor.getString(additionalParamsColumn)).thenReturn("not a json")
        checkPreloadInfo(provider.invoke(), trackingId, JSONObject(), true)
    }

    @Test
    fun retrievePreloadInfoValidAdditionalParams() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn(trackingId)
        whenever(cursor.getString(additionalParamsColumn)).thenReturn(additionalParams.toString())
        checkPreloadInfo(provider.invoke(), trackingId, additionalParams, true)
    }

    @Test
    fun retrievePreloadInfoCursorThrowsExceptionForTrackingId() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getColumnIndexOrThrow(columnTrackingId)).thenThrow(RuntimeException())
        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun retrievePreloadInfoCursorThrowsExceptionForAdditionalParams() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn(trackingId)
        whenever(cursor.getColumnIndexOrThrow(columnAdditionalParameters)).thenThrow(RuntimeException())
        assertThat(provider.invoke()).isNull()
    }

    @Test
    fun retrievePreloadInfoCursorHasSeveralRows() {
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.moveToNext()).thenReturn(true)
        whenever(cursor.getString(trackingIdColumn)).thenReturn(trackingId, "aaa")
        whenever(cursor.getString(additionalParamsColumn)).thenReturn(
            additionalParams.toString(),
            JSONObject().put("a", "b").toString()
        )
        checkPreloadInfo(provider.invoke(), trackingId, additionalParams, true)
    }

    private fun checkPreloadInfo(
        actual: PreloadInfoState?,
        trackingId: String?,
        additionalParams: JSONObject,
        wasSet: Boolean
    ) {
        ObjectPropertyAssertions(actual)
            .withIgnoredFields("additionalParameters")
            .checkField("trackingId", trackingId)
            .checkField("wasSet", wasSet)
            .checkField("autoTrackingEnabled", false)
            .checkField("source", DistributionSource.SATELLITE)
            .checkAll()
        JSONAssert.assertEquals(additionalParams, actual?.additionalParameters, true)
    }
}
