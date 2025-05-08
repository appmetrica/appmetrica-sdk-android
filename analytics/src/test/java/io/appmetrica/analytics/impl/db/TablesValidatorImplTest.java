package io.appmetrica.analytics.impl.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TablesValidatorImplTest extends CommonTest {

    @Test
    public void testIsDbSchemeValid() throws Exception {
        final String table1 = "table1";
        final String[] columns1 = new String[]{
            "A", "C", "B"
        };
        final String table2 = "table2";
        final String[] columns2 = new String[]{
            "1", "3", "2"
        };
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor1 = mock(Cursor.class);
        when(cursor1.getColumnNames()).thenReturn(new String[]{
            "A", "B", "C"
        });
        Cursor cursor2 = mock(Cursor.class);
        when(cursor2.getColumnNames()).thenReturn(columns2);
        when(
            database.query(eq(table1),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull()
            )
        ).thenReturn(cursor1);
        when(
            database.query(eq(table2),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull()
            )
        ).thenReturn(cursor2);
        TablesValidatorImpl impl = new TablesValidatorImpl("test", new HashMap<String, List<String>>() {
            {
                put(table1, CollectionUtils.createSortedListWithoutRepetitions(columns1));
                put(table2, CollectionUtils.createSortedListWithoutRepetitions(columns2));
            }
        });
        assertThat(impl.isDbSchemeValid(database)).isTrue();
    }

    @Test
    public void testIsDbSchemeInvalid() throws Exception {
        final String table1 = "table1";
        final String[] columns1 = new String[]{
            "A", "C", "B"
        };
        final String table2 = "table2";
        final String[] columns2 = new String[]{
            "1", "3", "2"
        };
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor1 = mock(Cursor.class);
        when(cursor1.getColumnNames()).thenReturn(new String[]{
            "A", "2", "C"
        });
        Cursor cursor2 = mock(Cursor.class);
        when(cursor2.getColumnNames()).thenReturn(columns2);
        when(
            database.query(eq(table1),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull()
            )
        ).thenReturn(cursor1);
        when(
            database.query(eq(table2),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull()
            )
        ).thenReturn(cursor2);
        TablesValidatorImpl impl = new TablesValidatorImpl("test", new HashMap<String, List<String>>() {
            {
                put(table1, CollectionUtils.createSortedListWithoutRepetitions(columns1));
                put(table2, CollectionUtils.createSortedListWithoutRepetitions(columns2));
            }
        });
        assertThat(impl.isDbSchemeValid(database)).isFalse();
    }

    @Test
    public void testDbWithoutTable() throws Exception {
        final String table1 = "table1";
        final String[] columns1 = new String[]{
            "A", "C", "B"
        };
        final String table2 = "table2";
        final String[] columns2 = new String[]{
            "1", "3", "2"
        };
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor1 = mock(Cursor.class);
        when(cursor1.getColumnNames()).thenReturn(new String[]{
            "A", "B", "C"
        });
        when(
            database.query(eq(table1),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull()
            )
        ).thenReturn(cursor1);
        TablesValidatorImpl impl = new TablesValidatorImpl("test", new HashMap<String, List<String>>() {
            {
                put(table1, CollectionUtils.createSortedListWithoutRepetitions(columns1));
                put(table2, CollectionUtils.createSortedListWithoutRepetitions(columns2));
            }
        });
        assertThat(impl.isDbSchemeValid(database)).isFalse();
    }

    @Test
    public void testCheckValidCursorColumns() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getColumnNames()).thenReturn(new String[]{
            "A",
            "B"
        });
        TablesValidatorImpl validator = new TablesValidatorImpl("test", mock(HashMap.class));
        assertThat(validator.checkCursorColumns(
            cursor,
            "table_name",
            CollectionUtils.createSortedListWithoutRepetitions("B", "A")
        )).isTrue();
    }

    @Test
    public void testCheckInvalidCursorColumns() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getColumnNames()).thenReturn(new String[]{
            "A",
            "B"
        });
        if (BuildConfig.DEBUG == false) {
            TablesValidatorImpl validator = new TablesValidatorImpl("test", mock(HashMap.class));
            assertThat(validator.checkCursorColumns(
                cursor,
                "table_name",
                CollectionUtils.createSortedListWithoutRepetitions("B")
            )).isFalse();
        }
    }
}
