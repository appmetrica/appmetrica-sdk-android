package io.appmetrica.analytics.impl.preloadinfo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PreloadInfoFromSatelliteProviderTest extends CommonTest {

    private static final String URI = "content://com.yandex.preinstallsatellite.appmetrica.provider/preload_info";
    private static final String COL_TRACKING_ID = "tracking_id";
    private static final String COL_ADDITIONAL_PARAMETERS = "additional_parameters";

    @Mock
    private ContentResolver contentResolver;
    @Mock
    private Cursor cursor;
    private final int trackingIdColumn = 0;
    private final int additionalParamsColumn = 1;
    private final String trackingId = "666777888";
    private final JSONObject additionalParams = new JSONObject();
    private Context context;
    private PreloadInfoFromSatelliteProvider provider;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        additionalParams.put("source", "satellite");
        context = TestUtils.createMockedContext();
        when(context.getContentResolver()).thenReturn(contentResolver);
        when(contentResolver.query(Uri.parse(URI),  null, null, null, null)).thenReturn(cursor);
        when(cursor.getColumnIndexOrThrow(COL_TRACKING_ID)).thenReturn(trackingIdColumn);
        when(cursor.getColumnIndexOrThrow(COL_ADDITIONAL_PARAMETERS)).thenReturn(additionalParamsColumn);
        provider = new PreloadInfoFromSatelliteProvider(context);
    }

    @Test
    public void retrievePreloadInfoNullContentResolver() throws Exception {
        when(context.getContentResolver()).thenReturn(null);
        assertThat(provider.invoke()).isNull();
    }

    @Test
    public void retrievePreloadInfoNullCursor() throws Exception {
        when(contentResolver.query(Uri.parse(URI),  null, null, null, null)).thenReturn(null);
        assertThat(provider.invoke()).isNull();
    }

    @Test
    public void retrievePreloadInfoEmptyCursor() throws Exception {
        when(cursor.moveToFirst()).thenReturn(false);
        assertThat(provider.invoke()).isNull();
    }

    @Test
    public void retrievePreloadInfoNoTrackingId() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(trackingIdColumn)).thenReturn(null);
        when(cursor.getString(additionalParamsColumn)).thenReturn(additionalParams.toString());
        checkPreloadInfo(
                provider.invoke(),
                null,
                additionalParams,
                false
        );
    }

    @Test
    public void retrievePreloadInfoEmptyTrackingId() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(trackingIdColumn)).thenReturn("");
        when(cursor.getString(additionalParamsColumn)).thenReturn(additionalParams.toString());
        checkPreloadInfo(
                provider.invoke(),
                "",
                additionalParams,
                false
        );
    }

    @Test
    public void retrievePreloadInfoNoAdditionalParams() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(trackingIdColumn)).thenReturn(trackingId);
        when(cursor.getString(additionalParamsColumn)).thenReturn(null);
        checkPreloadInfo(
                provider.invoke(),
                trackingId,
                new JSONObject(),
                true
        );
    }

    @Test
    public void retrievePreloadInfoEmptyAdditionalParams() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(trackingIdColumn)).thenReturn(trackingId);
        when(cursor.getString(additionalParamsColumn)).thenReturn("");
        checkPreloadInfo(
                provider.invoke(),
                trackingId,
                new JSONObject(),
                true
        );
    }

    @Test
    public void retrievePreloadInfoBadJsonAdditionalParams() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(trackingIdColumn)).thenReturn(trackingId);
        when(cursor.getString(additionalParamsColumn)).thenReturn("not a json");
        checkPreloadInfo(
                provider.invoke(),
                trackingId,
                new JSONObject(),
                true
        );
    }

    @Test
    public void retrievePreloadInfoValidAdditionalParams() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(trackingIdColumn)).thenReturn(trackingId);
        when(cursor.getString(additionalParamsColumn)).thenReturn(additionalParams.toString());
        checkPreloadInfo(
                provider.invoke(),
                trackingId,
                additionalParams,
                true
        );
    }

    @Test
    public void retrievePreloadInfoCursorThrowsExceptionForTrackingId() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getColumnIndexOrThrow(COL_TRACKING_ID)).thenThrow(new RuntimeException());
        assertThat(provider.invoke()).isNull();
    }

    @Test
    public void retrievePreloadInfoCursorThrowsExceptionForAdditionalParams() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(trackingIdColumn)).thenReturn(trackingId);
        when(cursor.getColumnIndexOrThrow(COL_ADDITIONAL_PARAMETERS)).thenThrow(new RuntimeException());
        assertThat(provider.invoke()).isNull();
    }

    @Test
    public void retrievePreloadInfoCursorHasSeveralRows() throws Exception {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.moveToNext()).thenReturn(true);
        when(cursor.getString(trackingIdColumn)).thenReturn(trackingId, "aaa");
        when(cursor.getString(additionalParamsColumn)).thenReturn(
                additionalParams.toString(),
                new JSONObject().put("a", "b").toString()
        );
        checkPreloadInfo(
                provider.invoke(),
                trackingId,
                additionalParams,
                true
        );
    }

    private void checkPreloadInfo(@NonNull PreloadInfoState actual,
                                  @Nullable String trackingId,
                                  @NonNull JSONObject additionalParams,
                                  boolean wasSet) throws Exception {
        ObjectPropertyAssertions(actual)
                .withIgnoredFields("additionalParameters")
                .checkField("trackingId", trackingId)
                .checkField("wasSet", wasSet)
                .checkField("autoTrackingEnabled", false)
                .checkField("source", DistributionSource.SATELLITE)
                .checkAll();
        JSONAssert.assertEquals(additionalParams, actual.additionalParameters, true);
    }
}
