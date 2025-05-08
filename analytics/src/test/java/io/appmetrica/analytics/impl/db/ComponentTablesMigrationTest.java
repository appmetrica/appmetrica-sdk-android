package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.db.DatabaseRevisions.DatabaseRevision;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.constants.Constants.BinaryDataTable;
import io.appmetrica.analytics.impl.db.constants.Constants.EventsTable;
import io.appmetrica.analytics.impl.db.constants.Constants.EventsTable.EventTableEntry;
import io.appmetrica.analytics.impl.db.constants.Constants.SessionTable;
import io.appmetrica.analytics.impl.db.constants.Constants.SessionTable.SessionTableEntry;
import io.appmetrica.analytics.impl.db.constants.DatabaseScriptsHolder;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ComponentTablesMigrationTest extends TablesMigrationTest {

    private static final HashMap<Integer, DatabaseRevision> TESTED_REVISIONS = new HashMap<Integer, DatabaseRevision>() {
        {
            put(112, new DatabaseRevisions.DatabaseV112());
        }
    };

    @ParameterizedRobolectricTestRunner.Parameters(name = "Update from db revision {0}")
    public static Collection<Object[]> data() {
        return prepareListOfAllRevisions(112);
    }

    public static final Collection<Integer> DROP_DATA_ON_UPGRADE_FROM_VERSION = Collections.emptyList();

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    public ComponentTablesMigrationTest(int databaseRevisionNumber) {
        super(databaseRevisionNumber);
    }

    @Override
    HashMap<Integer, DatabaseRevision> getDatabaseRevisions() {
        return TESTED_REVISIONS;
    }

    @Test
    public void testCanSaveSessionAfterUpgrade() {
        tryToSaveSession(upgradeDbFromOldToNewest(getOldDb()));
    }

    @Test
    public void testCanSaveReportAfterUpgrade() {
        tryToSaveReport(upgradeDbFromOldToNewest(getOldDb()));
    }

    @Test
    public void testCanSaveBinaryDataAfterUpgrade() {
        SQLiteDatabase database = upgradeDbFromOldToNewest(getOldDb());
        String key = "Binary data test key";
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22, 23};
        ContentValues contentValues = new ContentValues();
        contentValues.put(BinaryDataTable.DATA_KEY, key);
        contentValues.put(BinaryDataTable.VALUE, data);

        assertThat(database.insertOrThrow(BinaryDataTable.TABLE_NAME, null, contentValues))
            .isGreaterThan(0);

        Cursor cursor = database.query(
            BinaryDataTable.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        );

        closeables.add(cursor);

        assertThat(cursor.getCount()).isEqualTo(1);
        assertThat(cursor.moveToNext()).isTrue();
        assertThat(cursor.getString(cursor.getColumnIndex(BinaryDataTable.DATA_KEY))).isEqualTo(key);
        assertThat(cursor.getBlob(cursor.getColumnIndex(BinaryDataTable.VALUE))).isEqualTo(data);
    }

    @Test
    public void testShouldNotLoseSavedReportAfterDbUpgrade() {
        SQLiteDatabase oldDb = getOldDb();
        oldDb.insertOrThrow(EventsTable.TABLE_NAME, null, createSimpleEventContentValues());
        Cursor cursor = upgradeDbFromOldToNewest(getOldDb())
            .query(EventsTable.TABLE_NAME, null, null, null, null, null, null);
        closeables.add(cursor);
        assertThat(cursor.getCount())
            .isEqualTo(DROP_DATA_ON_UPGRADE_FROM_VERSION.contains(mStartUpgradeFromRevision.getVersion()) ? 0 : 1);
    }

    @Test
    public void testShouldNotLoseSavedSessionAfterDbUpgrade() {
        SQLiteDatabase oldDb = getOldDb();
        oldDb.insertOrThrow(SessionTable.TABLE_NAME, null, createSimpleSessionContentValues());
        Cursor cursor = upgradeDbFromOldToNewest(getOldDb())
            .query(SessionTable.TABLE_NAME, null, null, null, null, null, null);
        closeables.add(cursor);
        assertThat(cursor.getCount())
            .isEqualTo(DROP_DATA_ON_UPGRADE_FROM_VERSION.contains(mStartUpgradeFromRevision.getVersion()) ? 0 : 1);
    }

    @Test
    public void testShouldBeValidReportsTableStructureAfterDbUpgrade() {
        checkTableStructure(EventsTable.TABLE_NAME, EventsTable.ACTUAL_COLUMNS);
    }

    @Test
    public void testShouldBeValidSessionTableStructureAfterDbUpgrade() {
        checkTableStructure(SessionTable.TABLE_NAME, SessionTable.ACTUAL_COLUMNS);
    }

    @Test
    public void testShouldBeValidBinaryTableStructureAfterDbUpdate() {
        checkTableStructure(BinaryDataTable.TABLE_NAME, BinaryDataTable.ACTUAL_COLUMNS);
    }

    private ContentValues createSimpleEventContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, new Random().nextInt());
        return contentValues;
    }

    private ContentValues createSimpleSessionContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SessionTableEntry.FIELD_SESSION_ID, new Random().nextInt());
        return contentValues;
    }

    @Override
    protected String getDbTag() {
        return "main";
    }

    @Override
    DatabaseScript getCreateDbScript() {
        return new DatabaseScriptsHolder.ComponentDatabaseCreateScript();
    }

    @Override
    DatabaseScript getDropDbScript() {
        return new DatabaseScriptsHolder.ComponentDatabaseDropScript();
    }

    @Override
    HashMultimap<Integer, DatabaseScript> getUpgradeScripts() {
        return Constants.getUpgradeDbScript();
    }

    @Override
    TablesManager getTablesManager() {
        return mDatabaseManagerProvider.buildComponentDatabaseManager(componentId);
    }

    private static void tryToSaveReport(SQLiteDatabase database) {
        ContentValues values = new ContentValues();

        values.put(EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, 2);
        values.put(EventTableEntry.FIELD_EVENT_TYPE, 2);
        values.put(EventTableEntry.FIELD_EVENT_TIME, 1000);
        values.put(EventTableEntry.FIELD_EVENT_SESSION, 12);
        values.put(EventTableEntry.FIELD_EVENT_SESSION_TYPE, 1);
        values.put(EventTableEntry.FIELD_EVENT_DESCRIPTION, "description".getBytes(StandardCharsets.UTF_8));

        //Now it's just a copy-paste of insertion logic from DatabaseHelper.insertReports()
        //But it's not an unit test, it's integration test. Main goal is to check migration from
        //different database revisions, but not DatabaseHelper's methods.
        long l = database.insert(EventsTable.TABLE_NAME, null, values);
        System.out.println(l);

        SQLiteDatabase wDatabase = database;
        wDatabase.beginTransaction();

        assertThat(wDatabase.insertOrThrow(EventsTable.TABLE_NAME, null, values)).isGreaterThan(0);

        wDatabase.setTransactionSuccessful();
        Utils.endTransaction(wDatabase);
    }

    private void tryToSaveSession(SQLiteDatabase database) {
        ContentValues session = new ContentValues();

        session.put(SessionTableEntry.FIELD_SESSION_ID, 1);
        session.put(SessionTableEntry.FIELD_SESSION_TYPE, 1);
        session.put(SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS, "request_params");
        session.put(SessionTableEntry.FIELD_SESSION_DESCRIPTION, "some string".getBytes(StandardCharsets.UTF_8));

        database.insertOrThrow(SessionTable.TABLE_NAME, null, session);
    }
}
