package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.sql.SQLException;
import java.util.Collections;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class RemoveBackgroundSessionIDTest extends CommonTest {

    private static final long SESSION_ID = 10000000030L;
    @Mock
    private ComponentUnit mComponentUnit;
    @Mock
    private ReportRequestConfig reportRequestConfig;
    private Context mContext;

    private DatabaseStorage testDB;
    DatabaseHelper helper;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = mRule.getContext();
        when(mComponentUnit.getContext()).thenReturn(mContext);
        when(mComponentUnit.getFreshReportRequestConfig()).thenReturn(reportRequestConfig);
        when(mComponentUnit.getStartupState()).thenReturn(TestUtils.createDefaultStartupState());
        final ComponentId componentId = mock(ComponentId.class);
        when(componentId.toStringAnonymized()).thenReturn("component_id");
        when(mComponentUnit.getComponentId()).thenReturn(componentId);
        testDB = new DatabaseStorage(
            mContext,
            "test_db",
            Constants.getDatabaseManagerProvider().buildComponentDatabaseManager(
                new ComponentId("package", "apiKey")
            )
        );
        helper = new DatabaseHelper(mComponentUnit, testDB);
        ServerTime.getInstance().init();
    }

    @Test
    public void testRemoveFromReports() throws SQLException, JSONException {
        final ContentValues reportValues = new ContentValues();
        reportValues.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, 1);
        reportValues.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME, 1000);
        reportValues.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION, SESSION_ID);
        reportValues.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE, 0);

        helper.insertEvents(Collections.singletonList(reportValues));

        SQLiteDatabase db = testDB.getWritableDatabase();

        assertThat(queryFromReports(db)).extracting("count").isEqualTo(1);
    }

    @Test
    public void testRemoveFromSessions() throws SQLException, JSONException {
        final ContentValues sessionValues = new ContentValues();
        sessionValues.put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID, SESSION_ID);
        sessionValues.put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE, 0);
        helper.addSessionValues(sessionValues);

        SQLiteDatabase db = testDB.getWritableDatabase();

        assertThat(queryFromSessions(db)).extracting("count").isEqualTo(1);
    }

    private Cursor queryFromReports(SQLiteDatabase db) {
        return db.query(
            Constants.EventsTable.TABLE_NAME,
            new String[]{Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION},
            Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION + " = ?",
            new String[]{String.valueOf(SESSION_ID)},
            null, null, null
        );
    }

    private Cursor queryFromSessions(SQLiteDatabase db) {
        return db.query(
            Constants.SessionTable.TABLE_NAME,
            new String[]{Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID},
            Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID + " = ?",
            new String[]{String.valueOf(SESSION_ID)},
            null, null, null
        );
    }
}
