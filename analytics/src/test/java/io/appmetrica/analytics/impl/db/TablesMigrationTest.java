package io.appmetrica.analytics.impl.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.db.DatabaseRevisions.DatabaseRevision;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.LogRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.junit.After;
import org.junit.Rule;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

abstract class TablesMigrationTest extends CommonTest {

    @Rule
    public LogRule logRule = new LogRule();

    final DatabaseRevision mStartUpgradeFromRevision;
    protected final DatabaseManagerProvider mDatabaseManagerProvider;

    protected final List<Closeable> closeables = new ArrayList<>();
    protected final List<SQLiteOpenHelper> helpers = new ArrayList<>();

    protected final ComponentId componentId = new ComponentId("some.package", "apiKey");

    public TablesMigrationTest(int databaseRevisionNumber) {
        mStartUpgradeFromRevision = getDatabaseRevision(databaseRevisionNumber);
        mDatabaseManagerProvider = Constants.getDatabaseManagerProvider();
    }

    @After
    public void tearDown() throws Exception {
        for (Closeable closeable : closeables) {
            closeable.close();
        }
        for (SQLiteOpenHelper helper : helpers) {
            helper.close();
        }
    }

    DatabaseRevisions.DatabaseRevision getDatabaseRevision(final int databaseRevisionNumber) {
        DatabaseRevision databaseRevision = null;
        for (int i = 1; i <= databaseRevisionNumber; i++) {
            final HashMap<Integer, DatabaseRevision> databaseRevisions = getDatabaseRevisions();
            if (databaseRevisions.containsKey(i)) {
                databaseRevision = databaseRevisions.get(i);
            }
        }
        return databaseRevision;
    }

    abstract HashMap<Integer, DatabaseRevision> getDatabaseRevisions();

    void checkTableStructure(String tableName, List<String> expectedColumns) {
        SQLiteDatabase db = upgradeDbFromOldNoNewestUsingStubDbValidator(getOldDb());
        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        closeables.add(cursor);
        String[] actualColumns = cursor.getColumnNames();
        checkEqualityUnorderedArrays(actualColumns, expectedColumns);
    }

    private void checkEqualityUnorderedArrays(String[] actualArray, List<String> expectedList) {
        Arrays.sort(actualArray);
        assertThat(expectedList).containsExactly(actualArray);
    }

    SQLiteDatabase upgradeDbFromOldNoNewestUsingStubDbValidator(SQLiteDatabase oldDatabase) {
        oldDatabase.close();
        return getUpgradedDbWithStubDbValidator();
    }

    SQLiteDatabase upgradeDbFromOldToNewest(SQLiteDatabase oldDatabase) {
        oldDatabase.close();
        return getUpgradedDb();
    }

    SQLiteDatabase getOldDb() {
        OldRevisionDatabaseHelper oldRevisionDatabaseHelper =
            new OldRevisionDatabaseHelper(RuntimeEnvironment.getApplication(), mStartUpgradeFromRevision);
        oldRevisionDatabaseHelper.close();
        helpers.add(oldRevisionDatabaseHelper);
        SQLiteDatabase database = oldRevisionDatabaseHelper.getWritableDatabase();
        closeables.add(database);
        return database;
    }

    SQLiteDatabase getUpgradedDb() {
        UpgradeDatabaseHelper newHelper = new UpgradeDatabaseHelper(
            RuntimeEnvironment.getApplication(), getTablesManager(), AppMetrica.getLibraryApiLevel());
        helpers.add(newHelper);
        SQLiteDatabase newDatabase = newHelper.getWritableDatabase();
        closeables.add(newDatabase);
        return newDatabase;
    }

    SQLiteDatabase getUpgradedDbWithStubDbValidator() {
        TablesManager tablesManager =
            new TablesManager.Creator().createTablesManager(getDbTag(), getCreateDbScript(),
                getDropDbScript(), getUpgradeScripts(), new TablesValidator() {
                    public boolean isDbSchemeValid(SQLiteDatabase database) {
                        return true;
                    }
                });
        UpgradeDatabaseHelper newHelper = new UpgradeDatabaseHelper(RuntimeEnvironment.getApplication(),
            tablesManager, AppMetrica.getLibraryApiLevel());
        helpers.add(newHelper);
        SQLiteDatabase newDatabase = newHelper.getWritableDatabase();
        closeables.add(newDatabase);
        return newDatabase;
    }

    abstract String getDbTag();

    abstract DatabaseScript getCreateDbScript();

    abstract DatabaseScript getDropDbScript();

    abstract HashMultimap<Integer, DatabaseScript> getUpgradeScripts();

    abstract TablesManager getTablesManager();

    public static Collection<Object[]> prepareListOfAllRevisions(int firstRevisionNumber) {
        Collection<Integer> firstRevisions = TestUtils.generateSequence(firstRevisionNumber, AppMetrica.getLibraryApiLevel() + 1);
        Collection<Object[]> data = new ArrayList<Object[]>();
        for (Integer revision : firstRevisions) {
            data.add(new Object[]{revision});
        }
        return data;
    }
}
