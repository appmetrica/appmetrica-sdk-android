package io.appmetrica.analytics.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SatelliteClidsInfoProviderTest extends CommonTest {

    private static final String URI = "content://com.yandex.preinstallsatellite.appmetrica.provider/clids";
    private static final String COL_CLID_kEY = "clid_key";
    private static final String COL_CLID_VALUE = "clid_value";

    @Mock
    private ContentResolver contentResolver;
    @Mock
    private Cursor cursor;
    private final Map<String, String> expectedClids = new HashMap<>();
    private Context context;
    private SatelliteClidsInfoProvider provider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(context.getContentResolver()).thenReturn(contentResolver);
        when(contentResolver.query(
                Uri.parse(URI),
                null,
                null,
                null,
                null
        )).thenReturn(cursor);
        expectedClids.put("clid0", "0");
        provider = new SatelliteClidsInfoProvider(context);
    }

    @Test
    public void nullContentResolver() {
        when(context.getContentResolver()).thenReturn(null);
        assertThat(provider.invoke()).isNull();
    }

    @Test
    public void nullCursor() {
        when(contentResolver.query(
                Uri.parse(URI),
                null,
                null,
                null,
                null
        )).thenReturn(null);
        assertThat(provider.invoke()).isNull();
    }

    @Test
    public void emptyCursor() {
        when(cursor.moveToNext()).thenReturn(false);
        assertThat(provider.invoke()).isEqualTo(new ClidsInfo.Candidate(new HashMap<String, String>(), DistributionSource.SATELLITE));
        verify(cursor).close();
    }

    @Test
    public void cursorWithInvalidRow() {
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getColumnIndexOrThrow(COL_CLID_kEY)).thenReturn(-1, 0);
        when(cursor.getColumnIndexOrThrow(COL_CLID_VALUE)).thenReturn(-1, 1);
        when(cursor.getString(0)).thenReturn("clid0");
        when(cursor.getString(1)).thenReturn("0");
        assertThat(provider.invoke()).isEqualTo(new ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE));
    }

    @Test
    public void cursorWithIRowThatThrows() {
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getColumnIndexOrThrow(COL_CLID_kEY)).thenThrow(new RuntimeException());
        when(cursor.getString(anyInt())).thenReturn("clid0");
        assertThat(provider.invoke()).isEqualTo(new ClidsInfo.Candidate(new HashMap<String, String>(), DistributionSource.SATELLITE));
    }

    @Test
    public void cursorWithNullKeyRow() {
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getColumnIndexOrThrow(COL_CLID_kEY)).thenReturn(0);
        when(cursor.getColumnIndexOrThrow(COL_CLID_VALUE)).thenReturn(1);
        when(cursor.getString(0)).thenReturn(null, "clid0");
        when(cursor.getString(1)).thenReturn("1", "0");
        assertThat(provider.invoke()).isEqualTo(new ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE));
    }

    @Test
    public void cursorWithEmptyKeyRow() {
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getColumnIndexOrThrow(COL_CLID_kEY)).thenReturn(0);
        when(cursor.getColumnIndexOrThrow(COL_CLID_VALUE)).thenReturn(1);
        when(cursor.getString(0)).thenReturn("", "clid0");
        when(cursor.getString(1)).thenReturn("1", "0");
        assertThat(provider.invoke()).isEqualTo(new ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE));
    }

    @Test
    public void cursorWithNullValueRow() {
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getColumnIndexOrThrow(COL_CLID_kEY)).thenReturn(0);
        when(cursor.getColumnIndexOrThrow(COL_CLID_VALUE)).thenReturn(1);
        when(cursor.getString(0)).thenReturn("clid1", "clid0");
        when(cursor.getString(1)).thenReturn(null, "0");
        assertThat(provider.invoke()).isEqualTo(new ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE));
    }

    @Test
    public void cursorWithEmptyValueRow() {
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getColumnIndexOrThrow(COL_CLID_kEY)).thenReturn(0);
        when(cursor.getColumnIndexOrThrow(COL_CLID_VALUE)).thenReturn(1);
        when(cursor.getString(0)).thenReturn("clid1", "clid0");
        when(cursor.getString(1)).thenReturn("", "0");
        assertThat(provider.invoke()).isEqualTo(new ClidsInfo.Candidate(expectedClids, DistributionSource.SATELLITE));
    }

    @Test
    public void cursorValid() {
        Map<String, String> clids = new HashMap<>();
        clids.put("clid0", "0");
        clids.put("clid1", "1");
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getColumnIndexOrThrow(COL_CLID_kEY)).thenReturn(0);
        when(cursor.getColumnIndexOrThrow(COL_CLID_VALUE)).thenReturn(1);
        when(cursor.getString(0)).thenReturn("clid1", "clid0");
        when(cursor.getString(1)).thenReturn("1", "0");
        assertThat(provider.invoke()).isEqualTo(new ClidsInfo.Candidate(clids, DistributionSource.SATELLITE));
    }
}
