package io.appmetrica.analytics.coreutils.internal.db;

import android.content.ContentValues;
import android.database.MatrixCursor;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
public class DBUtilsTest extends CommonTest {

    public static final String[] COLUMNS = new String[]{
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
            "FLOAT"
    };

    private static final Class<?>[] TYPES = new Class<?>[]{
            Long.class,
            Long.class,
            Long.class,
            Long.class,
            String.class,
            Long.class,
            Long.class,
            String.class,
            Double.class
    };

    @Test
    public void testMyCursorRowToContentValues() {
        spy(DBUtils.class);
        MatrixCursor reportCursor = new MatrixCursor(COLUMNS) {
            {
                newRow().add(1).add(234).add(0).add(0).add("{}").add(0).add(0).add("sadfsdfd").add(1.2);
            }
        };
        ContentValues contentValues = new ContentValues();
        assertThat(reportCursor.moveToFirst()).isTrue();
        DBUtils.enhancedCursorRowToContentValues(reportCursor, contentValues);
        assertThat(reportCursor.getColumnCount()).isEqualTo(contentValues.size());
        for (int i = 0; i < TYPES.length; i++) {
            assertThat(contentValues.get(COLUMNS[i])).isInstanceOf(TYPES[i]);
        }
    }

}
