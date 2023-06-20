package io.appmetrica.analytics.impl.db.storage;

import android.content.ContentValues;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.impl.db.connectors.DBConnector;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class BinaryDataHelperTest extends CommonTest {

    private final String mTable = "testTable";

    private final DBConnector mDBConnector = mock(DBConnector.class);
    private BinaryDataHelper mDataHelper;
    private SQLiteDatabase mDatabase = mock(SQLiteDatabase.class);

    @Before
    public void setUp() {
        mDataHelper = new BinaryDataHelper(mDBConnector, mTable);
        doReturn(this.mDatabase).when(mDBConnector).openDb();
    }

    @Test
    public void testInsert() {
        String key = "key";
        byte[] value = {};

        mDataHelper.insert(key, value);

        ContentValues values = new ContentValues();
        values.put(Constants.BinaryDataTable.DATA_KEY, key);
        values.put(Constants.BinaryDataTable.VALUE, value);

        verify(mDatabase).insertWithOnConflict(mTable, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Test
    public void testQuery() {
        String key = "testKey";

        mDataHelper.get(key);

        verify(mDatabase).query(mTable,
                null,
                Constants.BinaryDataTable.DATA_KEY + " = ?",
                new String[]{key}, null, null, null);
    }

    @Test
    public void testNullCursor() {
        String key = "testKey";
        assertThat(mDataHelper.get(key)).isNull();
    }

    @Test
    public void testEmptyCursor() {
        String key = "testKey";
        MatrixCursor cursor = new MatrixCursor(getActualColumnsArray());
        doReturn(cursor).when(mDatabase).query(mTable,
                null,
                Constants.BinaryDataTable.DATA_KEY + " = ?",
                new String[]{key}, null, null, null);
        assertThat(mDataHelper.get(key)).isNull();
    }

    @Test
    public void testCursorWithOneValue() {
        String key = "testKey";
        byte[] value = {};
        MatrixCursor cursor = new MatrixCursor(getActualColumnsArray());
        cursor.addRow(new Object[]{key, value});
        doReturn(cursor).when(mDatabase).query(mTable,
                null,
                Constants.BinaryDataTable.DATA_KEY + " = ?",
                new String[]{key}, null, null, null);
        assertThat(mDataHelper.get(key)).isSameAs(value);
    }

    @Test
    public void testCursorWithMultipleValues() {
        String key = "testKey";
        byte[] value = {};
        MatrixCursor cursor = new MatrixCursor(getActualColumnsArray());
        cursor.addRow(new Object[]{key, value});
        cursor.addRow(new Object[]{"anotherKey", new byte[]{1}});
        doReturn(cursor).when(mDatabase).query(mTable,
                null,
                Constants.BinaryDataTable.DATA_KEY + " = ?",
                new String[]{key}, null, null, null);
        assertThat(mDataHelper.get(key)).isNull();
    }

    @Test
    public void testRemove() {
        String key = "testKey";

        mDataHelper.remove(key);

        verify(mDatabase).delete(mTable,
                Constants.BinaryDataTable.DATA_KEY + " = ?",
                new String[]{key});
    }

    private String[] getActualColumnsArray() {
        return Constants.BinaryDataTable.ACTUAL_COLUMNS.toArray(new String[Constants.BinaryDataTable.ACTUAL_COLUMNS.size()]);
    }
}
