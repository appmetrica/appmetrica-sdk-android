package io.appmetrica.analytics.impl.db.storage;

import android.content.ContentValues;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.db.DatabaseManagerProvider;
import io.appmetrica.analytics.impl.db.DatabaseStorage;
import io.appmetrica.analytics.impl.db.connectors.SimpleDBConnector;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static io.appmetrica.analytics.impl.db.constants.Constants.KeyValueTable.BOOL;
import static io.appmetrica.analytics.impl.db.constants.Constants.KeyValueTable.FLOAT;
import static io.appmetrica.analytics.impl.db.constants.Constants.KeyValueTable.INT;
import static io.appmetrica.analytics.impl.db.constants.Constants.KeyValueTable.LONG;
import static io.appmetrica.analytics.impl.db.constants.Constants.KeyValueTable.STRING;
import static io.appmetrica.analytics.impl.db.constants.Constants.PreferencesTable.TABLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class KeyValueTableDbHelperTest extends CommonTest {

    public static final String[] COLUMNS = new String[]{
        Constants.PreferencesTable.KeyValueTableEntry.FIELD_KEY,
        Constants.PreferencesTable.KeyValueTableEntry.FIELD_VALUE,
        Constants.PreferencesTable.KeyValueTableEntry.FIELD_TYPE
    };
    public static final String TEST_STRING = "String";
    public static final long TEST_LONG = Long.MAX_VALUE;
    public static final int TEST_INT = Integer.MAX_VALUE;
    public static final float TEST_FLOAT = Float.MAX_VALUE;
    public static final boolean TEST_BOOL_FALSE = false;
    public static final boolean TEST_BOOL_TRUE = true;
    public static final String KEY_LONG = "key_long";
    public static final String KEY_INT = "key_int";
    public static final String KEY_STRING = "key_string";
    public static final String KEY_BOOLEAN = "key_boolean";
    public static final String KEY_FLOAT = "key_float";

    private KeyValueTableDbHelper dbHelper;
    private KeyValueTableDbHelper newDbHelper;
    private DatabaseStorage mDbStorage;
    private SQLiteDatabase mDatabase;
    public static final String KEY_TO_REMOVE_LONG = "key_long2";
    public static final String KEY_TO_REMOVE_INT = "key_int2";
    public static final String KEY_TO_REMOVE_STRING = "key_string2";
    public static final String KEY_TO_REMOVE_BOOL = "key_boolean2";
    public static final String KEY_TO_REMOVE_FLOAT = "key_float2";
    private MatrixCursor mPrefsCursor;
    static final DatabaseManagerProvider sDatabaseManagerProvider = Constants.getDatabaseManagerProvider();

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    public static class KeyValueTableDbHelperParametrizedTest extends CommonTest {
        protected KeyValueTableDbHelper dbHelper;
        protected KeyValueTableDbHelper newDbHelper;
        protected DatabaseStorage mDbStorage;
        protected SQLiteDatabase mDatabase;
        protected MatrixCursor mPrefsCursor;

        @Rule
        public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

        @Before
        public void setUp() {
            mDbStorage = mock(DatabaseStorage.class);

            mDatabase = mock(SQLiteDatabase.class);
            doReturn(mDatabase).when(mDbStorage).getReadableDatabase();
            doReturn(mDatabase).when(mDbStorage).getWritableDatabase();

            mPrefsCursor = new MatrixCursor(COLUMNS);
            doReturn(mPrefsCursor).when(mDatabase).query(nullable(String.class), nullable(String[].class), nullable(String.class),
                nullable(String[].class), nullable(String.class), nullable(String.class), nullable(String.class));
        }

        @After
        public void tearDown() throws Exception {
            if (dbHelper != null) {
                dbHelper.close();
            }
            if (newDbHelper != null) {
                newDbHelper.close();
            }
        }

    }

    public static class KeyValueTableDbHelperParametrizedTestForFloat extends KeyValueTableDbHelperParametrizedTest {

        protected Object mStoredValue;
        protected int mType;
        protected float mExpectedValue;

        public KeyValueTableDbHelperParametrizedTestForFloat(Object storedValue, int type, float expectedValue) {
            mStoredValue = storedValue;
            mType = type;
            mExpectedValue = expectedValue;
        }
    }

    @Before
    public void setUp() throws Exception {
        mDbStorage = mock(DatabaseStorage.class);

        mDatabase = mock(SQLiteDatabase.class);
        doReturn(mDatabase).when(mDbStorage).getReadableDatabase();
        doReturn(mDatabase).when(mDbStorage).getWritableDatabase();

        mPrefsCursor = new MatrixCursor(COLUMNS);
        doReturn(mPrefsCursor).when(mDatabase).query(nullable(String.class), nullable(String[].class), nullable(String.class),
            nullable(String[].class), nullable(String.class), nullable(String.class), nullable(String.class));
    }

    @After
    public void tearDown() {
        if (mPrefsCursor != null) {
            mPrefsCursor.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (newDbHelper != null) {
            newDbHelper.close();
        }
    }

    @Test
    public void testReadValuesOnStart() throws Exception {
        mPrefsCursor.newRow().add(KEY_LONG).add(TEST_LONG).add(LONG);
        mPrefsCursor.newRow().add(KEY_STRING).add(TEST_STRING).add(STRING);
        mPrefsCursor.newRow().add(KEY_BOOLEAN).add(TEST_BOOL_TRUE).add(BOOL);
        mPrefsCursor.newRow().add(KEY_INT).add(TEST_INT).add(INT);
        mPrefsCursor.moveToPosition(-1);

        dbHelper = createHelper(mDbStorage);

        assertThat(dbHelper.getBoolean(KEY_BOOLEAN, false)).isTrue();
        assertThat(dbHelper.getString(KEY_STRING, null)).isEqualTo(TEST_STRING);
        assertThat(dbHelper.getLong(KEY_LONG, -1)).isEqualTo(TEST_LONG);
        assertThat(dbHelper.getInt(KEY_INT, -1)).isEqualTo(TEST_INT);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class ReadFloatOnStartTests extends KeyValueTableDbHelperParametrizedTestForFloat {

        @ParameterizedRobolectricTestRunner.Parameters(name = "Should return value = {3} for stored value = {1} with type = {2} ")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {1f, "1f", FLOAT, 1f},
                {-1f, "-1f", FLOAT, -1f},
                {1.1f, "1.1f", FLOAT, 1.1f},
                {-1.1f, "-1.1f", FLOAT, -1.1f},
                {0f, "0f", FLOAT, 0f},
                {0, "0", FLOAT, 0f},
                {0L, "0L", FLOAT, 0f},
                {0f, "0f", INT, Float.NEGATIVE_INFINITY},
                {0f, "0f", BOOL, Float.NEGATIVE_INFINITY},
                {0f, "0f", LONG, Float.NEGATIVE_INFINITY},
                {0f, "0f", STRING, Float.NEGATIVE_INFINITY},
                {1L, "1L", LONG, Float.NEGATIVE_INFINITY},
                {1L, "1L", FLOAT, 1f},
                {"sads", "sads", STRING, Float.NEGATIVE_INFINITY},
                {"asdas", "asdas", FLOAT, Float.NEGATIVE_INFINITY},
                {true, "true", BOOL, Float.NEGATIVE_INFINITY},
                {true, "true", FLOAT, Float.NEGATIVE_INFINITY},
                {1, "1", INT, Float.NEGATIVE_INFINITY},
                {null, "null", FLOAT, Float.NEGATIVE_INFINITY},
                {null, "null", INT, Float.NEGATIVE_INFINITY},
                {null, "null", STRING, Float.NEGATIVE_INFINITY}
            });
        }

        public ReadFloatOnStartTests(Object storedValue, String textPresentation, int type, float expectedValue) {
            super(storedValue, type, expectedValue);
        }

        @Before
        public void setUp() {
            super.setUp();
        }

        @Test
        public void test() {
            mPrefsCursor.newRow().add(KEY_FLOAT).add(mStoredValue).add(mType);
            mPrefsCursor.moveToPosition(-1);

            dbHelper = createHelper(mDbStorage);

            float storedValue = Float.NEGATIVE_INFINITY;
            try {
                storedValue = dbHelper.getFloat(KEY_FLOAT, Float.NEGATIVE_INFINITY);
            } catch (Exception e) {
            }
            assertThat(storedValue).isEqualTo(mExpectedValue);
        }
    }

    @Test
    public void testRemoveInMemory() throws Exception {
        mPrefsCursor.newRow().add(KEY_LONG).add(TEST_LONG).add(LONG);
        mPrefsCursor.newRow().add(KEY_INT).add(TEST_INT).add(INT);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_LONG).add(TEST_LONG).add(LONG);
        mPrefsCursor.newRow().add(KEY_STRING).add(TEST_STRING).add(STRING);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_INT).add(TEST_INT).add(INT);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_STRING).add(TEST_STRING).add(STRING);
        mPrefsCursor.newRow().add(KEY_BOOLEAN).add(TEST_BOOL_TRUE).add(BOOL);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_BOOL).add(TEST_BOOL_FALSE).add(BOOL);
        mPrefsCursor.newRow().add(KEY_FLOAT).add(TEST_FLOAT).add(FLOAT);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_FLOAT).add(TEST_FLOAT).add(FLOAT);
        mPrefsCursor.moveToPosition(-1);

        dbHelper = createHelper(mDbStorage);
        dbHelper.remove(KEY_TO_REMOVE_LONG);
        dbHelper.remove(KEY_TO_REMOVE_BOOL);
        dbHelper.remove(KEY_TO_REMOVE_STRING);
        dbHelper.remove(KEY_TO_REMOVE_INT);
        dbHelper.remove(KEY_TO_REMOVE_FLOAT);

        assertThat(dbHelper.getBoolean(KEY_TO_REMOVE_BOOL, false)).isFalse();
        assertThat(dbHelper.getLong(KEY_TO_REMOVE_LONG, -1)).isEqualTo(-1);
        assertThat(dbHelper.getLong(KEY_TO_REMOVE_INT, -1)).isEqualTo(-1);
        assertThat(dbHelper.getString(KEY_TO_REMOVE_STRING, null)).isNull();
        assertThat(dbHelper.getFloat(KEY_TO_REMOVE_FLOAT, Float.NEGATIVE_INFINITY)).isEqualTo(Float.NEGATIVE_INFINITY);

        assertThat(dbHelper.getBoolean(KEY_BOOLEAN, false)).isTrue();
        assertThat(dbHelper.getLong(KEY_LONG, -1)).isEqualTo(Long.MAX_VALUE);
        assertThat(dbHelper.getInt(KEY_INT, -1)).isEqualTo(TEST_INT);
        assertThat(dbHelper.getString(KEY_STRING, null)).isEqualTo(TEST_STRING);
        assertThat(dbHelper.getFloat(KEY_FLOAT, Float.NEGATIVE_INFINITY)).isEqualTo(TEST_FLOAT);
    }

    @Test
    public void testRemoveInDb() throws Exception {
        mPrefsCursor.newRow().add(KEY_LONG).add(TEST_LONG).add(LONG);
        mPrefsCursor.newRow().add(KEY_INT).add(TEST_INT).add(INT);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_LONG).add(TEST_LONG).add(LONG);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_INT).add(TEST_INT).add(INT);
        mPrefsCursor.newRow().add(KEY_STRING).add(TEST_STRING).add(STRING);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_STRING).add(TEST_STRING).add(STRING);
        mPrefsCursor.newRow().add(KEY_BOOLEAN).add(TEST_BOOL_TRUE).add(BOOL);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_BOOL).add(TEST_BOOL_FALSE).add(BOOL);
        mPrefsCursor.newRow().add(KEY_FLOAT).add(TEST_FLOAT).add(FLOAT);
        mPrefsCursor.newRow().add(KEY_TO_REMOVE_FLOAT).add(TEST_FLOAT).add(FLOAT);
        mPrefsCursor.moveToPosition(-1);

        dbHelper = createHelper(mDbStorage);
        dbHelper.remove(KEY_TO_REMOVE_LONG)
            .remove(KEY_TO_REMOVE_BOOL)
            .remove(KEY_TO_REMOVE_STRING)
            .remove(KEY_TO_REMOVE_INT)
            .remove(KEY_TO_REMOVE_FLOAT);

        ArgumentCaptor<String[]> whereArgsCaptor = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<String> tableCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> whereCaptor = ArgumentCaptor.forClass(String.class);

        verify(mDatabase, times(5)).delete(tableCaptor.capture(), whereCaptor.capture(), whereArgsCaptor.capture());

        assertThat(tableCaptor.getValue()).isEqualTo(TABLE_NAME);
        assertThat(whereCaptor.getValue()).isEqualTo(Constants.PreferencesTable.DELETE_WHERE_KEY);
        List<String> removedKeys = new ArrayList<String>();
        for (String[] whereArgs : whereArgsCaptor.getAllValues()) {
            removedKeys.add(whereArgs[0]);
        }
        assertThat(removedKeys).contains(KEY_TO_REMOVE_LONG);
        assertThat(removedKeys).contains(KEY_TO_REMOVE_BOOL);
        assertThat(removedKeys).contains(KEY_TO_REMOVE_STRING);
        assertThat(removedKeys).contains(KEY_TO_REMOVE_INT);
        assertThat(removedKeys).contains(KEY_TO_REMOVE_FLOAT);

        assertThat(dbHelper.getBoolean(KEY_BOOLEAN, false)).isEqualTo(TEST_BOOL_TRUE);
        assertThat(dbHelper.getLong(KEY_LONG, -1)).isEqualTo(TEST_LONG);
        assertThat(dbHelper.getInt(KEY_INT, -1)).isEqualTo(TEST_INT);
        assertThat(dbHelper.getString(KEY_STRING, null)).isEqualTo(TEST_STRING);
        assertThat(dbHelper.getFloat(KEY_FLOAT, Float.NEGATIVE_INFINITY)).isEqualTo(TEST_FLOAT);

        assertThat(dbHelper.getBoolean(KEY_TO_REMOVE_BOOL, false)).isFalse();
        assertThat(dbHelper.getLong(KEY_TO_REMOVE_LONG, -1)).isEqualTo(-1);
        assertThat(dbHelper.getLong(KEY_TO_REMOVE_INT, -1)).isEqualTo(-1);
        assertThat(dbHelper.getString(KEY_TO_REMOVE_STRING, null)).isNull();
        assertThat(dbHelper.getFloat(KEY_TO_REMOVE_FLOAT, Float.NEGATIVE_INFINITY)).isEqualTo(Float.NEGATIVE_INFINITY);
    }

    @Test
    public void testInsertInMemory() throws Exception {
        dbHelper = createHelper(mDbStorage);

        dbHelper.put(KEY_LONG, TEST_LONG)
            .put(KEY_STRING, TEST_STRING)
            .put(KEY_BOOLEAN, TEST_BOOL_TRUE)
            .put(KEY_INT, TEST_INT);

        assertThat(dbHelper.getBoolean(KEY_BOOLEAN, false)).isTrue();
        assertThat(dbHelper.getLong(KEY_LONG, -1)).isEqualTo(Long.MAX_VALUE);
        assertThat(dbHelper.getInt(KEY_INT, -1)).isEqualTo(TEST_INT);
        assertThat(dbHelper.getString(KEY_STRING, null)).isEqualTo(TEST_STRING);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class InsertFloatInMemoryTests extends KeyValueTableDbHelperParametrizedTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "Should return expected value = {2} for input value = {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {1f, "1f", 1f},
                {-1f, "-1f", -1f},
                {1.1f, "1.1f", 1.1f},
                {-1.1f, "-1.1f", -1.1f},
                {0f, "0f", 0.0f},
                {0, "0", Float.NEGATIVE_INFINITY},
                {0L, "0L", Float.NEGATIVE_INFINITY},
                {1L, "1L", Float.NEGATIVE_INFINITY},
                {"sads", "sads", Float.NEGATIVE_INFINITY},
                {"", "", Float.NEGATIVE_INFINITY},
                {true, "true", Float.NEGATIVE_INFINITY},
                {false, "false", Float.NEGATIVE_INFINITY},
                {1, "1", Float.NEGATIVE_INFINITY},
            });
        }

        private final Object mInputValue;
        private final float mExpectedValue;

        public InsertFloatInMemoryTests(Object inputValue, String inputPresentation, float expectedValue) {
            mInputValue = inputValue;
            mExpectedValue = expectedValue;
        }

        @Test
        public void insertFloatInMemory() {
            dbHelper = createHelper(mDbStorage);
            dbHelper.putValue(KEY_FLOAT, mInputValue);
            float value = Float.NEGATIVE_INFINITY;
            try {
                value = dbHelper.getFloat(KEY_FLOAT, Float.NEGATIVE_INFINITY);
            } catch (Exception e) {
            }
            assertThat(value).isEqualTo(mExpectedValue);
        }
    }

    @Test
    public void insertLongInDb() throws Exception {
        DatabaseStorage storage = new DatabaseStorage(
            RuntimeEnvironment.getApplication(),
            "test",
            sDatabaseManagerProvider.buildServiceDatabaseManager()
        );
        dbHelper = createHelper(storage);
        dbHelper.put(KEY_LONG, TEST_LONG);


        newDbHelper = createHelper(storage);

        assertThat(newDbHelper.getLong(KEY_LONG, 0)).isEqualTo(TEST_LONG);
    }

    @Test
    public void testInsertIntInDb() throws Exception {
        DatabaseStorage storage = new DatabaseStorage(RuntimeEnvironment.getApplication(), "test", sDatabaseManagerProvider.buildServiceDatabaseManager());
        dbHelper = createHelper(storage);
        dbHelper.put(KEY_INT, TEST_INT);

        newDbHelper = createHelper(storage);

        assertThat(newDbHelper.getInt(KEY_INT, 0)).isEqualTo(TEST_INT);
    }

    @Test
    public void testInsertStringInDb() throws Exception {
        DatabaseStorage storage = new DatabaseStorage(RuntimeEnvironment.getApplication(), "test", sDatabaseManagerProvider.buildServiceDatabaseManager());
        dbHelper = createHelper(storage);

        dbHelper.put(KEY_STRING, TEST_STRING);

        newDbHelper = createHelper(storage);

        assertThat(newDbHelper.getString(KEY_STRING, null)).isEqualTo(TEST_STRING);
    }

    @Test
    public void testInsertInDb() throws Exception {
        DatabaseStorage storage = new DatabaseStorage(RuntimeEnvironment.getApplication(), "test", sDatabaseManagerProvider.buildServiceDatabaseManager());
        dbHelper = createHelper(storage);
        dbHelper.put(KEY_BOOLEAN, true);

        newDbHelper = createHelper(storage);

        assertThat(newDbHelper.getBoolean(KEY_BOOLEAN, false)).isTrue();
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class InsertFloatInDbTests extends KeyValueTableDbHelperParametrizedTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "Should return expected value = {2} for input value = {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {1f, "1f", 1f},
                {-1f, "-1f", -1f},
                {1.1f, "1.1f", 1.1f},
                {-1.1f, "-1.1f", -1.1f},
                {0f, "0f", 0.0f},
                {0, "0", Float.NEGATIVE_INFINITY},
                {0L, "0L", Float.NEGATIVE_INFINITY},
                {1L, "1L", Float.NEGATIVE_INFINITY},
                {"sads", "sads", Float.NEGATIVE_INFINITY},
                {"", "", Float.NEGATIVE_INFINITY},
                {true, "true", Float.NEGATIVE_INFINITY},
                {false, "false", Float.NEGATIVE_INFINITY},
                {1, "1", Float.NEGATIVE_INFINITY},
            });
        }

        private final Object mInputValue;
        private final float mExpectedValue;

        public InsertFloatInDbTests(Object inputValue, String inputPresentation, float expectedValue) {
            mInputValue = inputValue;
            mExpectedValue = expectedValue;
        }

        @Test
        public void test() {
            DatabaseStorage storage = new DatabaseStorage(RuntimeEnvironment.getApplication(), "test", sDatabaseManagerProvider.buildServiceDatabaseManager());
            dbHelper = createHelper(storage);
            dbHelper.putValue(KEY_FLOAT, mInputValue);

            newDbHelper = createHelper(storage);

            float actualValue = Float.NEGATIVE_INFINITY;

            try {
                actualValue = newDbHelper.getFloat(KEY_FLOAT, Float.NEGATIVE_INFINITY);
            } catch (Exception e) {
            }

            assertThat(actualValue).isEqualTo(mExpectedValue);
        }
    }

    @Test
    public void testBooleanWrite() throws Exception {
        dbHelper = createHelper(mDbStorage);
        dbHelper.put(KEY_BOOLEAN, true);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentValues> valuesArgsCaptor = ArgumentCaptor.forClass(ContentValues.class);
        ArgumentCaptor<Integer> arg4 = ArgumentCaptor.forClass(Integer.class);

        verify(mDatabase, times(1)).insertWithOnConflict(arg1.capture(), arg2.capture(), valuesArgsCaptor.capture(), arg4.capture());

        Integer type = valuesArgsCaptor.getValue().getAsInteger(Constants.KeyValueTable.KeyValueTableEntry.FIELD_TYPE);

        assertThat(type).isEqualTo(Constants.KeyValueTable.BOOL);
    }

    @Test
    public void testLongWrite() throws Exception {
        dbHelper = createHelper(mDbStorage);
        dbHelper.put(KEY_LONG, 111L);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentValues> valuesArgsCaptor = ArgumentCaptor.forClass(ContentValues.class);
        ArgumentCaptor<Integer> arg4 = ArgumentCaptor.forClass(Integer.class);

        verify(mDatabase, times(1)).insertWithOnConflict(arg1.capture(), arg2.capture(), valuesArgsCaptor.capture(), arg4.capture());

        Integer type = valuesArgsCaptor.getValue().getAsInteger(Constants.KeyValueTable.KeyValueTableEntry.FIELD_TYPE);

        assertThat(type).isEqualTo(Constants.KeyValueTable.LONG);
    }

    @Test
    public void testStringWrite() throws Exception {
        dbHelper = createHelper(mDbStorage);
        dbHelper.put(KEY_STRING, "123");

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentValues> valuesArgsCaptor = ArgumentCaptor.forClass(ContentValues.class);
        ArgumentCaptor<Integer> arg4 = ArgumentCaptor.forClass(Integer.class);

        verify(mDatabase, times(1)).insertWithOnConflict(arg1.capture(), arg2.capture(), valuesArgsCaptor.capture(), arg4.capture());

        Integer type = valuesArgsCaptor.getValue().getAsInteger(Constants.KeyValueTable.KeyValueTableEntry.FIELD_TYPE);
        String value = valuesArgsCaptor.getValue().getAsString(Constants.KeyValueTable.KeyValueTableEntry.FIELD_VALUE);

        assertThat(type).isEqualTo(Constants.KeyValueTable.STRING);
        assertThat(value).isEqualTo("123");
    }

    @Test
    public void testIntWrite() throws Exception {
        dbHelper = createHelper(mDbStorage);
        dbHelper.put(KEY_INT, 123);

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentValues> valuesArgsCaptor = ArgumentCaptor.forClass(ContentValues.class);
        ArgumentCaptor<Integer> arg4 = ArgumentCaptor.forClass(Integer.class);

        verify(mDatabase, times(1)).insertWithOnConflict(arg1.capture(), arg2.capture(), valuesArgsCaptor.capture(), arg4.capture());

        Integer type = valuesArgsCaptor.getValue().getAsInteger(Constants.KeyValueTable.KeyValueTableEntry.FIELD_TYPE);

        assertThat(type).isEqualTo(Constants.KeyValueTable.INT);
    }

    @Test
    public void keys() {
        dbHelper = createHelper(mDbStorage);
        dbHelper.put("key1", "value1");
        dbHelper.put("key2", 2);
        dbHelper.put("key3", "value3");
        dbHelper.put("key4", 4);
        dbHelper.remove("key2");
        dbHelper.remove("key3");
        assertThat(dbHelper.keys()).containsExactlyInAnyOrder("key1", "key4");
        // check no concurrent modification exception
        for (String key : dbHelper.keys()) {
            dbHelper.remove(key);
        }
    }

    private static KeyValueTableDbHelper createHelper(@NonNull DatabaseStorage storage) {
        // Create a synchronous executor for tests to avoid timing issues
        IHandlerExecutor syncExecutor = mock(IHandlerExecutor.class);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(syncExecutor).execute(any(Runnable.class));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(syncExecutor).executeDelayed(any(Runnable.class), anyLong());

        return new KeyValueTableDbHelper(
            Constants.PreferencesTable.TABLE_NAME,
            new SimpleDBConnector(storage),
            syncExecutor
        );
    }
}
