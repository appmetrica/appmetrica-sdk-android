package io.appmetrica.analytics.impl.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.testutils.CommonTest;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TablesManagerTest extends CommonTest {

    private static String TEST_TABLE_NAME = "test_table";
    private String[] mTestTableColumns = {
            "ID",
            "TEST_COLUMN_1",
            "TEST_COLUMN_2",
            "TEST_COLUMN_4",
            "TEST_COLUMN_3"
    };
    private SQLiteDatabase mDBMock;
    private TablesManager.Creator mCreator;

    private HashMultimap<Integer, DatabaseScript> createUpdateScriptsMap() {
        HashMultimap<Integer, DatabaseScript> scripts =
                new HashMultimap<>();
        DatabaseScript updateTo10 =
                mock(DatabaseScript.class);
        DatabaseScript updateTo20 =
                mock(DatabaseScript.class);
        DatabaseScript updateTo30 =
                mock(DatabaseScript.class);
        DatabaseScript updateTo40 =
                mock(DatabaseScript.class);
        scripts.put(10, updateTo10);
        scripts.put(20, updateTo20);
        scripts.put(30, updateTo30);
        scripts.put(40, updateTo40);
        return scripts;
    }

    @Before
    public void createmDBMock() {
        mDBMock = mock(SQLiteDatabase.class);
        mCreator = new TablesManager.Creator();
    }

    @Test
    public void testDbUpgrade() throws SQLException, JSONException {
        HashMultimap<Integer, DatabaseScript> scripts = createUpdateScriptsMap();
        TablesManager manager = createTableManagerSpyWithMockedScripts(scripts, new ConfidingTablesValidator());
        manager.onUpgrade(mDBMock, 10, BuildConfig.API_LEVEL);
        verify(manager, times(0)).recreateDatabase(any(SQLiteDatabase.class));
        verify(scripts.get(10).iterator().next(), never()).runScript(mDBMock);
        verify(scripts.get(20).iterator().next(), times(1)).runScript(mDBMock);
        verify(scripts.get(30).iterator().next(), times(1)).runScript(mDBMock);
        verify(scripts.get(40).iterator().next(), times(1)).runScript(mDBMock);
    }

    private TablesManager createTableManagerSpyWithMockedScripts(HashMultimap<Integer, DatabaseScript> scripts, TablesValidator validator) {
        return spy(mCreator.createTablesManager(
                "test",
                mock(DatabaseScript.class),
                mock(DatabaseScript.class),
                scripts,
                validator
        ));
    }

    @Test
    public void testDbUpgradeWithoutReset() throws SQLException, JSONException {
        HashMultimap<Integer, DatabaseScript> scripts = createUpdateScriptsMap();
        TablesManager manager = createTableManagerSpyWithMockedScripts(scripts, new ConfidingTablesValidator());
        manager.onUpgrade(mDBMock, 10, BuildConfig.API_LEVEL);
        verify(manager, times(0)).recreateDatabase(any(SQLiteDatabase.class));
        verify(scripts.get(10).iterator().next(), never()).runScript(mDBMock);
        verify(scripts.get(20).iterator().next(), times(1)).runScript(mDBMock);
        verify(scripts.get(30).iterator().next(), times(1)).runScript(mDBMock);
        verify(scripts.get(40).iterator().next(), times(1)).runScript(mDBMock);
    }

    @Test
    public void testDbUpgradeWithError() throws SQLException, JSONException {
        HashMultimap<Integer, DatabaseScript> scripts = createUpdateScriptsMap();
        TablesManager manager = createTableManagerSpyWithMockedScripts(scripts, new ConfidingTablesValidator());
        doThrow(SQLException.class).when(scripts.get(20).iterator().next()).runScript(mDBMock);
        manager.onUpgrade(mDBMock, 10, BuildConfig.API_LEVEL);
        verify(manager, times(1)).recreateDatabase(any(SQLiteDatabase.class));
        verify(scripts.get(10).iterator().next(), never()).runScript(mDBMock);
        verify(scripts.get(20).iterator().next(), times(1)).runScript(mDBMock);
        verify(scripts.get(30).iterator().next(), times(0)).runScript(mDBMock);
        verify(scripts.get(40).iterator().next(), times(0)).runScript(mDBMock);
    }

    @Test
    public void testCreateWithExceptionDoesntCrashService() throws SQLException, JSONException {
        DatabaseScript createMock =
                mock(DatabaseScript.class);
        TablesManager manager = spy(mCreator.createTablesManager(
                "test",
                createMock,
                mock(DatabaseScript.class),
                new HashMultimap<Integer, DatabaseScript>(),
                mock(TablesValidator.class)
        ));
        doThrow(IllegalStateException.class).when(createMock).runScript(mDBMock);
        manager.onCreate(mDBMock);
    }

    @Test
    public void testDropExceptionDoesntCrashService() throws SQLException, JSONException {
        DatabaseScript createMock =
                mock(DatabaseScript.class);
        DatabaseScript dropMock =
                mock(DatabaseScript.class);
        TablesManager manager = spy(mCreator.createTablesManager(
                "test",
                createMock,
                dropMock,
                new HashMultimap<Integer, DatabaseScript>(),
                mock(TablesValidator.class)
        ));
        doThrow(IllegalStateException.class).when(dropMock).runScript(mDBMock);
        manager.recreateDatabase(mDBMock);
    }

    @Test
    public void testOpenWithExceptionDoesntCrashService() throws SQLException, JSONException {
        DatabaseScript createMock =
                mock(DatabaseScript.class);
        TablesValidator validator = mock(TablesValidator.class);
        TablesManager manager = spy(mCreator.createTablesManager(
                "test",
                createMock,
                mock(DatabaseScript.class),
                new HashMultimap<Integer, DatabaseScript>(),
                validator
        ));
        doThrow(IllegalStateException.class).when(validator).isDbSchemeValid(mDBMock);
        manager.onOpen(mDBMock);
    }

    @Test
    public void testSuccessfulUpgrade() {
        TablesManager manager = spy(mCreator.createTablesManager(
                "test",
                mock(DatabaseScript.class),
                mock(DatabaseScript.class),
                new HashMultimap<Integer, DatabaseScript>(),
                new ConfidingTablesValidator()
        ));
        manager.onUpgrade(mDBMock, 10, BuildConfig.API_LEVEL);
        verify(manager, times(0)).recreateDatabase(any(SQLiteDatabase.class));
    }

    @Test
    public void testSuccessfulUpgradeWithOneTable() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getColumnNames()).thenReturn(mTestTableColumns);
        when(mDBMock.query(
                eq(TEST_TABLE_NAME),
                isNull(String[].class),
                isNull(String.class),
                isNull(String[].class),
                isNull(String.class),
                isNull(String.class),
                isNull(String.class))
        ).thenReturn(cursor);
        TablesManager manager = spy(mCreator.createTablesManager(
                "test",
                mock(DatabaseScript.class),
                mock(DatabaseScript.class),
                new HashMultimap<Integer, DatabaseScript>(),
                new TablesValidatorImpl("test",
                        new HashMap<String, List<String>>() {
                            {
                                put(TEST_TABLE_NAME, CollectionUtils.createSortedListWithoutRepetitions(mTestTableColumns));
                            }
                        }
                )
        ));
        manager.onUpgrade(mDBMock, 10, BuildConfig.API_LEVEL);
        verify(manager, times(0)).recreateDatabase(any(SQLiteDatabase.class));
    }

    @Test
    public void testUnsuccessfulUpgradeDueToSchemeError() {
        TablesValidator validator = mock(TablesValidator.class);
        TablesManager manager = spy(mCreator.createTablesManager(
                "test",
                mock(DatabaseScript.class),
                mock(DatabaseScript.class),
                new HashMultimap<Integer, DatabaseScript>(),
                validator
        ));
        when(validator.isDbSchemeValid(any(SQLiteDatabase.class))).thenReturn(false);
        manager.onUpgrade(mDBMock, 10, BuildConfig.API_LEVEL);
        verify(manager, times(1)).recreateDatabase(any(SQLiteDatabase.class));
    }

    @Test
    public void testUnsuccessfulUpgradeDueToUpgradeErrors() {
        TablesManager manager = spy(Constants.getDatabaseManagerProvider().buildComponentDatabaseManager(new ComponentId("package", "apiKey")));
        when(manager.getValidator()).thenReturn(new ConfidingTablesValidator());

        manager.onUpgrade(mDBMock, 10, BuildConfig.API_LEVEL);
        verify(manager, times(1)).recreateDatabase(any(SQLiteDatabase.class));
    }

    @Test
    public void downgradeOldLevelIsGreater() throws SQLException, JSONException {
        DatabaseScript dropDbScript = mock(DatabaseScript.class);
        DatabaseScript createDbScript = mock(DatabaseScript.class);
        TablesManager manager = mCreator.createTablesManager(
                "test",
                createDbScript,
                dropDbScript,
                new HashMultimap<Integer, DatabaseScript>(),
                new ConfidingTablesValidator()
        );
        manager.onDowngrade(mDBMock, 52, 51);
        InOrder inOrder = Mockito.inOrder(dropDbScript, createDbScript);
        inOrder.verify(dropDbScript).runScript(mDBMock);
        inOrder.verify(createDbScript).runScript(mDBMock);
    }

    @Test
    public void downgradeDropThrowsException() throws SQLException, JSONException {
        DatabaseScript dropDbScript = mock(DatabaseScript.class);
        DatabaseScript createDbScript = mock(DatabaseScript.class);
        doThrow(new RuntimeException()).when(dropDbScript).runScript(mDBMock);
        TablesManager manager = mCreator.createTablesManager(
                "test",
                createDbScript,
                dropDbScript,
                new HashMultimap<Integer, DatabaseScript>(),
                new ConfidingTablesValidator()
        );
        manager.onDowngrade(mDBMock, 52, 51);
        InOrder inOrder = Mockito.inOrder(dropDbScript, createDbScript);
        inOrder.verify(dropDbScript).runScript(mDBMock);
        inOrder.verify(createDbScript).runScript(mDBMock);
    }

    @Test
    public void downgradeCreateThrowsException() throws SQLException, JSONException {
        DatabaseScript dropDbScript = mock(DatabaseScript.class);
        DatabaseScript createDbScript = mock(DatabaseScript.class);
        doThrow(new RuntimeException()).when(createDbScript).runScript(mDBMock);
        TablesManager manager = mCreator.createTablesManager(
                "test",
                createDbScript,
                dropDbScript,
                new HashMultimap<Integer, DatabaseScript>(),
                new ConfidingTablesValidator()
        );
        manager.onDowngrade(mDBMock, 52, 51);
        InOrder inOrder = Mockito.inOrder(dropDbScript, createDbScript);
        inOrder.verify(dropDbScript).runScript(mDBMock);
        inOrder.verify(createDbScript).runScript(mDBMock);
    }

    @Test
    public void downgradeOldLevelIsLess() {
        DatabaseScript dropDbScript = mock(DatabaseScript.class);
        DatabaseScript createDbScript = mock(DatabaseScript.class);
        TablesManager manager = mCreator.createTablesManager(
                "test",
                createDbScript,
                dropDbScript,
                new HashMultimap<Integer, DatabaseScript>(),
                new ConfidingTablesValidator()
        );
        manager.onDowngrade(mDBMock, 50, 51);
        verifyNoInteractions(createDbScript, dropDbScript);
    }

    @Test
    public void downgradeOldLevelIsTheSame() {
        DatabaseScript dropDbScript = mock(DatabaseScript.class);
        DatabaseScript createDbScript = mock(DatabaseScript.class);
        TablesManager manager = mCreator.createTablesManager(
                "test",
                createDbScript,
                dropDbScript,
                new HashMultimap<Integer, DatabaseScript>(),
                new ConfidingTablesValidator()
        );
        manager.onDowngrade(mDBMock, 50, 50);
        verifyNoInteractions(createDbScript, dropDbScript);
    }
}
